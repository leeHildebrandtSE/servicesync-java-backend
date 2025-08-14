-- ServiceSync Database Migration Script
-- Version: 1.0.0
-- Description: Create database schema for ServiceSync application

-- Create database (run as superuser)
-- CREATE DATABASE servicesync_prod;
-- CREATE USER servicesync_prod WITH PASSWORD 'your_secure_password_here';
-- GRANT ALL PRIVILEGES ON DATABASE servicesync_prod TO servicesync_prod;

-- Connect to servicesync_prod database
\c servicesync_prod;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create custom types
CREATE TYPE employee_role AS ENUM ('HOSTESS', 'NURSE', 'SUPERVISOR', 'ADMIN');
CREATE TYPE meal_type AS ENUM ('BREAKFAST', 'LUNCH', 'SUPPER', 'BEVERAGES');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED');
CREATE TYPE qr_location_type AS ENUM ('KITCHEN_EXIT', 'WARD_ARRIVAL', 'NURSE_STATION');

-- Create audit trigger function
CREATE OR REPLACE FUNCTION audit_trigger_row()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        NEW.updated_at = NOW();
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        NEW.created_at = NOW();
        NEW.updated_at = NOW();
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create hospitals table
CREATE TABLE hospitals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT hospitals_code_check CHECK (code ~ '^[A-Z0-9_]+$'),
    CONSTRAINT hospitals_email_check CHECK (contact_email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Create wards table
CREATE TABLE wards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hospital_id UUID REFERENCES hospitals(id) ON DELETE RESTRICT NOT NULL,
    name VARCHAR(100) NOT NULL,
    floor_number INTEGER CHECK (floor_number > 0),
    capacity INTEGER CHECK (capacity > 0),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW() NOT NULL,

    UNIQUE(hospital_id, name)
);

-- Create employees table
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role employee_role NOT NULL,
    hospital_id UUID REFERENCES hospitals(id) ON DELETE RESTRICT NOT NULL,
    shift_schedule TEXT,
    is_active BOOLEAN DEFAULT true NOT NULL,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT employees_employee_id_check CHECK (employee_id ~ '^[A-Z]\d{3}$'),
    CONSTRAINT employees_email_check CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Create service_sessions table
CREATE TABLE service_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id VARCHAR(100) UNIQUE NOT NULL,
    employee_id UUID REFERENCES employees(id) ON DELETE RESTRICT NOT NULL,
    ward_id UUID REFERENCES wards(id) ON DELETE RESTRICT NOT NULL,
    meal_type meal_type NOT NULL,
    meal_count INTEGER CHECK (meal_count > 0 AND meal_count <= 100) NOT NULL,
    meals_served INTEGER DEFAULT 0 CHECK (meals_served >= 0) NOT NULL,
    status session_status DEFAULT 'ACTIVE' NOT NULL,

    -- Timestamp fields
    kitchen_exit_time TIMESTAMP,
    ward_arrival_time TIMESTAMP,
    nurse_alert_time TIMESTAMP,
    nurse_response_time TIMESTAMP,
    service_start_time TIMESTAMP,
    service_complete_time TIMESTAMP,

    -- Additional fields
    comments TEXT,
    nurse_name VARCHAR(255),

    -- Diet sheet documentation
    diet_sheet_photo_path VARCHAR(500),
    diet_sheet_notes TEXT,
    diet_sheet_documented BOOLEAN DEFAULT false NOT NULL,

    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT sessions_meals_served_check CHECK (meals_served <= meal_count),
    CONSTRAINT sessions_timestamps_check CHECK (
        (kitchen_exit_time IS NULL OR ward_arrival_time IS NULL OR kitchen_exit_time <= ward_arrival_time) AND
        (nurse_alert_time IS NULL OR nurse_response_time IS NULL OR nurse_alert_time <= nurse_response_time) AND
        (service_start_time IS NULL OR service_complete_time IS NULL OR service_start_time <= service_complete_time)
    )
);

-- Create performance tracking table
CREATE TABLE performance_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES service_sessions(id) ON DELETE CASCADE NOT NULL,
    travel_time_seconds INTEGER,
    nurse_response_time_seconds INTEGER,
    serving_time_seconds INTEGER,
    total_duration_seconds INTEGER,
    completion_rate DECIMAL(5,2),
    efficiency_rating VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW() NOT NULL
);

-- Create indexes for performance
CREATE INDEX idx_hospitals_code ON hospitals(code);
CREATE INDEX idx_hospitals_active ON hospitals(is_active) WHERE is_active = true;

CREATE INDEX idx_wards_hospital_id ON wards(hospital_id);
CREATE INDEX idx_wards_active ON wards(is_active) WHERE is_active = true;
CREATE INDEX idx_wards_hospital_active ON wards(hospital_id, is_active) WHERE is_active = true;

CREATE INDEX idx_employees_employee_id ON employees(employee_id);
CREATE INDEX idx_employees_hospital_id ON employees(hospital_id);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_role ON employees(role);
CREATE INDEX idx_employees_active ON employees(is_active) WHERE is_active = true;
CREATE INDEX idx_employees_hospital_role ON employees(hospital_id, role) WHERE is_active = true;

CREATE INDEX idx_sessions_session_id ON service_sessions(session_id);
CREATE INDEX idx_sessions_employee_id ON service_sessions(employee_id);
CREATE INDEX idx_sessions_ward_id ON service_sessions(ward_id);
CREATE INDEX idx_sessions_status ON service_sessions(status);
CREATE INDEX idx_sessions_created_at ON service_sessions(created_at);
CREATE INDEX idx_sessions_employee_status ON service_sessions(employee_id, status);
CREATE INDEX idx_sessions_ward_status ON service_sessions(ward_id, status);
CREATE INDEX idx_sessions_status_created ON service_sessions(status, created_at);
CREATE INDEX idx_sessions_meal_type_created ON service_sessions(meal_type, created_at);
CREATE INDEX idx_sessions_stale_active ON service_sessions(status, created_at) WHERE status = 'ACTIVE';

-- Performance metrics indexes
CREATE INDEX idx_performance_session_id ON performance_metrics(session_id);
CREATE INDEX idx_performance_created_at ON performance_metrics(created_at);

-- Create audit triggers
CREATE TRIGGER hospitals_audit_trigger
    BEFORE INSERT OR UPDATE ON hospitals
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_row();

CREATE TRIGGER wards_audit_trigger
    BEFORE INSERT OR UPDATE ON wards
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_row();

CREATE TRIGGER employees_audit_trigger
    BEFORE INSERT OR UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_row();

CREATE TRIGGER sessions_audit_trigger
    BEFORE INSERT OR UPDATE ON service_sessions
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_row();

-- Create views for reporting
CREATE VIEW v_active_sessions AS
SELECT
    s.id,
    s.session_id,
    e.name as employee_name,
    e.employee_id,
    e.role as employee_role,
    w.name as ward_name,
    w.floor_number,
    h.name as hospital_name,
    h.code as hospital_code,
    s.meal_type,
    s.meal_count,
    s.meals_served,
    s.status,
    ROUND((s.meals_served::DECIMAL / s.meal_count * 100), 2) as completion_rate,
    s.kitchen_exit_time,
    s.ward_arrival_time,
    s.nurse_alert_time,
    s.nurse_response_time,
    s.service_start_time,
    s.service_complete_time,
    s.created_at,
    s.updated_at,
    CASE
        WHEN s.kitchen_exit_time IS NULL THEN 'Kitchen Exit'
        WHEN s.ward_arrival_time IS NULL THEN 'Ward Arrival'
        WHEN NOT s.diet_sheet_documented THEN 'Diet Sheet Documentation'
        WHEN s.nurse_alert_time IS NULL THEN 'Nurse Alert'
        WHEN s.nurse_response_time IS NULL THEN 'Awaiting Nurse Response'
        WHEN s.service_start_time IS NULL THEN 'Nurse Station'
        ELSE 'Service in Progress'
    END as current_step
FROM service_sessions s
JOIN employees e ON s.employee_id = e.id
JOIN wards w ON s.ward_id = w.id
JOIN hospitals h ON w.hospital_id = h.id
WHERE s.status IN ('ACTIVE', 'IN_TRANSIT');

-- Create view for session analytics
CREATE VIEW v_session_analytics AS
SELECT
    DATE(s.created_at) as session_date,
    h.name as hospital_name,
    w.name as ward_name,
    e.role as employee_role,
    s.meal_type,
    COUNT(*) as total_sessions,
    COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completed_sessions,
    AVG(CASE WHEN s.status = 'COMPLETED' THEN s.meals_served::DECIMAL / s.meal_count * 100 END) as avg_completion_rate,
    AVG(CASE WHEN s.ward_arrival_time IS NOT NULL AND s.kitchen_exit_time IS NOT NULL
         THEN EXTRACT(EPOCH FROM (s.ward_arrival_time - s.kitchen_exit_time)) END) as avg_travel_time_seconds,
    AVG(CASE WHEN s.nurse_response_time IS NOT NULL AND s.nurse_alert_time IS NOT NULL
         THEN EXTRACT(EPOCH FROM (s.nurse_response_time - s.nurse_alert_time)) END) as avg_nurse_response_seconds
FROM service_sessions s
JOIN employees e ON s.employee_id = e.id
JOIN wards w ON s.ward_id = w.id
JOIN hospitals h ON w.hospital_id = h.id
GROUP BY DATE(s.created_at), h.name, w.name, e.role, s.meal_type;

-- Insert initial data
INSERT INTO hospitals (code, name, address, contact_email, contact_phone) VALUES
('WPC_GH', 'Western Province General Hospital', '123 Hospital St, Cape Town, Western Cape, 8001', 'admin@wpcgh.co.za', '+27 21 123 4567'),
('WPC_MH', 'Western Province Maternity Hospital', '456 Medical Ave, Cape Town, Western Cape, 8001', 'admin@wpcmh.co.za', '+27 21 234 5678'),
('TYGERBERG', 'Tygerberg Hospital', '789 University Rd, Tygerberg, Western Cape, 7505', 'admin@tygerberg.co.za', '+27 21 345 6789');

-- Insert wards
INSERT INTO wards (hospital_id, name, floor_number, capacity) VALUES
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3A - General Medicine', 3, 25),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3B - General Medicine', 3, 30),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '4A - Surgery', 4, 20),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '4B - Surgery', 4, 18),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '5A - ICU', 5, 12),
((SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'Maternity Ward A', 2, 20),
((SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'Maternity Ward B', 2, 22),
((SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'NICU', 3, 15),
((SELECT id FROM hospitals WHERE code = 'TYGERBERG'), 'Trauma Unit', 1, 30),
((SELECT id FROM hospitals WHERE code = 'TYGERBERG'), 'Cardiology', 4, 25);

-- Insert employees (passwords are bcrypt hashed versions of 'password123')
INSERT INTO employees (employee_id, name, email, password_hash, role, hospital_id, shift_schedule) VALUES
-- WPC General Hospital
('H001', 'Sarah Johnson', 'sarah.johnson@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'HOSTESS', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3A,3B,4A'),
('H002', 'Michael Brown', 'michael.brown@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'HOSTESS', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), '4B,5A'),
('N001', 'Mary Williams', 'mary.williams@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3A'),
('N002', 'Jennifer Davis', 'jennifer.davis@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3B'),
('N003', 'Robert Wilson', 'robert.wilson@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), '4A'),
('S001', 'David Smith', 'david.smith@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'SUPERVISOR', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), 'ALL'),
('A001', 'Administrator', 'admin@wpcgh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'ADMIN', (SELECT id FROM hospitals WHERE code = 'WPC_GH'), 'ALL'),

-- WPC Maternity Hospital
('H003', 'Lisa Thompson', 'lisa.thompson@wpcmh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'HOSTESS', (SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'Maternity Ward A,Maternity Ward B'),
('N004', 'Amanda Garcia', 'amanda.garcia@wpcmh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'Maternity Ward A'),
('N005', 'Michelle Martinez', 'michelle.martinez@wpcmh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'NICU'),
('S002', 'James Rodriguez', 'james.rodriguez@wpcmh.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'SUPERVISOR', (SELECT id FROM hospitals WHERE code = 'WPC_MH'), 'ALL'),

-- Tygerberg Hospital
('H004', 'Karen Anderson', 'karen.anderson@tygerberg.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'HOSTESS', (SELECT id FROM hospitals WHERE code = 'TYGERBERG'), 'Trauma Unit'),
('N006', 'Christopher Taylor', 'christopher.taylor@tygerberg.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'TYGERBERG'), 'Trauma Unit'),
('S003', 'Patricia Thomas', 'patricia.thomas@tygerberg.co.za', '$2a$10$N9qo8uLOickgx2ZMRZoMye6p8p.dYDpAYs0n.T3P2iU1nDuVsF3L2', 'SUPERVISOR', (SELECT id FROM hospitals WHERE code = 'TYGERBERG'), 'ALL');

-- Insert sample service session for testing
INSERT INTO service_sessions (session_id, employee_id, ward_id, meal_type, meal_count, status, comments) VALUES
('INIT-2024-001',
 (SELECT id FROM employees WHERE employee_id = 'H001'),
 (SELECT id FROM wards WHERE name = '3A - General Medicine'),
 'BREAKFAST', 12, 'COMPLETED', 'Database initialization complete');

-- Create stored procedures for common operations
CREATE OR REPLACE FUNCTION get_active_sessions_by_hospital(hospital_code VARCHAR)
RETURNS TABLE (
    session_id VARCHAR,
    employee_name VARCHAR,
    ward_name VARCHAR,
    meal_type meal_type,
    current_step TEXT,
    created_at TIMESTAMP
) AS $
BEGIN
    RETURN QUERY
    SELECT
        s.session_id,
        e.name,
        w.name,
        s.meal_type,
        CASE
            WHEN s.kitchen_exit_time IS NULL THEN 'Kitchen Exit'
            WHEN s.ward_arrival_time IS NULL THEN 'Ward Arrival'
            WHEN NOT s.diet_sheet_documented THEN 'Diet Sheet Documentation'
            WHEN s.nurse_alert_time IS NULL THEN 'Nurse Alert'
            WHEN s.nurse_response_time IS NULL THEN 'Awaiting Nurse Response'
            WHEN s.service_start_time IS NULL THEN 'Nurse Station'
            ELSE 'Service in Progress'
        END,
        s.created_at
    FROM service_sessions s
    JOIN employees e ON s.employee_id = e.id
    JOIN wards w ON s.ward_id = w.id
    JOIN hospitals h ON w.hospital_id = h.id
    WHERE h.code = hospital_code
    AND s.status IN ('ACTIVE', 'IN_TRANSIT')
    ORDER BY s.created_at DESC;
END;
$ LANGUAGE plpgsql;

-- Create function to calculate session performance
CREATE OR REPLACE FUNCTION calculate_session_performance(session_uuid UUID)
RETURNS TABLE (
    travel_time_seconds INTEGER,
    nurse_response_time_seconds INTEGER,
    serving_time_seconds INTEGER,
    total_duration_seconds INTEGER,
    completion_rate DECIMAL,
    efficiency_rating VARCHAR
) AS $
DECLARE
    session_rec service_sessions%ROWTYPE;
BEGIN
    SELECT * INTO session_rec FROM service_sessions WHERE id = session_uuid;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Session not found';
    END IF;

    RETURN QUERY
    SELECT
        CASE WHEN session_rec.kitchen_exit_time IS NOT NULL AND session_rec.ward_arrival_time IS NOT NULL
             THEN EXTRACT(EPOCH FROM (session_rec.ward_arrival_time - session_rec.kitchen_exit_time))::INTEGER
             ELSE NULL END,
        CASE WHEN session_rec.nurse_alert_time IS NOT NULL AND session_rec.nurse_response_time IS NOT NULL
             THEN EXTRACT(EPOCH FROM (session_rec.nurse_response_time - session_rec.nurse_alert_time))::INTEGER
             ELSE NULL END,
        CASE WHEN session_rec.service_start_time IS NOT NULL AND session_rec.service_complete_time IS NOT NULL
             THEN EXTRACT(EPOCH FROM (session_rec.service_complete_time - session_rec.service_start_time))::INTEGER
             ELSE NULL END,
        CASE WHEN session_rec.kitchen_exit_time IS NOT NULL AND session_rec.service_complete_time IS NOT NULL
             THEN EXTRACT(EPOCH FROM (session_rec.service_complete_time - session_rec.kitchen_exit_time))::INTEGER
             ELSE NULL END,
        ROUND((session_rec.meals_served::DECIMAL / session_rec.meal_count * 100), 2),
        CASE
            WHEN (session_rec.meals_served::DECIMAL / session_rec.meal_count * 100) >= 95 THEN 'Excellent'
            WHEN (session_rec.meals_served::DECIMAL / session_rec.meal_count * 100) >= 85 THEN 'Good'
            WHEN (session_rec.meals_served::DECIMAL / session_rec.meal_count * 100) >= 75 THEN 'Acceptable'
            ELSE 'Below Average'
        END;
END;
$ LANGUAGE plpgsql;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO servicesync_prod;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO servicesync_prod;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO servicesync_prod;

-- Create backup script execution permission
GRANT pg_read_all_data TO servicesync_prod;

-- Final verification
DO $
BEGIN
    RAISE NOTICE 'ServiceSync database migration completed successfully!';
    RAISE NOTICE 'Hospitals: %', (SELECT COUNT(*) FROM hospitals);
    RAISE NOTICE 'Wards: %', (SELECT COUNT(*) FROM wards);
    RAISE NOTICE 'Employees: %', (SELECT COUNT(*) FROM employees);
    RAISE NOTICE 'Active sessions view: %', (SELECT COUNT(*) FROM v_active_sessions);
END
$;
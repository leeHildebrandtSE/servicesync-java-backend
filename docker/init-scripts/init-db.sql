-- ServiceSync Database Initialization Script

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create enums
CREATE TYPE employee_role AS ENUM ('HOSTESS', 'NURSE', 'SUPERVISOR', 'ADMIN');
CREATE TYPE meal_type AS ENUM ('BREAKFAST', 'LUNCH', 'SUPPER', 'BEVERAGES');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED');
CREATE TYPE qr_location_type AS ENUM ('KITCHEN_EXIT', 'WARD_ARRIVAL', 'NURSE_STATION');

-- Create hospitals table
CREATE TABLE hospitals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create wards table
CREATE TABLE wards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hospital_id UUID REFERENCES hospitals(id),
    name VARCHAR(100) NOT NULL,
    floor_number INTEGER,
    capacity INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create employees table
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role employee_role NOT NULL,
    hospital_id UUID REFERENCES hospitals(id),
    shift_schedule JSONB,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create service_sessions table
CREATE TABLE service_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id VARCHAR(100) UNIQUE NOT NULL,
    employee_id UUID REFERENCES employees(id),
    ward_id UUID REFERENCES wards(id),
    meal_type meal_type NOT NULL,
    meal_count INTEGER NOT NULL,
    meals_served INTEGER DEFAULT 0,
    status session_status DEFAULT 'ACTIVE',
    kitchen_exit_time TIMESTAMP,
    ward_arrival_time TIMESTAMP,
    nurse_alert_time TIMESTAMP,
    nurse_response_time TIMESTAMP,
    service_start_time TIMESTAMP,
    service_complete_time TIMESTAMP,
    comments TEXT,
    nurse_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_employees_employee_id ON employees(employee_id);
CREATE INDEX idx_employees_hospital_id ON employees(hospital_id);
CREATE INDEX idx_sessions_session_id ON service_sessions(session_id);
CREATE INDEX idx_sessions_employee_ward ON service_sessions(employee_id, ward_id);
CREATE INDEX idx_sessions_status_created ON service_sessions(status, created_at);

-- Insert sample data
INSERT INTO hospitals (code, name, address, contact_email) VALUES
('WPC_GH', 'Western Province General Hospital', '123 Hospital St, Cape Town', 'admin@wpcgh.co.za'),
('WPC_MH', 'Western Province Maternity Hospital', '456 Medical Ave, Cape Town', 'admin@wpcmh.co.za');

INSERT INTO wards (hospital_id, name, floor_number, capacity) VALUES
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3A - General Medicine', 3, 25),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '3B - General Medicine', 3, 30),
((SELECT id FROM hospitals WHERE code = 'WPC_GH'), '4A - Surgery', 4, 20);

INSERT INTO employees (employee_id, name, email, password_hash, role, hospital_id) VALUES
('H001', 'Sarah Johnson', 'sarah.johnson@wpcgh.co.za', '$2a$10$hash1', 'HOSTESS', (SELECT id FROM hospitals WHERE code = 'WPC_GH')),
('N001', 'Mary Williams', 'mary.williams@wpcgh.co.za', '$2a$10$hash2', 'NURSE', (SELECT id FROM hospitals WHERE code = 'WPC_GH')),
('S001', 'David Smith', 'david.smith@wpcgh.co.za', '$2a$10$hash3', 'SUPERVISOR', (SELECT id FROM hospitals WHERE code = 'WPC_GH'));

-- Log initialization
INSERT INTO service_sessions (session_id, employee_id, ward_id, meal_type, meal_count, status, comments) VALUES
('INIT-2024-001',
 (SELECT id FROM employees WHERE employee_id = 'H001'),
 (SELECT id FROM wards WHERE name = '3A - General Medicine'),
 'BREAKFAST', 12, 'COMPLETED', 'Database initialization complete');

-- Print success message
DO $$
BEGIN
    RAISE NOTICE 'ServiceSync database initialized successfully!';
    RAISE NOTICE 'Hospitals: %', (SELECT COUNT(*) FROM hospitals);
    RAISE NOTICE 'Wards: %', (SELECT COUNT(*) FROM wards);
    RAISE NOTICE 'Employees: %', (SELECT COUNT(*) FROM employees);
END
$$;
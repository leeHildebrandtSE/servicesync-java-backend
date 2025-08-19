// DataInitializer.java - Fixed version with proper JSON formatting

package com.wpc.servicesync_backend.config;

import com.wpc.servicesync_backend.model.entity.*;
import com.wpc.servicesync_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if no hospitals exist
        if (hospitalRepository.count() == 0) {
            logger.info("Initializing sample data...");
            initializeData();
        }
    }

    private void initializeData() {
        // Create Hospitals
        Hospital hospital1 = new Hospital();
        hospital1.setCode("CTH001");
        hospital1.setName("Cape Town General Hospital");
        hospital1.setAddress("Anzio Road, Observatory, Cape Town, 7925");
        hospital1.setContactEmail("info@ctgh.co.za");
        hospital1.setContactPhone("+27-21-404-2911");
        hospital1.setIsActive(true);

        Hospital hospital2 = new Hospital();
        hospital2.setCode("GSH002");
        hospital2.setName("Groote Schuur Hospital");
        hospital2.setAddress("Main Road, Observatory, Cape Town, 7925");
        hospital2.setContactEmail("contact@gsh.co.za");
        hospital2.setContactPhone("+27-21-404-9111");
        hospital2.setIsActive(true);

        hospitalRepository.save(hospital1);
        hospitalRepository.save(hospital2);

        // Create Wards
        Ward ward1 = new Ward();
        ward1.setName("ICU");
        ward1.setFloorNumber(3);
        ward1.setCapacity(20);
        ward1.setHospital(hospital1);
        ward1.setIsActive(true);

        Ward ward2 = new Ward();
        ward2.setName("General Medicine");
        ward2.setFloorNumber(2);
        ward2.setCapacity(40);
        ward2.setHospital(hospital1);
        ward2.setIsActive(true);

        Ward ward3 = new Ward();
        ward3.setName("Pediatrics");
        ward3.setFloorNumber(1);
        ward3.setCapacity(30);
        ward3.setHospital(hospital2);
        ward3.setIsActive(true);

        wardRepository.save(ward1);
        wardRepository.save(ward2);
        wardRepository.save(ward3);

        // Create Employees with proper JSON formatting 👨‍⚕️👩‍⚕️
        Employee admin = Employee.builder()
                .employeeId("ADM001")
                .name("Sarah Johnson")
                .email("sarah.johnson@ctgh.co.za")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(EmployeeRole.ADMIN)
                .hospital(hospital1)
                .isActive(true)
                .shiftSchedule("{\"monday\": \"08:00-17:00\", \"tuesday\": \"08:00-17:00\", \"wednesday\": \"08:00-17:00\", \"thursday\": \"08:00-17:00\", \"friday\": \"08:00-17:00\"}")
                .build();

        Employee hostess = Employee.builder()
                .employeeId("HOST001")
                .name("Maria Gonzalez")
                .email("maria.gonzalez@ctgh.co.za")
                .passwordHash(passwordEncoder.encode("hostess123"))
                .role(EmployeeRole.HOSTESS)
                .hospital(hospital1)
                .isActive(true)
                .shiftSchedule("{\"monday\": \"06:00-14:00\", \"tuesday\": \"06:00-14:00\", \"wednesday\": \"06:00-14:00\", \"thursday\": \"06:00-14:00\", \"friday\": \"06:00-14:00\"}")
                .build();

        Employee nurse = Employee.builder()
                .employeeId("NUR001")
                .name("John Smith")
                .email("john.smith@ctgh.co.za")
                .passwordHash(passwordEncoder.encode("nurse123"))
                .role(EmployeeRole.NURSE)
                .hospital(hospital1)
                .isActive(true)
                .shiftSchedule("{\"monday\": \"07:00-19:00\", \"wednesday\": \"07:00-19:00\", \"friday\": \"07:00-19:00\", \"sunday\": \"07:00-19:00\"}")
                .build();

        Employee supervisor = Employee.builder()
                .employeeId("SUP001")
                .name("Lisa Williams")
                .email("lisa.williams@gsh.co.za")
                .passwordHash(passwordEncoder.encode("supervisor123"))
                .role(EmployeeRole.SUPERVISOR)
                .hospital(hospital2)
                .isActive(true)
                .shiftSchedule("{\"monday\": \"09:00-18:00\", \"tuesday\": \"09:00-18:00\", \"wednesday\": \"09:00-18:00\", \"thursday\": \"09:00-18:00\"}")
                .build();

        employeeRepository.save(admin);
        employeeRepository.save(hostess);
        employeeRepository.save(nurse);
        employeeRepository.save(supervisor);

        logger.info("Sample data initialization completed successfully!");
        logger.info("Created {} hospitals, {} wards, and {} employees",
                hospitalRepository.count(),
                wardRepository.count(),
                employeeRepository.count());
    }
}
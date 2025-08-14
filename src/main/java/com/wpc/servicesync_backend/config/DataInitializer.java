package com.wpc.servicesync_backend.config;

import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.model.entity.Hospital;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import com.wpc.servicesync_backend.repository.HospitalRepository;
import com.wpc.servicesync_backend.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HospitalRepository hospitalRepository;
    private final WardRepository wardRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (hospitalRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Initializing sample data...");

        // Create hospitals
        Hospital hospital1 = Hospital.builder()
                .code("WPC_GH")
                .name("Western Province General Hospital")
                .address("123 Hospital St, Cape Town")
                .contactEmail("admin@wpcgh.co.za")
                .contactPhone("+27 21 123 4567")
                .build();
        hospital1 = hospitalRepository.save(hospital1);

        Hospital hospital2 = Hospital.builder()
                .code("WPC_MH")
                .name("Western Province Maternity Hospital")
                .address("456 Medical Ave, Cape Town")
                .contactEmail("admin@wpcmh.co.za")
                .contactPhone("+27 21 234 5678")
                .build();
        hospital2 = hospitalRepository.save(hospital2);

        // Create wards
        Ward ward1 = Ward.builder()
                .hospital(hospital1)
                .name("3A - General Medicine")
                .floorNumber(3)
                .capacity(25)
                .build();
        wardRepository.save(ward1);

        Ward ward2 = Ward.builder()
                .hospital(hospital1)
                .name("3B - General Medicine")
                .floorNumber(3)
                .capacity(30)
                .build();
        wardRepository.save(ward2);

        Ward ward3 = Ward.builder()
                .hospital(hospital1)
                .name("4A - Surgery")
                .floorNumber(4)
                .capacity(20)
                .build();
        wardRepository.save(ward3);

        // Create employees
        Employee hostess = Employee.builder()
                .employeeId("H001")
                .name("Sarah Johnson")
                .email("sarah.johnson@wpcgh.co.za")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(EmployeeRole.HOSTESS)
                .hospital(hospital1)
                .shiftSchedule("3A,3B,4A") // Simple ward assignments
                .build();
        employeeRepository.save(hostess);

        Employee nurse = Employee.builder()
                .employeeId("N001")
                .name("Mary Williams")
                .email("mary.williams@wpcgh.co.za")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(EmployeeRole.NURSE)
                .hospital(hospital1)
                .shiftSchedule("3A")
                .build();
        employeeRepository.save(nurse);

        Employee supervisor = Employee.builder()
                .employeeId("S001")
                .name("David Smith")
                .email("david.smith@wpcgh.co.za")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(EmployeeRole.SUPERVISOR)
                .hospital(hospital1)
                .shiftSchedule("ALL")
                .build();
        employeeRepository.save(supervisor);

        Employee admin = Employee.builder()
                .employeeId("A001")
                .name("Administrator")
                .email("admin@wpcgh.co.za")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(EmployeeRole.ADMIN)
                .hospital(hospital1)
                .shiftSchedule("ALL")
                .build();
        employeeRepository.save(admin);

        log.info("Sample data initialized successfully!");
        log.info("Test credentials:");
        log.info("Hostess: H001 / password123");
        log.info("Nurse: N001 / password123");
        log.info("Supervisor: S001 / password123");
        log.info("Admin: A001 / admin123");
    }
}
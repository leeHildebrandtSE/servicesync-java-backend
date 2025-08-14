package com.wpc.servicesync_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this annotation
public class ServicesyncBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicesyncBackendApplication.class, args);

        // Using standard Java text blocks (stable since Java 15)
        System.out.println("""
            🏥 ServiceSync API Started Successfully!
            
            Features enabled:
            - Java 21 LTS Runtime
            - Spring Boot 3.2+
            - PostgreSQL Database
            - JWT Authentication
            - WebSocket Real-time Updates
            - OpenAPI Documentation
            - Scheduled Tasks
            
            Documentation: http://localhost:8080/swagger-ui.html
            Health Check: http://localhost:8080/actuator/health
            """);
    }
}
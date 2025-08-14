package com.wpc.servicesync_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceSync API")
                        .version("1.0.0")
                        .description("""
                            🏥 ServiceSync - Digital hospital meal delivery tracking system
                            
                            This API provides endpoints for managing meal delivery services in healthcare facilities,
                            including QR code workflow management, real-time nurse notifications, and comprehensive
                            performance analytics.
                            
                            **Features:**
                            - Employee authentication and authorization
                            - Service session management
                            - Real-time tracking and notifications
                            - Performance analytics and reporting
                            - QR code generation and validation
                            """)
                        .contact(new Contact()
                                .name("ServiceSync Team")
                                .email("support@servicesync.co.za")
                                .url("https://servicesync.co.za"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}
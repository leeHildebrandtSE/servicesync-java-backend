// src/main/java/com/wpc/servicesync_backend/config/JpaConfig.java
package com.wpc.servicesync_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.wpc.servicesync_backend.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
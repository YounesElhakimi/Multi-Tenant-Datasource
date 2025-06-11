package com.izicap.dynamicmultidatabase.service;

import com.izicap.dynamicmultidatabase.config.FlywayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    
    @Autowired
    private FlywayConfig flywayConfig;
    
    /**
     * Onboard a new tenant by setting up their database schema
     */
    public void onboardNewTenant(String tenantId, String jdbcUrl, String username, String password) {
        logger.info("Starting onboarding process for new tenant: {}", tenantId);
        
        try {
            // Validate tenant parameters
            if (tenantId == null || tenantId.trim().isEmpty()) {
                throw new IllegalArgumentException("Tenant ID cannot be null or empty");
            }
            
            if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("JDBC URL cannot be null or empty");
            }
            
            logger.debug("Validating database connection for tenant: {}", tenantId);
            
            // Run Flyway migrations for the new tenant database
            flywayConfig.migrateTenantDatabase(jdbcUrl, username, password, tenantId);
            
            logger.info("Successfully onboarded new tenant: {}", tenantId);
            
        } catch (Exception e) {
            logger.error("Failed to onboard tenant: {}", tenantId, e);
            throw new RuntimeException("Tenant onboarding failed for: " + tenantId, e);
        }
    }
    
    /**
     * Validate tenant database connectivity
     */
    public boolean validateTenantDatabase(String jdbcUrl, String username, String password) {
        logger.debug("Validating database connectivity for URL: {}", jdbcUrl);
        
        try {
            // This will be implemented based on your specific validation requirements
            // For now, we'll rely on Flyway's connection validation
            return true;
        } catch (Exception e) {
            logger.error("Database validation failed for URL: {}", jdbcUrl, e);
            return false;
        }
    }
}
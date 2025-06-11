package com.izicap.dynamicmultidatabase.config;

import com.izicap.dynamicmultidatabase.DBTypeEnum;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FlywayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);
    
    @Value("${app.datasource.main.jdbc-url}")
    private String mainDbUrl;
    
    @Value("${app.datasource.main.username}")
    private String mainDbUsername;
    
    @Value("${app.datasource.main.password}")
    private String mainDbPassword;
    
    @Value("${app.datasource.clienta.jdbc-url}")
    private String clientADbUrl;
    
    @Value("${app.datasource.clienta.username}")
    private String clientADbUsername;
    
    @Value("${app.datasource.clienta.password}")
    private String clientADbPassword;
    
    @Value("${app.datasource.clientb.jdbc-url}")
    private String clientBDbUrl;
    
    @Value("${app.datasource.clientb.username}")
    private String clientBDbUsername;
    
    @Value("${app.datasource.clientb.password}")
    private String clientBDbPassword;
    
    @EventListener(ApplicationReadyEvent.class)
    public void migrateDatabases() {
        logger.info("Starting Flyway migrations for all tenant databases...");
        
        Map<String, DatabaseConfig> databases = new HashMap<>();
        databases.put("MAIN", new DatabaseConfig(mainDbUrl, mainDbUsername, mainDbPassword));
        databases.put("CLIENT_A", new DatabaseConfig(clientADbUrl, clientADbUsername, clientADbPassword));
        databases.put("CLIENT_B", new DatabaseConfig(clientBDbUrl, clientBDbUsername, clientBDbPassword));
        
        for (Map.Entry<String, DatabaseConfig> entry : databases.entrySet()) {
            String dbName = entry.getKey();
            DatabaseConfig config = entry.getValue();
            
            try {
                logger.info("Running Flyway migration for database: {}", dbName);
                
                Flyway flyway = Flyway.configure()
                        .dataSource(config.url, config.username, config.password)
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .validateOnMigrate(true)
                        .cleanDisabled(true)
                        .load();
                
                // Get migration info before running
                var migrationInfos = flyway.info().all();
                logger.debug("Found {} migrations for database {}", migrationInfos.length, dbName);
                
                // Run migrations
                var result = flyway.migrate();
                
                if (result.success) {
                    logger.info("Successfully applied {} migrations to database: {}", 
                              result.migrationsExecuted, dbName);
                    
                    if (result.migrationsExecuted > 0) {
                        logger.info("Migrations applied to {}: {}", dbName, 
                                  result.migrations.stream()
                                          .map(m -> m.version + " - " + m.description)
                                          .toArray());
                    } else {
                        logger.info("Database {} is already up to date", dbName);
                    }
                } else {
                    logger.error("Migration failed for database: {}", dbName);
                }
                
            } catch (Exception e) {
                logger.error("Error running Flyway migration for database: {}", dbName, e);
                throw new RuntimeException("Failed to migrate database: " + dbName, e);
            }
        }
        
        logger.info("Completed Flyway migrations for all tenant databases");
    }
    
    /**
     * Migrate a specific tenant database (useful for new tenant onboarding)
     */
    public void migrateTenantDatabase(String jdbcUrl, String username, String password, String tenantName) {
        logger.info("Running Flyway migration for new tenant: {}", tenantName);
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrl, username, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(true)
                    .cleanDisabled(true)
                    .load();
            
            var result = flyway.migrate();
            
            if (result.success) {
                logger.info("Successfully applied {} migrations to tenant database: {}", 
                          result.migrationsExecuted, tenantName);
            } else {
                logger.error("Migration failed for tenant database: {}", tenantName);
                throw new RuntimeException("Failed to migrate tenant database: " + tenantName);
            }
            
        } catch (Exception e) {
            logger.error("Error running Flyway migration for tenant: {}", tenantName, e);
            throw new RuntimeException("Failed to migrate tenant database: " + tenantName, e);
        }
    }
    
    private static class DatabaseConfig {
        final String url;
        final String username;
        final String password;
        
        DatabaseConfig(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }
}
package com.izicap.dynamicmultidatabase.service;

import com.izicap.dynamicmultidatabase.DBContextHolder;
import com.izicap.dynamicmultidatabase.DBTypeEnum;
import com.izicap.dynamicmultidatabase.PostRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);
    
    @Autowired
    private PostRepository postRepository;
    
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
    
    /**
     * Get overall system health status
     */
    public Map<String, Object> getOverallHealth() {
        logger.debug("Performing comprehensive system health check");
        
        Map<String, Object> overallHealth = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        try {
            // Check database connectivity
            Map<String, Object> dbHealth = getDatabaseHealth();
            boolean dbHealthy = (Boolean) dbHealth.get("allDatabasesHealthy");
            
            // Check migration status
            Map<String, Object> migrationHealth = getMigrationHealth();
            boolean migrationsHealthy = (Boolean) migrationHealth.get("allMigrationsUpToDate");
            
            // Check routing functionality
            Map<String, Object> routingHealth = getRoutingHealth();
            boolean routingHealthy = (Boolean) routingHealth.get("routingHealthy");
            
            // Check system resources
            Map<String, Object> systemHealth = getSystemHealth();
            boolean systemHealthy = (Boolean) systemHealth.get("systemHealthy");
            
            // Aggregate results
            if (!dbHealthy) issues.add("Database connectivity issues");
            if (!migrationsHealthy) issues.add("Migration status issues");
            if (!routingHealthy) issues.add("Database routing issues");
            if (!systemHealthy) issues.add("System resource issues");
            
            boolean overallHealthy = issues.isEmpty();
            
            overallHealth.put("healthy", overallHealthy);
            overallHealth.put("status", overallHealthy ? "UP" : "DOWN");
            overallHealth.put("timestamp", System.currentTimeMillis());
            overallHealth.put("checks", Map.of(
                "database", dbHealthy,
                "migrations", migrationsHealthy,
                "routing", routingHealthy,
                "system", systemHealthy
            ));
            
            if (!issues.isEmpty()) {
                overallHealth.put("issues", issues);
            }
            
            overallHealth.put("details", Map.of(
                "database", dbHealth,
                "migrations", migrationHealth,
                "routing", routingHealth,
                "system", systemHealth
            ));
            
        } catch (Exception e) {
            logger.error("Error during overall health check", e);
            overallHealth.put("healthy", false);
            overallHealth.put("status", "DOWN");
            overallHealth.put("error", e.getMessage());
            overallHealth.put("timestamp", System.currentTimeMillis());
        }
        
        return overallHealth;
    }
    
    /**
     * Check database connectivity for all tenant databases
     */
    public Map<String, Object> getDatabaseHealth() {
        logger.debug("Checking database connectivity for all tenant databases");
        
        Map<String, Object> databaseHealth = new HashMap<>();
        Map<String, Object> databases = new HashMap<>();
        boolean allHealthy = true;
        
        // Define database configurations
        Map<String, DatabaseConfig> dbConfigs = Map.of(
            "MAIN", new DatabaseConfig(mainDbUrl, mainDbUsername, mainDbPassword),
            "CLIENT_A", new DatabaseConfig(clientADbUrl, clientADbUsername, clientADbPassword),
            "CLIENT_B", new DatabaseConfig(clientBDbUrl, clientBDbUsername, clientBDbPassword)
        );
        
        for (Map.Entry<String, DatabaseConfig> entry : dbConfigs.entrySet()) {
            String dbName = entry.getKey();
            DatabaseConfig config = entry.getValue();
            
            Map<String, Object> dbStatus = checkDatabaseConnectivity(dbName, config);
            databases.put(dbName, dbStatus);
            
            if (!(Boolean) dbStatus.get("connected")) {
                allHealthy = false;
            }
        }
        
        databaseHealth.put("allDatabasesHealthy", allHealthy);
        databaseHealth.put("databases", databases);
        databaseHealth.put("timestamp", System.currentTimeMillis());
        
        return databaseHealth;
    }
    
    /**
     * Check migration status for all databases
     */
    public Map<String, Object> getMigrationHealth() {
        logger.debug("Checking migration status for all databases");
        
        Map<String, Object> migrationHealth = new HashMap<>();
        Map<String, Object> databases = new HashMap<>();
        boolean allUpToDate = true;
        
        // Define database configurations
        Map<String, DatabaseConfig> dbConfigs = Map.of(
            "MAIN", new DatabaseConfig(mainDbUrl, mainDbUsername, mainDbPassword),
            "CLIENT_A", new DatabaseConfig(clientADbUrl, clientADbUsername, clientADbPassword),
            "CLIENT_B", new DatabaseConfig(clientBDbUrl, clientBDbUsername, clientBDbPassword)
        );
        
        for (Map.Entry<String, DatabaseConfig> entry : dbConfigs.entrySet()) {
            String dbName = entry.getKey();
            DatabaseConfig config = entry.getValue();
            
            Map<String, Object> migrationStatus = checkMigrationStatus(dbName, config);
            databases.put(dbName, migrationStatus);
            
            if (!(Boolean) migrationStatus.get("upToDate")) {
                allUpToDate = false;
            }
        }
        
        migrationHealth.put("allMigrationsUpToDate", allUpToDate);
        migrationHealth.put("databases", databases);
        migrationHealth.put("timestamp", System.currentTimeMillis());
        
        return migrationHealth;
    }
    
    /**
     * Check database routing functionality
     */
    public Map<String, Object> getRoutingHealth() {
        logger.debug("Testing database routing functionality");
        
        Map<String, Object> routingHealth = new HashMap<>();
        Map<String, Object> routingTests = new HashMap<>();
        boolean routingWorking = true;
        
        // Test routing to each database
        DBTypeEnum[] dbTypes = {DBTypeEnum.MAIN, DBTypeEnum.CLIENT_A, DBTypeEnum.CLIENT_B};
        
        for (DBTypeEnum dbType : dbTypes) {
            Map<String, Object> routingTest = testDatabaseRouting(dbType);
            routingTests.put(dbType.name(), routingTest);
            
            if (!(Boolean) routingTest.get("success")) {
                routingWorking = false;
            }
        }
        
        routingHealth.put("routingHealthy", routingWorking);
        routingHealth.put("routingTests", routingTests);
        routingHealth.put("timestamp", System.currentTimeMillis());
        
        return routingHealth;
    }
    
    /**
     * Check system resources
     */
    public Map<String, Object> getSystemHealth() {
        logger.debug("Checking system resources");
        
        Map<String, Object> systemHealth = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            // Get thread information
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            while (rootGroup.getParent() != null) {
                rootGroup = rootGroup.getParent();
            }
            int activeThreads = rootGroup.activeCount();
            
            // Determine if system is healthy (memory usage < 90%)
            boolean systemHealthy = memoryUsagePercent < 90.0;
            
            systemHealth.put("systemHealthy", systemHealthy);
            systemHealth.put("memory", Map.of(
                "maxMemoryMB", maxMemory / (1024 * 1024),
                "totalMemoryMB", totalMemory / (1024 * 1024),
                "usedMemoryMB", usedMemory / (1024 * 1024),
                "freeMemoryMB", freeMemory / (1024 * 1024),
                "usagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0
            ));
            systemHealth.put("threads", Map.of(
                "activeCount", activeThreads
            ));
            systemHealth.put("jvm", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor")
            ));
            systemHealth.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error checking system health", e);
            systemHealth.put("systemHealthy", false);
            systemHealth.put("error", e.getMessage());
            systemHealth.put("timestamp", System.currentTimeMillis());
        }
        
        return systemHealth;
    }
    
    /**
     * Check connectivity to a specific database
     */
    private Map<String, Object> checkDatabaseConnectivity(String dbName, DatabaseConfig config) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(config.url, config.username, config.password)
                    .load();
            
            // Test connection by getting datasource
            try (Connection connection = flyway.getConfiguration().getDataSource().getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                
                status.put("connected", isValid);
                status.put("url", config.url);
                status.put("connectionValid", isValid);
                
                if (isValid) {
                    status.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                    status.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
                }
                
            }
            
        } catch (Exception e) {
            logger.warn("Database connectivity check failed for {}: {}", dbName, e.getMessage());
            status.put("connected", false);
            status.put("url", config.url);
            status.put("error", e.getMessage());
        }
        
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
    
    /**
     * Check migration status for a specific database
     */
    private Map<String, Object> checkMigrationStatus(String dbName, DatabaseConfig config) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(config.url, config.username, config.password)
                    .locations("classpath:db/migration")
                    .load();
            
            MigrationInfo[] migrations = flyway.info().all();
            MigrationInfo[] pending = flyway.info().pending();
            
            boolean upToDate = pending.length == 0;
            
            status.put("upToDate", upToDate);
            status.put("totalMigrations", migrations.length);
            status.put("pendingMigrations", pending.length);
            
            // Get current version
            MigrationInfo current = flyway.info().current();
            if (current != null) {
                status.put("currentVersion", current.getVersion().toString());
                status.put("currentDescription", current.getDescription());
            }
            
            // List pending migrations if any
            if (pending.length > 0) {
                List<Map<String, String>> pendingList = new ArrayList<>();
                for (MigrationInfo migration : pending) {
                    pendingList.add(Map.of(
                        "version", migration.getVersion().toString(),
                        "description", migration.getDescription(),
                        "state", migration.getState().toString()
                    ));
                }
                status.put("pendingMigrationsList", pendingList);
            }
            
        } catch (Exception e) {
            logger.warn("Migration status check failed for {}: {}", dbName, e.getMessage());
            status.put("upToDate", false);
            status.put("error", e.getMessage());
        }
        
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
    
    /**
     * Test database routing to a specific database
     */
    private Map<String, Object> testDatabaseRouting(DBTypeEnum dbType) {
        Map<String, Object> test = new HashMap<>();
        
        try {
            // Set database context
            DBContextHolder.setCurrentDb(dbType);
            
            // Try to count posts (simple query to test routing)
            long postCount = postRepository.count();
            
            test.put("success", true);
            test.put("database", dbType.name());
            test.put("postCount", postCount);
            test.put("message", "Successfully routed to " + dbType.name() + " database");
            
        } catch (Exception e) {
            logger.warn("Database routing test failed for {}: {}", dbType, e.getMessage());
            test.put("success", false);
            test.put("database", dbType.name());
            test.put("error", e.getMessage());
        } finally {
            // Always clear context
            DBContextHolder.clear();
        }
        
        test.put("timestamp", System.currentTimeMillis());
        return test;
    }
    
    /**
     * Database configuration holder
     */
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
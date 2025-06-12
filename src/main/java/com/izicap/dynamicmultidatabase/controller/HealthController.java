package com.izicap.dynamicmultidatabase.controller;

import com.izicap.dynamicmultidatabase.service.HealthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
@Api(tags = "Health Check", description = "System health monitoring endpoints for multi-database infrastructure")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private HealthService healthService;
    
    @GetMapping
    @ApiOperation(
        value = "Overall system health check",
        notes = "Provides a comprehensive health status of the entire multi-database system including all tenant databases, migrations, and connectivity.",
        response = Map.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "System is healthy"),
        @ApiResponse(code = 503, message = "System has health issues")
    })
    public ResponseEntity<Map<String, Object>> getOverallHealth() {
        logger.info("Performing overall system health check");
        
        try {
            Map<String, Object> healthStatus = healthService.getOverallHealth();
            boolean isHealthy = (Boolean) healthStatus.get("healthy");
            
            if (isHealthy) {
                logger.info("Overall system health check passed");
                return ResponseEntity.ok(healthStatus);
            } else {
                logger.warn("Overall system health check failed");
                return ResponseEntity.status(503).body(healthStatus);
            }
            
        } catch (Exception e) {
            logger.error("Error during overall health check", e);
            return ResponseEntity.status(503).body(Map.of(
                "healthy", false,
                "error", "Health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/databases")
    @ApiOperation(
        value = "Database connectivity health check",
        notes = "Checks the connectivity status of all configured tenant databases and provides detailed connection information.",
        response = Map.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All databases are accessible"),
        @ApiResponse(code = 503, message = "One or more databases are not accessible")
    })
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        logger.info("Performing database connectivity health check");
        
        try {
            Map<String, Object> databaseHealth = healthService.getDatabaseHealth();
            boolean allHealthy = (Boolean) databaseHealth.get("allDatabasesHealthy");
            
            if (allHealthy) {
                logger.info("All databases are healthy");
                return ResponseEntity.ok(databaseHealth);
            } else {
                logger.warn("Some databases are unhealthy");
                return ResponseEntity.status(503).body(databaseHealth);
            }
            
        } catch (Exception e) {
            logger.error("Error during database health check", e);
            return ResponseEntity.status(503).body(Map.of(
                "allDatabasesHealthy", false,
                "error", "Database health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/migrations")
    @ApiOperation(
        value = "Migration status health check",
        notes = "Checks the Flyway migration status across all tenant databases to ensure schemas are up-to-date and consistent.",
        response = Map.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "All migrations are up-to-date"),
        @ApiResponse(code = 503, message = "Migration issues detected")
    })
    public ResponseEntity<Map<String, Object>> getMigrationHealth() {
        logger.info("Performing migration status health check");
        
        try {
            Map<String, Object> migrationHealth = healthService.getMigrationHealth();
            boolean allUpToDate = (Boolean) migrationHealth.get("allMigrationsUpToDate");
            
            if (allUpToDate) {
                logger.info("All database migrations are up-to-date");
                return ResponseEntity.ok(migrationHealth);
            } else {
                logger.warn("Some database migrations are not up-to-date");
                return ResponseEntity.status(503).body(migrationHealth);
            }
            
        } catch (Exception e) {
            logger.error("Error during migration health check", e);
            return ResponseEntity.status(503).body(Map.of(
                "allMigrationsUpToDate", false,
                "error", "Migration health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/routing")
    @ApiOperation(
        value = "Database routing health check",
        notes = "Tests the database routing mechanism by verifying that each tenant database can be accessed through the routing system.",
        response = Map.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Database routing is working correctly"),
        @ApiResponse(code = 503, message = "Database routing issues detected")
    })
    public ResponseEntity<Map<String, Object>> getRoutingHealth() {
        logger.info("Performing database routing health check");
        
        try {
            Map<String, Object> routingHealth = healthService.getRoutingHealth();
            boolean routingWorking = (Boolean) routingHealth.get("routingHealthy");
            
            if (routingWorking) {
                logger.info("Database routing is working correctly");
                return ResponseEntity.ok(routingHealth);
            } else {
                logger.warn("Database routing issues detected");
                return ResponseEntity.status(503).body(routingHealth);
            }
            
        } catch (Exception e) {
            logger.error("Error during routing health check", e);
            return ResponseEntity.status(503).body(Map.of(
                "routingHealthy", false,
                "error", "Routing health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    @GetMapping("/system")
    @ApiOperation(
        value = "System resources health check",
        notes = "Provides information about system resources including memory usage, thread counts, and JVM statistics.",
        response = Map.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "System resources are healthy"),
        @ApiResponse(code = 503, message = "System resource issues detected")
    })
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        logger.info("Performing system resources health check");
        
        try {
            Map<String, Object> systemHealth = healthService.getSystemHealth();
            boolean systemHealthy = (Boolean) systemHealth.get("systemHealthy");
            
            if (systemHealthy) {
                logger.info("System resources are healthy");
                return ResponseEntity.ok(systemHealth);
            } else {
                logger.warn("System resource issues detected");
                return ResponseEntity.status(503).body(systemHealth);
            }
            
        } catch (Exception e) {
            logger.error("Error during system health check", e);
            return ResponseEntity.status(503).body(Map.of(
                "systemHealthy", false,
                "error", "System health check failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
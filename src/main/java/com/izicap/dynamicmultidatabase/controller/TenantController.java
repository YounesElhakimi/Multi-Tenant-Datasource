package com.izicap.dynamicmultidatabase.controller;

import com.izicap.dynamicmultidatabase.service.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant")
@Api(tags = "Tenant Management", description = "Operations for managing tenant databases and onboarding")
public class TenantController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);
    
    @Autowired
    private TenantService tenantService;
    
    @PostMapping("/onboard")
    @ApiOperation(
        value = "Onboard a new tenant",
        notes = "Creates and migrates database schema for a new tenant. This endpoint sets up the complete database structure using Flyway migrations.",
        response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Tenant successfully onboarded"),
        @ApiResponse(code = 400, message = "Invalid tenant parameters"),
        @ApiResponse(code = 500, message = "Internal server error during onboarding")
    })
    public ResponseEntity<String> onboardTenant(
            @ApiParam(value = "Unique identifier for the new tenant", required = true, example = "client-c")
            @RequestParam String tenantId,
            
            @ApiParam(value = "JDBC URL for the tenant's database", required = true, 
                     example = "jdbc:mysql://localhost:3306/multi_client_c?useSSL=false")
            @RequestParam String jdbcUrl,
            
            @ApiParam(value = "Database username", required = true, example = "root")
            @RequestParam String username,
            
            @ApiParam(value = "Database password", required = true, example = "admin")
            @RequestParam String password) {
        
        logger.info("Received tenant onboarding request for: {}", tenantId);
        
        try {
            tenantService.onboardNewTenant(tenantId, jdbcUrl, username, password);
            
            String successMessage = String.format("Tenant '%s' has been successfully onboarded with database schema migrations applied.", tenantId);
            logger.info("Successfully onboarded tenant: {}", tenantId);
            
            return ResponseEntity.ok(successMessage);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters for tenant onboarding: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid parameters: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Failed to onboard tenant: {}", tenantId, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to onboard tenant: " + e.getMessage());
        }
    }
}
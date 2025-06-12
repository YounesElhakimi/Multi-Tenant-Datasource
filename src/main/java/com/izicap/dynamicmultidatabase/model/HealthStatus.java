package com.izicap.dynamicmultidatabase.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

@ApiModel(description = "Health status response model for system monitoring")
public class HealthStatus {
    
    @ApiModelProperty(value = "Overall health status", example = "true")
    private boolean healthy;
    
    @ApiModelProperty(value = "System status", example = "UP", allowableValues = "UP,DOWN")
    private String status;
    
    @ApiModelProperty(value = "Timestamp of health check", example = "1642248600000")
    private long timestamp;
    
    @ApiModelProperty(value = "Individual component health checks")
    private Map<String, Boolean> checks;
    
    @ApiModelProperty(value = "List of identified issues")
    private List<String> issues;
    
    @ApiModelProperty(value = "Detailed health information for each component")
    private Map<String, Object> details;
    
    public HealthStatus() {
    }
    
    public HealthStatus(boolean healthy, String status, long timestamp) {
        this.healthy = healthy;
        this.status = status;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public boolean isHealthy() {
        return healthy;
    }
    
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Boolean> getChecks() {
        return checks;
    }
    
    public void setChecks(Map<String, Boolean> checks) {
        this.checks = checks;
    }
    
    public List<String> getIssues() {
        return issues;
    }
    
    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    @Override
    public String toString() {
        return "HealthStatus{" +
                "healthy=" + healthy +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                ", checks=" + checks +
                ", issues=" + issues +
                '}';
    }
}
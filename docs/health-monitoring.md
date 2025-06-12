# Health Monitoring Documentation

## Overview

The Dynamic Multi-Database Router application includes comprehensive health monitoring capabilities to ensure system reliability and provide visibility into the multi-tenant database infrastructure. The health monitoring system provides real-time status information about database connectivity, migration status, routing functionality, and system resources.

## Health Check Endpoints

### Base URL
All health check endpoints are available under `/health` path:
```
http://localhost:8080/health
```

### 1. Overall System Health
**Endpoint**: `GET /health`

Provides a comprehensive health status of the entire multi-database system.

**Response Structure**:
```json
{
  "healthy": true,
  "status": "UP",
  "timestamp": 1642248600000,
  "checks": {
    "database": true,
    "migrations": true,
    "routing": true,
    "system": true
  },
  "details": {
    "database": { /* Database health details */ },
    "migrations": { /* Migration status details */ },
    "routing": { /* Routing test results */ },
    "system": { /* System resource information */ }
  }
}
```

**HTTP Status Codes**:
- `200 OK`: System is healthy
- `503 Service Unavailable`: System has health issues

### 2. Database Connectivity Health
**Endpoint**: `GET /health/databases`

Checks connectivity to all configured tenant databases.

**Response Structure**:
```json
{
  "allDatabasesHealthy": true,
  "databases": {
    "MAIN": {
      "connected": true,
      "url": "jdbc:mysql://localhost:3306/multi_main",
      "connectionValid": true,
      "databaseProduct": "MySQL",
      "databaseVersion": "8.0.28",
      "timestamp": 1642248600000
    },
    "CLIENT_A": {
      "connected": true,
      "url": "jdbc:mysql://localhost:3306/multi_client_a",
      "connectionValid": true,
      "databaseProduct": "MySQL",
      "databaseVersion": "8.0.28",
      "timestamp": 1642248600000
    },
    "CLIENT_B": {
      "connected": false,
      "url": "jdbc:mysql://localhost:3306/multi_client_b",
      "error": "Connection refused",
      "timestamp": 1642248600000
    }
  },
  "timestamp": 1642248600000
}
```

### 3. Migration Status Health
**Endpoint**: `GET /health/migrations`

Checks Flyway migration status across all tenant databases.

**Response Structure**:
```json
{
  "allMigrationsUpToDate": true,
  "databases": {
    "MAIN": {
      "upToDate": true,
      "totalMigrations": 2,
      "pendingMigrations": 0,
      "currentVersion": "2",
      "currentDescription": "Add sample data",
      "timestamp": 1642248600000
    },
    "CLIENT_A": {
      "upToDate": false,
      "totalMigrations": 2,
      "pendingMigrations": 1,
      "currentVersion": "1",
      "currentDescription": "Create post table",
      "pendingMigrationsList": [
        {
          "version": "2",
          "description": "Add sample data",
          "state": "PENDING"
        }
      ],
      "timestamp": 1642248600000
    }
  },
  "timestamp": 1642248600000
}
```

### 4. Database Routing Health
**Endpoint**: `GET /health/routing`

Tests the database routing mechanism by verifying access to each tenant database.

**Response Structure**:
```json
{
  "routingHealthy": true,
  "routingTests": {
    "MAIN": {
      "success": true,
      "database": "MAIN",
      "postCount": 3,
      "message": "Successfully routed to MAIN database",
      "timestamp": 1642248600000
    },
    "CLIENT_A": {
      "success": true,
      "database": "CLIENT_A",
      "postCount": 3,
      "message": "Successfully routed to CLIENT_A database",
      "timestamp": 1642248600000
    },
    "CLIENT_B": {
      "success": false,
      "database": "CLIENT_B",
      "error": "Connection refused",
      "timestamp": 1642248600000
    }
  },
  "timestamp": 1642248600000
}
```

### 5. System Resources Health
**Endpoint**: `GET /health/system`

Provides information about system resources and JVM statistics.

**Response Structure**:
```json
{
  "systemHealthy": true,
  "memory": {
    "maxMemoryMB": 1024,
    "totalMemoryMB": 512,
    "usedMemoryMB": 256,
    "freeMemoryMB": 256,
    "usagePercent": 25.0
  },
  "threads": {
    "activeCount": 15
  },
  "jvm": {
    "version": "17.0.1",
    "vendor": "Eclipse Adoptium"
  },
  "timestamp": 1642248600000
}
```

## Health Check Implementation

### Core Components

#### 1. HealthController
- **Location**: `src/main/java/com/izicap/dynamicmultidatabase/controller/HealthController.java`
- **Purpose**: REST endpoints for health monitoring
- **Features**:
  - Comprehensive error handling
  - Detailed logging
  - Swagger documentation
  - Appropriate HTTP status codes

#### 2. HealthService
- **Location**: `src/main/java/com/izicap/dynamicmultidatabase/service/HealthService.java`
- **Purpose**: Business logic for health checks
- **Features**:
  - Database connectivity testing
  - Migration status verification
  - Routing functionality testing
  - System resource monitoring

#### 3. HealthStatus Model
- **Location**: `src/main/java/com/izicap/dynamicmultidatabase/model/HealthStatus.java`
- **Purpose**: Response model for health status
- **Features**:
  - Swagger annotations
  - Comprehensive status information
  - Structured response format

## Monitoring Strategies

### 1. Automated Health Checks

#### Continuous Monitoring
```bash
# Check overall health every 30 seconds
while true; do
  curl -s http://localhost:8080/health | jq '.healthy'
  sleep 30
done
```

#### Database-Specific Monitoring
```bash
# Monitor database connectivity
curl -s http://localhost:8080/health/databases | jq '.allDatabasesHealthy'

# Monitor migration status
curl -s http://localhost:8080/health/migrations | jq '.allMigrationsUpToDate'
```

### 2. Integration with Monitoring Tools

#### Prometheus Integration
The health endpoints can be integrated with Prometheus for metrics collection:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'multi-database-app'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/health'
    scrape_interval: 30s
```

#### Grafana Dashboard
Create dashboards to visualize:
- Database connectivity status
- Migration status across tenants
- System resource utilization
- Routing performance metrics

### 3. Alerting Rules

#### Critical Alerts
- Overall system health failure
- Database connectivity loss
- Migration failures
- High memory usage (>90%)

#### Warning Alerts
- Individual database connectivity issues
- Pending migrations
- Routing test failures
- Elevated memory usage (>75%)

## Health Check Logging

### Log Levels
- **INFO**: Successful health checks
- **WARN**: Individual component failures
- **ERROR**: Critical health check failures
- **DEBUG**: Detailed health check information

### Log Examples

#### Successful Health Check
```
2024-01-15 10:30:15.123 INFO  HealthController - Performing overall system health check
2024-01-15 10:30:15.125 INFO  HealthService - All databases are healthy
2024-01-15 10:30:15.127 INFO  HealthController - Overall system health check passed
```

#### Health Check Failure
```
2024-01-15 10:30:15.123 INFO  HealthController - Performing database connectivity health check
2024-01-15 10:30:15.125 WARN  HealthService - Database connectivity check failed for CLIENT_B: Connection refused
2024-01-15 10:30:15.127 WARN  HealthController - Some databases are unhealthy
```

## Best Practices

### 1. Health Check Frequency
- **Production**: Every 30-60 seconds
- **Development**: Every 10-30 seconds
- **Load Testing**: Reduce frequency to avoid overhead

### 2. Timeout Configuration
- Database connectivity: 5 seconds
- Migration checks: 10 seconds
- Routing tests: 5 seconds per database

### 3. Error Handling
- Graceful degradation on partial failures
- Detailed error messages for troubleshooting
- Appropriate HTTP status codes
- Comprehensive logging

### 4. Performance Considerations
- Lightweight health checks
- Caching for expensive operations
- Asynchronous checks where possible
- Connection pooling for database tests

## Troubleshooting Guide

### Common Issues

#### 1. Database Connectivity Failures
**Symptoms**: `allDatabasesHealthy: false`
**Causes**:
- Database server down
- Network connectivity issues
- Incorrect credentials
- Connection pool exhaustion

**Resolution**:
1. Check database server status
2. Verify network connectivity
3. Validate connection credentials
4. Monitor connection pool metrics

#### 2. Migration Status Issues
**Symptoms**: `allMigrationsUpToDate: false`
**Causes**:
- Failed migrations
- Missing migration files
- Database schema corruption
- Version conflicts

**Resolution**:
1. Check Flyway migration logs
2. Verify migration file integrity
3. Run migration repair if needed
4. Validate database schema

#### 3. Routing Test Failures
**Symptoms**: `routingHealthy: false`
**Causes**:
- Database context issues
- Connection routing problems
- Repository access failures
- Thread-local context corruption

**Resolution**:
1. Check database context management
2. Verify routing configuration
3. Test individual database connections
4. Review thread-local usage

#### 4. System Resource Issues
**Symptoms**: `systemHealthy: false`
**Causes**:
- High memory usage
- Thread pool exhaustion
- JVM performance issues
- Resource leaks

**Resolution**:
1. Monitor memory usage patterns
2. Check for memory leaks
3. Tune JVM parameters
4. Review thread pool configuration

## Security Considerations

### 1. Endpoint Security
- Consider authentication for health endpoints
- Limit access to monitoring systems
- Avoid exposing sensitive information
- Use HTTPS in production

### 2. Information Disclosure
- Sanitize error messages
- Avoid exposing database credentials
- Limit detailed system information
- Use appropriate log levels

### 3. Rate Limiting
- Implement rate limiting for health endpoints
- Prevent abuse of health check endpoints
- Monitor for unusual access patterns
- Set appropriate timeout values

## Integration Examples

### 1. Docker Health Check
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1
```

### 2. Kubernetes Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
  timeoutSeconds: 10
  failureThreshold: 3
```

### 3. Load Balancer Health Check
```nginx
upstream app_servers {
    server app1:8080;
    server app2:8080;
}

location /health {
    proxy_pass http://app_servers;
    proxy_connect_timeout 5s;
    proxy_read_timeout 10s;
}
```

## Conclusion

The health monitoring system provides comprehensive visibility into the Dynamic Multi-Database Router application's operational status. It enables proactive monitoring, quick issue identification, and reliable system operation across multiple tenant databases.

For additional monitoring requirements or custom health checks, the system can be extended by adding new endpoints to the HealthController and implementing corresponding logic in the HealthService.
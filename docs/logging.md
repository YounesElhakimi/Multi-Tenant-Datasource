# Logging System Documentation

## Overview

The Dynamic Multi-Database Router application implements a comprehensive logging system using **SLF4J** with **Logback** as the underlying logging framework. This system provides detailed visibility into database routing operations, request processing, and system behavior across multiple tenant databases.

## Logging Architecture

### Core Components

1. **SLF4J (Simple Logging Facade for Java)** - Provides the logging API
2. **Logback** - The actual logging implementation
3. **Custom Configuration** - Tailored for multi-database operations

### Logger Hierarchy

```
Root Logger (INFO)
├── com.izicap.dynamicmultidatabase (DEBUG)
│   ├── PostController (INFO/DEBUG)
│   ├── DBContextHolder (DEBUG)
│   ├── MultiRoutingDataSource (DEBUG/INFO)
│   └── PersistenceConfiguration (INFO/DEBUG)
├── org.hibernate.SQL (DEBUG)
├── org.hibernate.type.descriptor.sql.BasicBinder (TRACE)
├── com.zaxxer.hikari (INFO)
└── org.springframework (INFO)
```

## Log Files Structure

The application generates multiple log files for different purposes:

### 1. Application Log (`logs/application.log`)
- **Purpose**: General application operations and business logic
- **Content**: Controller requests, service operations, configuration startup
- **Rotation**: Daily, max 10MB per file, 30 days retention, 1GB total cap

### 2. Database Log (`logs/database.log`)
- **Purpose**: Database-specific operations and routing decisions
- **Content**: SQL queries, database context switching, connection routing
- **Rotation**: Daily, max 10MB per file, 30 days retention, 500MB total cap

### 3. Error Log (`logs/error.log`)
- **Purpose**: Error-only logging for quick troubleshooting
- **Content**: Exceptions, stack traces, critical errors
- **Rotation**: Daily, max 10MB per file, 60 days retention, 500MB total cap

## Logging Levels

### Production Levels
- **ERROR**: Critical errors that need immediate attention
- **WARN**: Warning conditions that should be monitored
- **INFO**: General information about application flow
- **DEBUG**: Detailed information for troubleshooting (dev profile only)
- **TRACE**: Very detailed information (specific components only)

### Development Levels
All levels are enabled in development profile for comprehensive debugging.

## Component-Specific Logging

### 1. PostController Logging

```java
// Request logging
logger.info("Received request to fetch posts for client: {}", client);

// Operation tracking
logger.debug("Setting database context to: {}", dbType);
logger.debug("Fetching posts from database: {}", dbType);

// Success/Error logging
logger.info("Successfully retrieved posts for client: {} from database: {}", client, dbType);
logger.error("Error occurred while fetching posts for client: {}", client, e);
```

**What it logs:**
- Incoming HTTP requests with client parameters
- Database context switching operations
- Success/failure of data retrieval operations
- Detailed error information with stack traces

### 2. DBContextHolder Logging

```java
// Context management
logger.debug("Setting database context to: {} for thread: {}", dbType, Thread.currentThread().getName());
logger.debug("Retrieved database context: {} for thread: {}", currentDb, Thread.currentThread().getName());
logger.debug("Cleared database context (was: {}) for thread: {}", previousDb, Thread.currentThread().getName());
```

**What it logs:**
- Thread-specific database context operations
- Context setting, retrieval, and clearing operations
- Thread safety verification information

### 3. MultiRoutingDataSource Logging

```java
// Routing decisions
logger.debug("Determining datasource lookup key: {} for thread: {}", currentDb, Thread.currentThread().getName());
logger.info("Routing to database: {}", currentDb);
logger.warn("No database context found, using default datasource");
```

**What it logs:**
- Database routing decisions
- DataSource lookup key determination
- Default fallback scenarios
- Thread-specific routing information

### 4. PersistenceConfiguration Logging

```java
// Configuration startup
logger.info("Configuring main datasource");
logger.info("Multi-routing datasource configured with {} target datasources", targetDataSources.size());
logger.info("Multi-entity manager configured for packages: {}", PACKAGE_SCAN);
```

**What it logs:**
- DataSource configuration during startup
- Entity manager and transaction manager setup
- Hibernate configuration details

## Database Operation Logging

### SQL Query Logging
```properties
# Hibernate SQL logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Example Output:**
```
2024-01-15 10:30:15.125 [http-nio-8080-exec-1] DEBUG org.hibernate.SQL - select post0_.id as id1_0_, post0_.name as name2_0_ from post post0_
2024-01-15 10:30:15.126 [http-nio-8080-exec-1] TRACE org.hibernate.type.descriptor.sql.BasicBinder - binding parameter [1] as [BIGINT] - [1]
```

### Connection Pool Logging
```properties
# HikariCP logging
logging.level.com.zaxxer.hikari=INFO
```

**What it logs:**
- Connection pool statistics
- Connection acquisition/release
- Pool configuration details

## Log Format

### Standard Format
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

**Components:**
- `%d{yyyy-MM-dd HH:mm:ss.SSS}` - Timestamp with milliseconds
- `[%thread]` - Thread name (important for multi-threading)
- `%-5level` - Log level (ERROR, WARN, INFO, DEBUG, TRACE)
- `%logger{36}` - Logger name (truncated to 36 characters)
- `%msg` - The actual log message
- `%n` - Line separator

### Example Log Entry
```
2024-01-15 10:30:15.123 [http-nio-8080-exec-1] INFO  PostController - Received request to fetch posts for client: client-a
```

## Configuration Files

### 1. Logback Configuration (`logback-spring.xml`)

Key features:
- **Multiple Appenders**: Console, File, Database, Error
- **Rolling Policies**: Time and size-based rotation
- **Profile-specific**: Different configurations for dev/prod
- **Filters**: Error-only filtering for error log

### 2. Application Properties (`application.properties`)

```properties
# Component-specific logging levels
logging.level.com.izicap.dynamicmultidatabase=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.zaxxer.hikari=INFO
```

## Monitoring and Troubleshooting

### Common Log Patterns

#### 1. Successful Request Flow
```
INFO  PostController - Received request to fetch posts for client: client-a
DEBUG DBContextHolder - Setting database context to: CLIENT_A for thread: http-nio-8080-exec-1
INFO  MultiRoutingDataSource - Routing to database: CLIENT_A
DEBUG org.hibernate.SQL - select post0_.id as id1_0_, post0_.name as name2_0_ from post post0_
INFO  PostController - Successfully retrieved posts for client: client-a from database: CLIENT_A
DEBUG DBContextHolder - Cleared database context (was: CLIENT_A) for thread: http-nio-8080-exec-1
```

#### 2. Error Scenario
```
INFO  PostController - Received request to fetch posts for client: client-a
DEBUG DBContextHolder - Setting database context to: CLIENT_A for thread: http-nio-8080-exec-1
ERROR PostController - Error occurred while fetching posts for client: client-a
java.sql.SQLException: Connection refused
    at com.mysql.cj.jdbc.ConnectionImpl.connectOneTryOnly(ConnectionImpl.java:956)
    ...
```

#### 3. Database Context Issues
```
WARN  MultiRoutingDataSource - No database context found, using default datasource
INFO  MultiRoutingDataSource - Routing to database: MAIN
```

### Troubleshooting Guide

#### Problem: No logs appearing
**Solution**: Check log file permissions and disk space

#### Problem: Too many DEBUG logs in production
**Solution**: Set appropriate profile (`spring.profiles.active=prod`)

#### Problem: Database routing not working
**Check logs for**:
- DBContextHolder context setting/clearing
- MultiRoutingDataSource routing decisions
- Thread-specific context information

#### Problem: SQL queries not logged
**Solution**: Verify Hibernate logging levels:
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Performance Considerations

### Log Level Impact
- **TRACE**: Highest overhead, use sparingly
- **DEBUG**: Moderate overhead, development only
- **INFO**: Low overhead, suitable for production
- **WARN/ERROR**: Minimal overhead

### File I/O Optimization
- **Asynchronous Logging**: Consider for high-throughput scenarios
- **Buffer Size**: Configured for optimal performance
- **Rotation Policies**: Prevent disk space issues

### Thread Safety
- All loggers are thread-safe
- ThreadLocal context ensures proper isolation
- No synchronization overhead in logging calls

## Best Practices

### 1. Structured Logging
```java
// Good: Structured with parameters
logger.info("User {} performed action {} on resource {}", userId, action, resourceId);

// Avoid: String concatenation
logger.info("User " + userId + " performed action " + action);
```

### 2. Appropriate Log Levels
```java
// ERROR: System errors, exceptions
logger.error("Failed to connect to database", exception);

// WARN: Recoverable issues
logger.warn("Using fallback configuration due to missing property");

// INFO: Business logic flow
logger.info("Processing order {} for customer {}", orderId, customerId);

// DEBUG: Detailed troubleshooting info
logger.debug("Calculated tax amount: {} for order: {}", taxAmount, orderId);
```

### 3. Exception Logging
```java
// Include exception for stack trace
logger.error("Error processing request", exception);

// Don't log and rethrow without adding value
try {
    // operation
} catch (Exception e) {
    logger.error("Operation failed", e);
    throw e; // Only if adding value
}
```

### 4. Performance Considerations
```java
// Use parameterized messages
logger.debug("Processing {} items with filter {}", count, filter);

// Avoid expensive operations in log statements
if (logger.isDebugEnabled()) {
    logger.debug("Complex calculation result: {}", expensiveCalculation());
}
```

## Integration with Monitoring Tools

### Log Aggregation
The structured log format is compatible with:
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Splunk**
- **Fluentd**
- **Grafana Loki**

### Metrics Integration
Logs can be used to generate metrics for:
- Request rates per tenant
- Database routing patterns
- Error rates by database
- Performance metrics

### Alerting
Set up alerts based on:
- Error log entries
- Database connection failures
- Unusual routing patterns
- Performance degradation

## Conclusion

The logging system provides comprehensive visibility into the Dynamic Multi-Database Router application's operations. It enables effective monitoring, troubleshooting, and performance analysis across multiple tenant databases while maintaining thread safety and optimal performance.

For additional configuration or custom logging requirements, refer to the [Logback documentation](http://logback.qos.ch/documentation.html) and [SLF4J manual](http://www.slf4j.org/manual.html).
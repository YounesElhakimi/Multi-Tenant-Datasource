# Flyway Database Migration Documentation

## Overview

The Dynamic Multi-Database Router application uses **Flyway** for database schema management across all tenant databases. This ensures consistent and up-to-date schemas for all tenants, whether they're existing databases or newly onboarded tenants.

## Architecture

### Core Components

1. **FlywayConfig** - Manages migrations for all tenant databases
2. **TenantService** - Handles new tenant onboarding with schema setup
3. **TenantController** - REST API for tenant management operations
4. **Migration Scripts** - SQL files defining database schema changes

### Migration Strategy

The application implements a **multi-tenant migration strategy** where:
- All tenant databases share the same schema structure
- Migrations are applied to each tenant database independently
- New tenants receive the complete schema during onboarding
- Existing tenants get incremental updates

## Migration Files

Migration files are located in `src/main/resources/db/migration/` and follow Flyway's naming convention:

### V1__Create_post_table.sql
```sql
-- Create post table with proper indexing and constraints
CREATE TABLE IF NOT EXISTS post (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_post_name ON post(name);
CREATE INDEX IF NOT EXISTS idx_post_created_at ON post(created_at);
```

### V2__Add_sample_data.sql
```sql
-- Sample data for testing multi-database routing
INSERT IGNORE INTO post (id, name) VALUES 
(1, 'Welcome to Multi-Database System'),
(2, 'Database Routing Example'),
(3, 'Flyway Migration Success');
```

## Configuration

### Application Properties
```properties
# Flyway Configuration
spring.flyway.enabled=false  # Disabled to use custom configuration
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true

# JPA Configuration - DDL disabled since using Flyway
spring.jpa.hibernate.ddl-auto=none
```

### Build Configuration (build.gradle)
```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
}

// Flyway plugin for manual migrations
flyway {
    url = 'jdbc:mysql://localhost:3306/multi_main?useSSL=false'
    user = 'root'
    password = 'admin'
    locations = ['classpath:db/migration']
    baselineOnMigrate = true
}
```

## Automatic Migration Process

### Application Startup
When the application starts, the `FlywayConfig` automatically:

1. **Identifies All Tenant Databases**
   ```java
   Map<String, DatabaseConfig> databases = new HashMap<>();
   databases.put("MAIN", new DatabaseConfig(mainDbUrl, mainDbUsername, mainDbPassword));
   databases.put("CLIENT_A", new DatabaseConfig(clientADbUrl, clientADbUsername, clientADbPassword));
   databases.put("CLIENT_B", new DatabaseConfig(clientBDbUrl, clientBDbUsername, clientBDbPassword));
   ```

2. **Runs Migrations for Each Database**
   ```java
   Flyway flyway = Flyway.configure()
           .dataSource(config.url, config.username, config.password)
           .locations("classpath:db/migration")
           .baselineOnMigrate(true)
           .validateOnMigrate(true)
           .cleanDisabled(true)
           .load();
   
   var result = flyway.migrate();
   ```

3. **Logs Migration Results**
   ```
   2024-01-15 10:30:15.123 INFO FlywayConfig - Running Flyway migration for database: MAIN
   2024-01-15 10:30:15.456 INFO FlywayConfig - Successfully applied 2 migrations to database: MAIN
   2024-01-15 10:30:15.789 INFO FlywayConfig - Database CLIENT_A is already up to date
   ```

## New Tenant Onboarding

### REST API Endpoint
```http
POST /tenant/onboard
```

**Parameters:**
- `tenantId`: Unique identifier for the new tenant
- `jdbcUrl`: JDBC URL for the tenant's database
- `username`: Database username
- `password`: Database password

### Example Request
```bash
curl -X POST "http://localhost:8080/tenant/onboard" \
  -d "tenantId=client-c" \
  -d "jdbcUrl=jdbc:mysql://localhost:3306/multi_client_c?useSSL=false" \
  -d "username=root" \
  -d "password=admin"
```

### Onboarding Process
1. **Parameter Validation**
   ```java
   if (tenantId == null || tenantId.trim().isEmpty()) {
       throw new IllegalArgumentException("Tenant ID cannot be null or empty");
   }
   ```

2. **Database Migration**
   ```java
   flywayConfig.migrateTenantDatabase(jdbcUrl, username, password, tenantId);
   ```

3. **Success Response**
   ```json
   {
     "message": "Tenant 'client-c' has been successfully onboarded with database schema migrations applied."
   }
   ```

## Migration Logging

### Successful Migration
```
2024-01-15 10:30:15.123 INFO FlywayConfig - Starting Flyway migrations for all tenant databases...
2024-01-15 10:30:15.124 INFO FlywayConfig - Running Flyway migration for database: MAIN
2024-01-15 10:30:15.125 DEBUG FlywayConfig - Found 2 migrations for database MAIN
2024-01-15 10:30:15.200 INFO FlywayConfig - Successfully applied 2 migrations to database: MAIN
2024-01-15 10:30:15.201 INFO FlywayConfig - Migrations applied to MAIN: [1 - Create post table, 2 - Add sample data]
```

### Up-to-Date Database
```
2024-01-15 10:30:15.300 INFO FlywayConfig - Running Flyway migration for database: CLIENT_A
2024-01-15 10:30:15.350 INFO FlywayConfig - Database CLIENT_A is already up to date
```

### Migration Error
```
2024-01-15 10:30:15.400 ERROR FlywayConfig - Error running Flyway migration for database: CLIENT_B
java.sql.SQLException: Connection refused
    at com.mysql.cj.jdbc.ConnectionImpl.connectOneTryOnly(ConnectionImpl.java:956)
```

## Manual Migration Commands

### Using Gradle Plugin
```bash
# Migrate main database
./gradlew flywayMigrate

# Get migration info
./gradlew flywayInfo

# Validate migrations
./gradlew flywayValidate

# Clean database (disabled in production)
./gradlew flywayClean
```

### Using Flyway CLI
```bash
# Migrate specific database
flyway -url=jdbc:mysql://localhost:3306/multi_client_a -user=root -password=admin migrate

# Get migration status
flyway -url=jdbc:mysql://localhost:3306/multi_client_a -user=root -password=admin info
```

## Best Practices

### 1. Migration File Naming
```
V1__Create_post_table.sql          # Initial schema
V2__Add_sample_data.sql            # Sample data
V3__Add_user_table.sql             # New feature
V4__Update_post_add_status.sql     # Schema modification
```

### 2. Safe Migration Patterns
```sql
-- Use IF NOT EXISTS for table creation
CREATE TABLE IF NOT EXISTS new_table (...);

-- Use IF EXISTS for dropping
DROP TABLE IF EXISTS old_table;

-- Add columns safely
ALTER TABLE post ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'active';

-- Create indexes safely
CREATE INDEX IF NOT EXISTS idx_post_status ON post(status);
```

### 3. Data Migration Safety
```sql
-- Use INSERT IGNORE for sample data
INSERT IGNORE INTO post (id, name) VALUES (1, 'Sample Post');

-- Use UPDATE with WHERE conditions
UPDATE post SET status = 'active' WHERE status IS NULL;

-- Backup critical data before major changes
CREATE TABLE post_backup AS SELECT * FROM post;
```

### 4. Rollback Strategies
```sql
-- V5__Add_category_column.sql
ALTER TABLE post ADD COLUMN category VARCHAR(100);

-- U5__Remove_category_column.sql (undo migration)
ALTER TABLE post DROP COLUMN category;
```

## Monitoring and Troubleshooting

### Migration Status Check
```java
// Check migration status programmatically
Flyway flyway = Flyway.configure()
    .dataSource(jdbcUrl, username, password)
    .load();

MigrationInfo[] migrations = flyway.info().all();
for (MigrationInfo migration : migrations) {
    logger.info("Migration: {} - Status: {}", 
                migration.getVersion(), 
                migration.getState());
}
```

### Common Issues

#### 1. Migration Checksum Mismatch
**Problem**: Migration file was modified after being applied
**Solution**: 
```bash
flyway repair  # Repairs checksum mismatches
```

#### 2. Failed Migration
**Problem**: Migration failed partway through
**Solution**:
```bash
flyway repair   # Mark failed migration as resolved
flyway migrate  # Re-run migrations
```

#### 3. Database Connection Issues
**Problem**: Cannot connect to tenant database
**Solution**: Check database connectivity and credentials

### Health Check Endpoint
Consider adding a health check endpoint to monitor migration status:

```java
@GetMapping("/health/migrations")
public ResponseEntity<Map<String, Object>> getMigrationHealth() {
    // Implementation to check migration status across all tenant databases
}
```

## Security Considerations

### 1. Database Credentials
- Store credentials securely (environment variables, vault)
- Use least-privilege database users for migrations
- Rotate credentials regularly

### 2. Migration File Security
- Review all migration files before deployment
- Use version control for migration tracking
- Implement approval process for schema changes

### 3. Production Safety
```properties
# Production settings
spring.flyway.clean-disabled=true      # Prevent accidental data loss
spring.flyway.validate-on-migrate=true # Ensure migration integrity
```

## Performance Considerations

### 1. Large Data Migrations
```sql
-- Process in batches for large datasets
UPDATE post SET status = 'active' 
WHERE status IS NULL 
LIMIT 1000;
```

### 2. Index Creation
```sql
-- Create indexes concurrently (MySQL 8.0+)
CREATE INDEX CONCURRENTLY idx_post_category ON post(category);
```

### 3. Migration Timing
- Run migrations during maintenance windows
- Monitor migration execution time
- Test migrations on staging environments first

## Integration with CI/CD

### Pipeline Integration
```yaml
# Example GitHub Actions workflow
- name: Run Database Migrations
  run: |
    ./gradlew flywayMigrate -Pflyway.url=${{ secrets.DB_URL }}
```

### Environment-Specific Migrations
```properties
# application-dev.properties
spring.flyway.locations=classpath:db/migration,classpath:db/dev

# application-prod.properties  
spring.flyway.locations=classpath:db/migration
```

## Conclusion

Flyway integration provides robust database schema management for the multi-tenant application, ensuring:

- **Consistency**: All tenant databases maintain identical schemas
- **Reliability**: Automated migrations reduce human error
- **Scalability**: Easy onboarding of new tenants
- **Traceability**: Complete migration history and versioning
- **Safety**: Built-in validation and rollback capabilities

This approach enables confident database evolution while maintaining the integrity of the multi-tenant architecture.
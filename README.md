# Dynamic Multi-Database Router

This project is a Spring Boot application demonstrating how to dynamically switch between multiple databases at runtime based on the context of an incoming HTTP request. It's a foundational example of a multi-tenant architecture where each tenant has its own dedicated database, with **Flyway integration** for consistent schema management across all tenant databases.

## 🚀 Overview

The core idea is to route database operations to a specific tenant's database. This is achieved by identifying the tenant from the request (in this case, via a URL query parameter) and using Spring's AbstractRoutingDataSource to select the appropriate DataSource for the transaction.

## ✨ How It Works

The request-to-database routing follows these steps:

1. **Request Interception**: The PostController receives an incoming HTTP request.

2. **Tenant Identification**: The controller inspects the client query parameter from the URL (`/test?client=client-a`).

3. **Context Setting**: Based on the tenant identifier, DBContextHolder sets the corresponding database type (DBTypeEnum) in a ThreadLocal variable. This ensures the context is bound to the current request thread and doesn't interfere with other concurrent requests.

4. **DataSource Routing**: The MultiRoutingDataSource, which extends AbstractRoutingDataSource, calls its determineCurrentLookupKey() method. This method retrieves the database type from DBContextHolder.

5. **Connection Selection**: The MultiRoutingDataSource uses the key to look up the actual DataSource from a map of configured data sources that was initialized at startup.

6. **Database Operation**: Spring Data JPA proceeds with the database operation using the selected data source.

7. **Context Clearing**: (Implicit) Once the request is complete, the ThreadLocal is cleared to prevent memory leaks and state pollution for the next request that uses the same thread.

## 📂 Key Components

### Core Application Components
- **DynamicMultiDatabaseApplication.java**: The main entry point for the Spring Boot application.
- **PostController.java**: A REST controller that exposes endpoints to test the dynamic routing. It sets the database context based on a request parameter.
- **PersistenceConfiguration.java**: The central configuration class. It defines the DataSource beans for each database (main, clienta, clientb) and configures the MultiRoutingDataSource to manage them. It also sets up the JPA EntityManagerFactory and TransactionManager.
- **MultiRoutingDataSource.java**: The core of the routing mechanism. It extends AbstractRoutingDataSource and implements determineCurrentLookupKey() to decide which database to use.
- **DBContextHolder.java**: A utility class that uses a ThreadLocal to hold the database context for the current request thread.
- **DBTypeEnum.java**: An enumeration (MAIN, CLIENT_A, CLIENT_B) that defines the unique keys for each database.
- **Post.java**: A simple JPA entity.
- **PostRepository.java**: A Spring Data JPA repository for the Post entity.

### Schema Management Components
- **FlywayConfig.java**: Manages database migrations across all tenant databases automatically on startup.
- **TenantService.java**: Handles new tenant onboarding with automatic schema setup.
- **TenantController.java**: REST API endpoints for tenant management operations.

### Configuration Components
- **SwaggerConfig.java**: Configures Swagger/OpenAPI documentation for the REST APIs.

## 🛠️ Setup and Configuration

### Prerequisites
- Java 17 or newer
- Gradle
- A running MySQL instance

### Database Setup
You need to create three separate databases in your MySQL server:
- `multi_main`
- `multi_client_a`
- `multi_client_b`

### Application Properties
Configure the database connections in `src/main/resources/application.properties`. Update the username and password to match your MySQL setup.

```properties
# Main Database
app.datasource.main.jdbc-url=jdbc:mysql://localhost:3306/multi_main?useSSL=false
app.datasource.main.username=root
app.datasource.main.password=admin

# Client A Database
app.datasource.clienta.jdbc-url=jdbc:mysql://localhost:3306/multi_client_a?useSSL=false
app.datasource.clienta.username=root
app.datasource.clienta.password=admin

# Client B Database
app.datasource.clientb.jdbc-url=jdbc:mysql://localhost:3306/multi_client_b?useSSL=false
app.datasource.clientb.username=root
app.datasource.clientb.password=admin
```

## ⚙️ How to Run

1. Clone the repository.

2. Build the project using Gradle:
   ```bash
   ./gradlew clean build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

4. The application will start on http://localhost:8080.

5. **Automatic Schema Setup**: On startup, Flyway will automatically create and migrate all tenant database schemas.

## 🌐 API Endpoints

You can use curl or your web browser to interact with the API endpoints. The application also provides **Swagger UI** at http://localhost:8080/swagger-ui/ for interactive API documentation.

### 1. Fetch Data from a Specific Database
This endpoint fetches all Post records. Use the client query parameter to specify which database to connect to.

**URL**: `GET /test`  
**Query Parameter**: `client` (values: `client-a`, `client-b`; defaults to `main`)

**Examples:**
```bash
# Fetch from the Main DB (default)
curl http://localhost:8080/test
# Response: [{"id":1,"name":"Welcome to Multi-Database System"}, ...]

# Fetch from Client A's DB
curl http://localhost:8080/test?client=client-a
# Response: [{"id":1,"name":"Welcome to Multi-Database System"}, ...]

# Fetch from Client B's DB
curl http://localhost:8080/test?client=client-b
# Response: [{"id":1,"name":"Welcome to Multi-Database System"}, ...]
```

### 2. Initialize Data (Legacy)
This endpoint will insert one record into each of the three databases.

**URL**: `GET /init-data`

```bash
curl http://localhost:8080/init-data
# Expected Response: Success!
```

### 3. Onboard New Tenant
This endpoint allows you to onboard a new tenant with automatic database schema setup.

**URL**: `POST /tenant/onboard`

**Parameters:**
- `tenantId`: Unique identifier for the new tenant
- `jdbcUrl`: JDBC URL for the tenant's database
- `username`: Database username  
- `password`: Database password

**Example:**
```bash
curl -X POST "http://localhost:8080/tenant/onboard" \
  -d "tenantId=client-c" \
  -d "jdbcUrl=jdbc:mysql://localhost:3306/multi_client_c?useSSL=false" \
  -d "username=root" \
  -d "password=admin"
```

## 🗄️ Database Schema Management

The application uses **Flyway** for database schema management, ensuring all tenant databases have consistent and up-to-date schemas.

### Automatic Migrations
- **On Startup**: All existing tenant databases are automatically migrated to the latest schema version.
- **New Tenant Onboarding**: New tenants receive the complete, up-to-date schema automatically.

### Migration Files
Migration files are located in `src/main/resources/db/migration/`:
- `V1__Create_post_table.sql`: Creates the post table with proper indexing
- `V2__Add_sample_data.sql`: Adds sample data for testing

### Manual Migration Commands
```bash
# Migrate main database
./gradlew flywayMigrate

# Get migration info
./gradlew flywayInfo

# Validate migrations
./gradlew flywayValidate
```

## 📊 Monitoring and Logging

The application includes comprehensive logging for:
- Database routing decisions
- Migration execution
- Tenant onboarding processes
- Request processing across different databases

Log files are organized by purpose:
- `logs/application.log`: General application operations
- `logs/database.log`: Database-specific operations and routing
- `logs/error.log`: Error-only logging for troubleshooting

## 📚 Documentation

Additional documentation is available in the `docs/` folder:
- `docs/logging.md`: Comprehensive logging system documentation
- `docs/flyway-migration.md`: Database migration and schema management guide

## 🔧 API Documentation

Interactive API documentation is available via Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui/
- **API Docs**: http://localhost:8080/v2/api-docs

## 🏗️ Architecture Benefits

- **Multi-Tenant Support**: Each tenant has its own dedicated database
- **Dynamic Routing**: Runtime database selection based on request context
- **Schema Consistency**: Flyway ensures all tenant databases have identical, up-to-date schemas
- **Easy Onboarding**: New tenants can be onboarded with a single API call
- **Comprehensive Monitoring**: Detailed logging and API documentation
- **Production Ready**: Includes proper error handling, validation, and security considerations
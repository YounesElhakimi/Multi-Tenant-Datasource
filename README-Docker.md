# Docker Setup for Dynamic Multi-Database Router

This document provides comprehensive instructions for running the Dynamic Multi-Database Router application using Docker and Docker Compose.

## 🐳 Docker Architecture

The Docker setup includes:

### Services
- **mysql-main**: Primary MySQL instance hosting `multi_main` and `multi_client_b` databases
- **mysql-client-a**: Secondary MySQL instance hosting `multi_client_a` database  
- **app**: Spring Boot application container
- **phpmyadmin**: Web-based MySQL administration tool (optional)

### Network Configuration
- **Ports**:
  - `3306`: MySQL main database
  - `3307`: MySQL client-a database
  - `8080`: Spring Boot application
  - `8081`: phpMyAdmin interface
- **Internal Network**: All services communicate via `multi-db-network`

## 🚀 Quick Start

### Prerequisites
- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB available RAM
- Ports 3306, 3307, 8080, 8081 available

### 1. Start the Environment
```bash
# Using the setup script (recommended)
./scripts/docker-setup.sh start

# Or using Docker Compose directly
docker-compose up -d
```

### 2. Verify Setup
```bash
# Check service status
./scripts/docker-setup.sh status

# Check application health
curl http://localhost:8080/health
```

### 3. Access the Application
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/
- **Health Dashboard**: http://localhost:8080/health
- **phpMyAdmin**: http://localhost:8081

## 🛠️ Management Commands

### Using the Setup Script
```bash
# Start all services
./scripts/docker-setup.sh start

# Stop all services
./scripts/docker-setup.sh stop

# Restart services
./scripts/docker-setup.sh restart

# View service status and health
./scripts/docker-setup.sh status

# View logs
./scripts/docker-setup.sh logs          # All services
./scripts/docker-setup.sh logs app     # Application only

# Connect to database
./scripts/docker-setup.sh db main      # Main database
./scripts/docker-setup.sh db client-a  # Client A database
./scripts/docker-setup.sh db client-b  # Client B database

# Clean up everything
./scripts/docker-setup.sh cleanup
```

### Using Docker Compose Directly
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f app

# Execute commands in containers
docker-compose exec app bash
docker-compose exec mysql-main mysql -u root -padmin
```

## 🗄️ Database Configuration

### Database Layout
```
mysql-main (port 3306):
├── multi_main (main tenant)
└── multi_client_b (client B tenant)

mysql-client-a (port 3307):
└── multi_client_a (client A tenant)
```

### Connection Details
```bash
# Main Database
Host: localhost:3306
Database: multi_main
Username: root
Password: admin

# Client A Database  
Host: localhost:3307
Database: multi_client_a
Username: root
Password: admin

# Client B Database
Host: localhost:3306
Database: multi_client_b
Username: root
Password: admin
```

### Database Access
```bash
# Connect to main database
docker-compose exec mysql-main mysql -u root -padmin multi_main

# Connect to client-a database
docker-compose exec mysql-client-a mysql -u root -padmin multi_client_a

# Connect to client-b database
docker-compose exec mysql-main mysql -u root -padmin multi_client_b
```

## 🔧 Configuration

### Environment Variables
The application supports environment-based configuration:

```bash
# Database URLs
APP_DATASOURCE_MAIN_JDBC_URL=jdbc:mysql://mysql-main:3306/multi_main?useSSL=false
APP_DATASOURCE_CLIENTA_JDBC_URL=jdbc:mysql://mysql-client-a:3306/multi_client_a?useSSL=false
APP_DATASOURCE_CLIENTB_JDBC_URL=jdbc:mysql://mysql-main:3306/multi_client_b?useSSL=false

# Credentials
APP_DATASOURCE_MAIN_USERNAME=root
APP_DATASOURCE_MAIN_PASSWORD=admin

# Logging levels
LOGGING_LEVEL_COM_IZICAP_DYNAMICMULTIDATABASE=INFO
LOGGING_LEVEL_ORG_HIBERNATE_SQL=INFO
```

### Development Override
For development, use `docker-compose.override.yml`:
```bash
# Start with development configuration
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

This enables:
- Debug logging levels
- Remote debugging on port 5005
- Volume mounts for hot reload
- MySQL query logging

## 🧪 Testing the Setup

### 1. Health Checks
```bash
# Overall system health
curl http://localhost:8080/health

# Database connectivity
curl http://localhost:8080/health/databases

# Migration status
curl http://localhost:8080/health/migrations

# Database routing
curl http://localhost:8080/health/routing
```

### 2. API Testing
```bash
# Test main database
curl "http://localhost:8080/test"

# Test client-a database
curl "http://localhost:8080/test?client=client-a"

# Test client-b database  
curl "http://localhost:8080/test?client=client-b"

# Initialize sample data
curl "http://localhost:8080/init-data"
```

### 3. Database Verification
```bash
# Check data in main database
docker-compose exec mysql-main mysql -u root -padmin -e "SELECT * FROM multi_main.post;"

# Check data in client-a database
docker-compose exec mysql-client-a mysql -u root -padmin -e "SELECT * FROM multi_client_a.post;"

# Check data in client-b database
docker-compose exec mysql-main mysql -u root -padmin -e "SELECT * FROM multi_client_b.post;"
```

## 📊 Monitoring

### Container Health
```bash
# Check container status
docker-compose ps

# View container health
docker inspect multi-db-app --format='{{.State.Health.Status}}'
```

### Application Metrics
```bash
# Memory usage
curl -s http://localhost:8080/health/system | jq '.memory'

# Database status
curl -s http://localhost:8080/health/databases | jq '.databases'

# Migration status
curl -s http://localhost:8080/health/migrations | jq '.databases'
```

### Log Monitoring
```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f mysql-main mysql-client-a

# Real-time log monitoring
docker-compose logs -f --tail=100
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Port Conflicts
**Error**: `Port 3306 is already in use`
**Solution**: 
```bash
# Stop local MySQL
sudo systemctl stop mysql

# Or change ports in docker-compose.yml
ports:
  - "3308:3306"  # Use different host port
```

#### 2. Database Connection Failures
**Error**: `Connection refused`
**Solution**:
```bash
# Check database health
./scripts/docker-setup.sh status

# Restart databases
docker-compose restart mysql-main mysql-client-a

# Check logs
docker-compose logs mysql-main
```

#### 3. Application Startup Issues
**Error**: `Application failed to start`
**Solution**:
```bash
# Check application logs
docker-compose logs app

# Verify database connectivity
curl http://localhost:8080/health/databases

# Restart application
docker-compose restart app
```

#### 4. Migration Failures
**Error**: `Migration failed`
**Solution**:
```bash
# Check migration status
curl http://localhost:8080/health/migrations

# Run migrations manually
./scripts/docker-setup.sh migrate

# Check database schema
./scripts/docker-setup.sh db main
```

### Debug Mode
Enable debug mode for detailed troubleshooting:
```bash
# Start with debug configuration
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d

# Connect remote debugger to port 5005
# View detailed logs
docker-compose logs -f app
```

## 🔒 Security Considerations

### Production Deployment
For production use:

1. **Change Default Passwords**:
```yaml
environment:
  MYSQL_ROOT_PASSWORD: your-secure-password
  APP_DATASOURCE_MAIN_PASSWORD: your-app-password
```

2. **Use Secrets Management**:
```yaml
secrets:
  db_password:
    file: ./secrets/db_password.txt
```

3. **Network Security**:
```yaml
networks:
  multi-db-network:
    driver: bridge
    internal: true  # Disable external access
```

4. **Remove Development Tools**:
```yaml
# Remove phpMyAdmin in production
# Remove debug ports
# Use production logging levels
```

## 📈 Performance Tuning

### Database Optimization
```yaml
mysql-main:
  environment:
    MYSQL_INNODB_BUFFER_POOL_SIZE: 256M
    MYSQL_MAX_CONNECTIONS: 100
  deploy:
    resources:
      limits:
        memory: 512M
```

### Application Optimization
```yaml
app:
  environment:
    JAVA_OPTS: "-Xms512m -Xmx1g -XX:+UseG1GC"
    SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: 10
  deploy:
    resources:
      limits:
        memory: 1G
```

## 🚀 Deployment

### Docker Swarm
```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml multi-db-stack
```

### Kubernetes
Convert using Kompose:
```bash
# Install kompose
curl -L https://github.com/kubernetes/kompose/releases/download/v1.26.1/kompose-linux-amd64 -o kompose

# Convert to Kubernetes manifests
kompose convert -f docker-compose.yml
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Health Check Best Practices](./docs/health-monitoring.md)

## 🆘 Support

For issues and questions:
1. Check the troubleshooting section above
2. Review application logs: `./scripts/docker-setup.sh logs`
3. Verify health status: `./scripts/docker-setup.sh status`
4. Check database connectivity: `curl http://localhost:8080/health/databases`
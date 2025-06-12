#!/bin/bash

# Docker setup script for Dynamic Multi-Database Router
# This script helps set up and manage the Docker environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    if ! command -v docker-compose > /dev/null 2>&1; then
        print_error "Docker Compose is not installed. Please install Docker Compose and try again."
        exit 1
    fi
    print_success "Docker Compose is available"
}

# Function to build and start services
start_services() {
    print_status "Building and starting services..."
    
    # Build the application image
    print_status "Building application image..."
    docker-compose build app
    
    # Start all services
    print_status "Starting all services..."
    docker-compose up -d
    
    print_success "Services started successfully"
}

# Function to wait for services to be healthy
wait_for_services() {
    print_status "Waiting for services to be healthy..."
    
    # Wait for databases
    print_status "Waiting for MySQL databases..."
    timeout 120 bash -c 'until docker-compose exec mysql-main mysqladmin ping -h localhost -u root -padmin --silent; do sleep 2; done'
    timeout 120 bash -c 'until docker-compose exec mysql-client-a mysqladmin ping -h localhost -u root -padmin --silent; do sleep 2; done'
    
    print_success "Databases are ready"
    
    # Wait for application
    print_status "Waiting for application..."
    timeout 180 bash -c 'until curl -f http://localhost:8080/health > /dev/null 2>&1; do sleep 5; done'
    
    print_success "Application is ready"
}

# Function to show service status
show_status() {
    print_status "Service status:"
    docker-compose ps
    
    echo ""
    print_status "Health checks:"
    
    # Check database health
    if curl -s http://localhost:8080/health/databases | jq -r '.allDatabasesHealthy' | grep -q true; then
        print_success "All databases are healthy"
    else
        print_warning "Some databases may have issues"
    fi
    
    # Check overall health
    if curl -s http://localhost:8080/health | jq -r '.healthy' | grep -q true; then
        print_success "Application is healthy"
    else
        print_warning "Application may have issues"
    fi
}

# Function to show logs
show_logs() {
    local service=${1:-}
    if [ -n "$service" ]; then
        print_status "Showing logs for $service..."
        docker-compose logs -f "$service"
    else
        print_status "Showing logs for all services..."
        docker-compose logs -f
    fi
}

# Function to stop services
stop_services() {
    print_status "Stopping services..."
    docker-compose down
    print_success "Services stopped"
}

# Function to clean up everything
cleanup() {
    print_status "Cleaning up Docker resources..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    print_success "Cleanup completed"
}

# Function to run database migrations manually
run_migrations() {
    print_status "Running database migrations..."
    docker-compose exec app ./gradlew flywayMigrate
    print_success "Migrations completed"
}

# Function to access database shell
database_shell() {
    local db=${1:-main}
    case $db in
        main)
            print_status "Connecting to main database..."
            docker-compose exec mysql-main mysql -u root -padmin multi_main
            ;;
        client-a)
            print_status "Connecting to client-a database..."
            docker-compose exec mysql-client-a mysql -u root -padmin multi_client_a
            ;;
        client-b)
            print_status "Connecting to client-b database..."
            docker-compose exec mysql-main mysql -u root -padmin multi_client_b
            ;;
        *)
            print_error "Unknown database: $db. Use 'main', 'client-a', or 'client-b'"
            exit 1
            ;;
    esac
}

# Function to show help
show_help() {
    echo "Docker setup script for Dynamic Multi-Database Router"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start       Build and start all services"
    echo "  stop        Stop all services"
    echo "  restart     Restart all services"
    echo "  status      Show service status and health"
    echo "  logs [svc]  Show logs (optionally for specific service)"
    echo "  cleanup     Stop services and clean up Docker resources"
    echo "  migrate     Run database migrations"
    echo "  db [name]   Connect to database shell (main|client-a|client-b)"
    echo "  help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start                 # Start all services"
    echo "  $0 logs app             # Show application logs"
    echo "  $0 db main              # Connect to main database"
    echo "  $0 status               # Check service health"
}

# Main script logic
case "${1:-}" in
    start)
        check_docker
        check_docker_compose
        start_services
        wait_for_services
        show_status
        echo ""
        print_success "Setup completed! Application is available at:"
        echo "  - Application: http://localhost:8080"
        echo "  - Swagger UI: http://localhost:8080/swagger-ui/"
        echo "  - phpMyAdmin: http://localhost:8081"
        echo "  - Health Check: http://localhost:8080/health"
        ;;
    stop)
        stop_services
        ;;
    restart)
        stop_services
        start_services
        wait_for_services
        show_status
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "${2:-}"
        ;;
    cleanup)
        cleanup
        ;;
    migrate)
        run_migrations
        ;;
    db)
        database_shell "${2:-main}"
        ;;
    help|--help|-h)
        show_help
        ;;
    "")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
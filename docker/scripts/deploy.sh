#!/bin/bash

set -e

echo "🏥 ServiceSync Production Deployment Script"
echo "==========================================="

# Configuration
PROJECT_NAME="servicesync"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    if [ ! -f "$ENV_FILE" ]; then
        log_error "Environment file $ENV_FILE not found"
        log_info "Please copy .env.prod.template to $ENV_FILE and configure it"
        exit 1
    fi

    log_info "Prerequisites check passed"
}

# Build application
build_application() {
    log_info "Building Spring Boot application..."
    ./mvnw clean package -DskipTests -Pprod
    log_info "Application built successfully"
}

# Deploy application
deploy() {
    log_info "Deploying ServiceSync to production..."

    # Create necessary directories
    mkdir -p docker/nginx/ssl
    mkdir -p logs
    mkdir -p uploads/diet-sheets

    # Pull latest images
    docker-compose -f $DOCKER_COMPOSE_FILE pull

    # Build and start services
    docker-compose -f $DOCKER_COMPOSE_FILE up -d --build

    log_info "Waiting for services to start..."
    sleep 30

    # Check health
    check_health
}

# Check application health
check_health() {
    log_info "Checking application health..."

    max_attempts=12
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost/health &> /dev/null; then
            log_info "Application is healthy!"
            return 0
        fi

        log_warn "Health check attempt $attempt/$max_attempts failed, retrying in 10 seconds..."
        sleep 10
        ((attempt++))
    done

    log_error "Application health check failed after $max_attempts attempts"
    show_logs
    exit 1
}

# Show application logs
show_logs() {
    log_info "Showing recent application logs..."
    docker-compose -f $DOCKER_COMPOSE_FILE logs --tail=50 app
}

# Stop application
stop() {
    log_info "Stopping ServiceSync..."
    docker-compose -f $DOCKER_COMPOSE_FILE down
    log_info "ServiceSync stopped"
}

# Restart application
restart() {
    log_info "Restarting ServiceSync..."
    stop
    deploy
}

# Backup database
backup() {
    log_info "Creating database backup..."
    docker-compose -f $DOCKER_COMPOSE_FILE exec postgres-backup /backup.sh
    log_info "Database backup completed"
}

# Show status
status() {
    log_info "ServiceSync Status:"
    docker-compose -f $DOCKER_COMPOSE_FILE ps
}

# Update application
update() {
    log_info "Updating ServiceSync..."

    # Git pull (if using git)
    if [ -d ".git" ]; then
        git pull origin main
    fi

    # Rebuild and deploy
    build_application
    deploy
}

# SSL certificate setup (placeholder)
setup_ssl() {
    log_warn "SSL certificate setup"
    log_info "Please ensure you have valid SSL certificates in docker/nginx/ssl/"
    log_info "Required files:"
    log_info "  - servicesync.crt"
    log_info "  - servicesync.key"

    if [ ! -f "docker/nginx/ssl/servicesync.crt" ] || [ ! -f "docker/nginx/ssl/servicesync.key" ]; then
        log_warn "SSL certificates not found, creating self-signed certificates for testing..."
        mkdir -p docker/nginx/ssl
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout docker/nginx/ssl/servicesync.key \
            -out docker/nginx/ssl/servicesync.crt \
            -subj "/C=ZA/ST=Western Cape/L=Cape Town/O=ServiceSync/CN=localhost"
        log_info "Self-signed certificates created (NOT for production use)"
    fi
}

# Main script logic
case "$1" in
    deploy)
        check_prerequisites
        build_application
        setup_ssl
        deploy
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        show_logs
        ;;
    backup)
        backup
        ;;
    update)
        update
        ;;
    health)
        check_health
        ;;
    *)
        echo "Usage: $0 {deploy|stop|restart|status|logs|backup|update|health}"
        echo ""
        echo "Commands:"
        echo "  deploy  - Build and deploy the application"
        echo "  stop    - Stop the application"
        echo "  restart - Restart the application"
        echo "  status  - Show application status"
        echo "  logs    - Show application logs"
        echo "  backup  - Create database backup"
        echo "  update  - Update and redeploy application"
        echo "  health  - Check application health"
        exit 1
        ;;
esac

exit 0
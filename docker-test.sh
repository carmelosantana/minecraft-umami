#!/bin/bash

# Docker test script for Umami Plugin
# Tests the plugin in a Docker container environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
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

# Configuration
PLUGIN_JAR="target/umami-1.0.0.jar"
CONTAINER_NAME="minecraft-umami-test"
TEST_TIMEOUT=60

print_status "Starting Umami Plugin Docker test..."

# Check if plugin JAR exists
if [[ ! -f "$PLUGIN_JAR" ]]; then
    print_error "Plugin JAR not found: $PLUGIN_JAR"
    print_status "Run 'make build' first"
    exit 1
fi

# Stop any existing container
if docker ps -a | grep -q "$CONTAINER_NAME"; then
    print_status "Stopping existing test container..."
    docker stop "$CONTAINER_NAME" >/dev/null 2>&1 || true
    docker rm "$CONTAINER_NAME" >/dev/null 2>&1 || true
fi

# Start the container
print_status "Starting Docker container..."
docker-compose up -d

# Wait for container to be ready
print_status "Waiting for container to start..."
sleep 10

# Get container ID
CONTAINER_ID=$(docker-compose ps -q minecraftbe)
if [[ -z "$CONTAINER_ID" ]]; then
    print_error "Failed to get container ID"
    exit 1
fi

# Copy plugin to container
print_status "Copying plugin to container..."
docker cp "$PLUGIN_JAR" "$CONTAINER_ID:/minecraft/plugins/"

# Restart container to load plugin
print_status "Restarting container to load plugin..."
docker-compose restart

# Wait for server to start
print_status "Waiting for Minecraft server to start..."
sleep 30

# Check if plugin loaded successfully
print_status "Checking if plugin loaded..."
TIMEOUT_COUNT=0
while [[ $TIMEOUT_COUNT -lt $TEST_TIMEOUT ]]; do
    if docker logs "$CONTAINER_ID" 2>&1 | grep -q "Umami.*enabled successfully"; then
        print_success "Plugin loaded successfully!"
        break
    fi
    
    sleep 2
    ((TIMEOUT_COUNT += 2))
    
    if [[ $TIMEOUT_COUNT -ge $TEST_TIMEOUT ]]; then
        print_error "Timeout waiting for plugin to load"
        print_status "Container logs:"
        docker logs "$CONTAINER_ID" 2>&1 | tail -20
        exit 1
    fi
done

# Test plugin commands
print_status "Testing plugin commands..."

# Test version command
docker exec "$CONTAINER_ID" mc-send-to-console "umami version"
sleep 2

# Test status command
docker exec "$CONTAINER_ID" mc-send-to-console "umami status"
sleep 2

# Check logs for command output
print_status "Checking command output..."
if docker logs "$CONTAINER_ID" 2>&1 | grep -q "Umami Plugin Information"; then
    print_success "Version command working"
else
    print_warning "Version command may not be working properly"
fi

# Test configuration
print_status "Testing configuration..."
if docker logs "$CONTAINER_ID" 2>&1 | grep -q "Configuration.*configured"; then
    print_success "Configuration detected"
else
    print_warning "Configuration warnings detected"
fi

# Check for errors
print_status "Checking for errors..."
if docker logs "$CONTAINER_ID" 2>&1 | grep -qi "error\|exception\|failed" | grep -i umami; then
    print_warning "Some errors detected in logs"
    docker logs "$CONTAINER_ID" 2>&1 | grep -i "error\|exception\|failed" | grep -i umami | tail -5
else
    print_success "No critical errors detected"
fi

# Final status
print_status "Getting final plugin status..."
docker exec "$CONTAINER_ID" mc-send-to-console "umami status"
sleep 3

# Show recent logs
print_status "Recent container logs:"
docker logs "$CONTAINER_ID" 2>&1 | tail -15

# Cleanup
print_status "Cleaning up..."
docker-compose down

print_success "Docker test completed!"
print_status "Review the logs above for any issues"

# Summary
print_status "=== Test Summary ==="
if docker logs "$CONTAINER_ID" 2>&1 | grep -q "Umami.*enabled successfully"; then
    print_success "✓ Plugin loads successfully"
else
    print_error "✗ Plugin failed to load"
fi

if docker logs "$CONTAINER_ID" 2>&1 | grep -q "Umami Plugin Information"; then
    print_success "✓ Commands are working"
else
    print_warning "? Commands may have issues"
fi

print_status "Docker test complete. Check output above for details."

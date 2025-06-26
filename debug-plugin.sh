#!/bin/bash

# Debug script for Umami Plugin
# Provides interactive testing and debugging commands

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

# Check if server is running
check_server() {
    if screen -list | grep -q "minecraft"; then
        return 0
    else
        return 1
    fi
}

# Send command to server
send_command() {
    local cmd="$1"
    if check_server; then
        screen -S minecraft -p 0 -X stuff "$cmd$(printf \\r)"
        print_status "Sent command: $cmd"
    else
        print_error "Server is not running. Start with 'make start'"
        return 1
    fi
}

# Show menu
show_menu() {
    echo ""
    echo -e "${BLUE}=== Umami Plugin Debug Menu ===${NC}"
    echo ""
    echo "1. Check plugin status"
    echo "2. Test plugin commands"
    echo "3. Reload configuration"
    echo "4. Send test event"
    echo "5. Show queue statistics"
    echo "6. Monitor logs"
    echo "7. Enable debug logging"
    echo "8. Disable debug logging"
    echo "9. Show online players"
    echo "10. Show recent logs"
    echo "0. Exit"
    echo ""
}

# Main interactive loop
main() {
    print_status "Starting Umami Plugin debug session..."
    
    if ! check_server; then
        print_warning "Minecraft server is not running"
        print_status "Start the server with: make start"
        exit 1
    fi
    
    while true; do
        show_menu
        read -p "Select option (0-10): " choice
        
        case $choice in
            1)
                print_status "Checking plugin status..."
                send_command "umami status"
                sleep 2
                ;;
            2)
                print_status "Testing plugin commands..."
                send_command "umami version"
                sleep 1
                send_command "umami online"
                sleep 1
                send_command "umami help"
                sleep 2
                ;;
            3)
                print_status "Reloading configuration..."
                send_command "umami reload"
                sleep 2
                ;;
            4)
                print_status "Sending test event to Umami..."
                send_command "umami test"
                sleep 3
                ;;
            5)
                print_status "Showing queue statistics..."
                send_command "umami status"
                sleep 2
                ;;
            6)
                print_status "Monitoring logs (Ctrl+C to stop)..."
                tail -f server/logs/latest.log | grep -i umami
                ;;
            7)
                print_status "Enabling debug logging..."
                if [[ -f "src/main/resources/config.yml" ]]; then
                    sed -i.bak 's/debug: false/debug: true/' src/main/resources/config.yml
                    print_success "Debug enabled in config.yml"
                    print_status "Reload plugin to apply: make dev"
                else
                    print_warning "config.yml not found"
                fi
                ;;
            8)
                print_status "Disabling debug logging..."
                if [[ -f "src/main/resources/config.yml" ]]; then
                    sed -i.bak 's/debug: true/debug: false/' src/main/resources/config.yml
                    print_success "Debug disabled in config.yml"
                    print_status "Reload plugin to apply: make dev"
                else
                    print_warning "config.yml not found"
                fi
                ;;
            9)
                print_status "Showing online players..."
                send_command "list"
                sleep 1
                send_command "umami online"
                sleep 2
                ;;
            10)
                print_status "Recent logs:"
                if [[ -f "server/logs/latest.log" ]]; then
                    tail -20 server/logs/latest.log | grep -i umami || print_warning "No Umami logs found"
                else
                    print_warning "Log file not found"
                fi
                ;;
            0)
                print_status "Exiting debug session..."
                break
                ;;
            *)
                print_error "Invalid option. Please select 0-10"
                ;;
        esac
        
        echo ""
        read -p "Press Enter to continue..."
    done
}

# Run main function
main

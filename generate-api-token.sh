#!/bin/bash

# Umami API Token Generator
# Helps generate authentication tokens for self-hosted Umami instances

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
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

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  Umami API Token Generator${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
}

show_help() {
    print_header
    echo "This script helps you generate authentication tokens for self-hosted Umami instances."
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help        Show this help message"
    echo "  -u, --url URL     Umami instance URL (e.g., https://analytics.example.com)"
    echo "  -U, --username    Username for Umami login"
    echo "  -P, --password    Password for Umami login"
    echo "  -i, --interactive Interactive mode (prompts for input)"
    echo ""
    echo "Examples:"
    echo "  $0 -i                                    # Interactive mode"
    echo "  $0 -u https://analytics.example.com -U admin -P password"
    echo ""
    echo "For Umami Cloud users:"
    echo "  Generate API keys directly from your Umami Cloud dashboard:"
    echo "  https://cloud.umami.is/settings/api-keys"
    echo ""
}

# Check if required tools are installed
check_dependencies() {
    if ! command -v curl &> /dev/null; then
        print_error "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed. JSON responses will not be formatted."
        print_info "Install jq for better output formatting: brew install jq (macOS) or apt install jq (Ubuntu)"
        USE_JQ=false
    else
        USE_JQ=true
    fi
}

# Validate URL format
validate_url() {
    local url="$1"
    if [[ ! "$url" =~ ^https?:// ]]; then
        print_error "Invalid URL format. URL must start with http:// or https://"
        return 1
    fi
    return 0
}

# Generate authentication token
generate_token() {
    local umami_url="$1"
    local username="$2"
    local password="$3"
    
    print_info "Attempting to authenticate with Umami..."
    
    # Remove trailing slash from URL
    umami_url="${umami_url%/}"
    
    local login_url="${umami_url}/api/auth/login"
    local payload="{\"username\":\"${username}\",\"password\":\"${password}\"}"
    
    # Make login request
    local response
    local http_code
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -d "$payload" \
        "$login_url" 2>/dev/null) || {
        print_error "Failed to connect to Umami instance at $umami_url"
        print_info "Please check the URL and ensure the Umami instance is accessible"
        return 1
    }
    
    # Extract HTTP status code
    http_code=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    response=$(echo "$response" | sed 's/HTTPSTATUS:[0-9]*$//')
    
    case "$http_code" in
        200)
            print_success "Authentication successful!"
            ;;
        401)
            print_error "Authentication failed. Invalid username or password."
            return 1
            ;;
        404)
            print_error "API endpoint not found. Please check the Umami URL."
            print_info "Make sure you're using a self-hosted Umami instance, not Umami Cloud."
            return 1
            ;;
        *)
            print_error "HTTP Error $http_code"
            if [[ "$USE_JQ" == true ]]; then
                echo "$response" | jq . 2>/dev/null || echo "$response"
            else
                echo "$response"
            fi
            return 1
            ;;
    esac
    
    # Extract token from response
    local token
    if [[ "$USE_JQ" == true ]]; then
        token=$(echo "$response" | jq -r '.token' 2>/dev/null)
    else
        # Fallback parsing without jq
        token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi
    
    if [[ -z "$token" || "$token" == "null" ]]; then
        print_error "Failed to extract token from response"
        echo "Response:"
        if [[ "$USE_JQ" == true ]]; then
            echo "$response" | jq . 2>/dev/null || echo "$response"
        else
            echo "$response"
        fi
        return 1
    fi
    
    print_success "Token generated successfully!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "API Token:"
    echo "$token"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Add this to your Umami plugin configuration:"
    echo ""
    echo "umami:"
    echo "  api:"
    echo "    endpoint: \"${umami_url}/api/send\""
    echo "    api_key: \"${token}\""
    echo ""
    print_info "Save this token securely. You'll need it for API requests."
    
    # Test the token
    print_info "Testing token validity..."
    test_token "$umami_url" "$token"
}

# Test token validity
test_token() {
    local umami_url="$1"
    local token="$2"
    
    local verify_url="${umami_url}/api/auth/verify"
    
    local response
    local http_code
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
        -H "Authorization: Bearer $token" \
        -H "Accept: application/json" \
        "$verify_url" 2>/dev/null) || {
        print_warning "Failed to verify token"
        return 1
    }
    
    http_code=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    response=$(echo "$response" | sed 's/HTTPSTATUS:[0-9]*$//')
    
    if [[ "$http_code" == "200" ]]; then
        print_success "Token is valid!"
        
        if [[ "$USE_JQ" == true ]]; then
            local username role
            username=$(echo "$response" | jq -r '.username' 2>/dev/null)
            role=$(echo "$response" | jq -r '.role' 2>/dev/null)
            
            if [[ "$username" != "null" && "$role" != "null" ]]; then
                print_info "Authenticated as: $username ($role)"
            fi
        fi
    else
        print_warning "Token verification failed (HTTP $http_code)"
        print_info "The token may still work for sending events"
    fi
}

# Interactive mode
interactive_mode() {
    print_header
    echo "This will help you generate an API token for your self-hosted Umami instance."
    echo ""
    print_warning "Note: This is for self-hosted Umami only. For Umami Cloud, generate API keys from your dashboard."
    echo ""
    
    # Get URL
    while true; do
        echo -n "Enter your Umami instance URL (e.g., https://analytics.example.com): "
        read -r UMAMI_URL
        
        if [[ -z "$UMAMI_URL" ]]; then
            print_error "URL cannot be empty"
            continue
        fi
        
        if validate_url "$UMAMI_URL"; then
            break
        fi
    done
    
    # Get username
    while true; do
        echo -n "Enter your Umami username: "
        read -r USERNAME
        
        if [[ -n "$USERNAME" ]]; then
            break
        fi
        print_error "Username cannot be empty"
    done
    
    # Get password
    while true; do
        echo -n "Enter your Umami password: "
        read -rs PASSWORD
        echo ""
        
        if [[ -n "$PASSWORD" ]]; then
            break
        fi
        print_error "Password cannot be empty"
    done
    
    echo ""
    generate_token "$UMAMI_URL" "$USERNAME" "$PASSWORD"
}

# Main function
main() {
    local INTERACTIVE=false
    local UMAMI_URL=""
    local USERNAME=""
    local PASSWORD=""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -i|--interactive)
                INTERACTIVE=true
                shift
                ;;
            -u|--url)
                UMAMI_URL="$2"
                shift 2
                ;;
            -U|--username)
                USERNAME="$2"
                shift 2
                ;;
            -P|--password)
                PASSWORD="$2"
                shift 2
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    check_dependencies
    
    if [[ "$INTERACTIVE" == true ]]; then
        interactive_mode
        return
    fi
    
    # Non-interactive mode
    if [[ -z "$UMAMI_URL" || -z "$USERNAME" || -z "$PASSWORD" ]]; then
        print_error "URL, username, and password are required for non-interactive mode"
        print_info "Use -i for interactive mode or provide all required options"
        show_help
        exit 1
    fi
    
    if ! validate_url "$UMAMI_URL"; then
        exit 1
    fi
    
    generate_token "$UMAMI_URL" "$USERNAME" "$PASSWORD"
}

# Initialize variables
USE_JQ=true

# Run main function
main "$@"

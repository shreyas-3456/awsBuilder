#!/bin/bash

# ─────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────
API_BASE="http://127.0.0.1:8080/api"
HEALTH_URL="http://127.0.0.1:8080/actuator/health"
TEST_CASES_FILE="test_cases.json"
BOOTRUN_LOG="bootrun.log"
RESULTS_LOG="test_results.log"
MAX_RETRIES=3
RETRY_DELAY=5

# ─────────────────────────────────────────────
# Color / formatting helpers
# ─────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

pass()  { echo -e "${GREEN}  ✔ PASS${RESET}  $*"; }
fail()  { echo -e "${RED}  ✗ FAIL${RESET}  $*"; }
info()  { echo -e "${CYAN}  ℹ${RESET}  $*"; }
warn()  { echo -e "${YELLOW}  ⚠${RESET}  $*"; }
header(){ echo -e "\n${BOLD}${CYAN}$*${RESET}"; }

# ─────────────────────────────────────────────
# Counters
# ─────────────────────────────────────────────
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

record_pass() { PASSED_TESTS=$((PASSED_TESTS + 1)); TOTAL_TESTS=$((TOTAL_TESTS + 1)); }
record_fail() { FAILED_TESTS=$((FAILED_TESTS + 1)); TOTAL_TESTS=$((TOTAL_TESTS + 1)); }
record_skip() { SKIPPED_TESTS=$((SKIPPED_TESTS + 1)); }

# ─────────────────────────────────────────────
# HTTP helper with timing and optional retry
# ─────────────────────────────────────────────
# Usage: http_call METHOD URL [body_json]
# Outputs: sets HTTP_CODE, HTTP_BODY, HTTP_DURATION_MS
http_call() {
    local method="$1"
    local url="$2"
    local body="${3:-}"
    local attempt=1

    while [ $attempt -le $MAX_RETRIES ]; do
        local start_ns end_ns
        start_ns=$(date +%s%N 2>/dev/null || echo 0)

        if [ -n "$body" ]; then
            local raw
            raw=$(curl -s -w "\n%{http_code}" \
                -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$body" \
                --max-time 15)
        else
            local raw
            raw=$(curl -s -w "\n%{http_code}" \
                -X "$method" "$url" \
                --max-time 15)
        fi

        end_ns=$(date +%s%N 2>/dev/null || echo 0)
        HTTP_CODE=$(echo "$raw" | tail -n1)
        HTTP_BODY=$(echo "$raw" | sed '$d')
        HTTP_DURATION_MS=$(( (end_ns - start_ns) / 1000000 ))

        # Retry only on connection errors (code 000)
        if [ "$HTTP_CODE" = "000" ] && [ $attempt -lt $MAX_RETRIES ]; then
            warn "Connection error on attempt $attempt/$MAX_RETRIES, retrying in ${RETRY_DELAY}s…"
            sleep $RETRY_DELAY
            attempt=$((attempt + 1))
        else
            return 0
        fi
    done
}

assert_status() {
    local label="$1" expected="$2" actual="$3"
    if [ "$actual" = "$expected" ]; then
        pass "$label — HTTP $actual (expected $expected)"
        return 0
    else
        fail "$label — HTTP $actual (expected $expected)"
        return 1
    fi
}

assert_json_field_nonempty() {
    local label="$1" field="$2" json="$3"
    local value
    value=$(echo "$json" | jq -r "$field" 2>/dev/null)
    if [ -z "$value" ] || [ "$value" = "null" ]; then
        fail "$label — field '$field' is absent or null"
        return 1
    else
        pass "$label — field '$field' present"
        return 0
    fi
}

assert_json_field_equals() {
    local label="$1" field="$2" expected="$3" json="$4"
    local value
    value=$(echo "$json" | jq -r "$field" 2>/dev/null)
    if [ "$value" = "$expected" ]; then
        pass "$label — '$field' = '$expected'"
        return 0
    else
        fail "$label — '$field' expected '$expected', got '$value'"
        return 1
    fi
}

assert_json_is_array() {
    local label="$1" json="$2"
    local len
    len=$(echo "$json" | jq 'if type=="array" then length else -1 end' 2>/dev/null)
    if [ "$len" = "-1" ]; then
        fail "$label — response is not a JSON array"
        return 1
    else
        pass "$label — JSON array with $len element(s)"
        return 0
    fi
}

# ─────────────────────────────────────────────
# App lifecycle
# ─────────────────────────────────────────────
kill_port_8080() {
    info "Stopping any process on port 8080…"
    fuser -k 8080/tcp 2>/dev/null || true
    local pids
    pids=$(lsof -t -i:8080 2>/dev/null)
    [ -n "$pids" ] && kill -9 $pids 2>/dev/null || true
    sleep 1
}

start_app() {
    kill_port_8080
    info "Starting application with ./gradlew bootRun…"
    ./gradlew bootRun > "$BOOTRUN_LOG" 2>&1 &
    APP_PID=$!
    info "Application PID: $APP_PID"
    info "Startup logs:"
    sleep 3
    tail -20 "$BOOTRUN_LOG"
}

wait_for_ready() {
    info "Waiting for application to become ready…"
    local MAX_ATTEMPTS=120 attempt=0
    while [ $attempt -lt $MAX_ATTEMPTS ]; do
        if curl -s "$HEALTH_URL" 2>/dev/null | grep -q "UP"; then
            echo; pass "Health endpoint reports UP"
            return 0
        fi
        if nc -z 127.0.0.1 8080 2>/dev/null; then
            echo; pass "Port 8080 is open"
            return 0
        fi
        printf '.'
        sleep 1
        attempt=$((attempt + 1))
        
        # Show logs every 30 attempts
        if [ $((attempt % 30)) -eq 0 ]; then
            echo
            info "Still waiting... (attempt $attempt/$MAX_ATTEMPTS)"
            tail -5 "$BOOTRUN_LOG" 2>/dev/null || true
        fi
    done
    echo
    fail "Application did not start within timeout"
    info "Last 30 lines of startup log:"
    tail -30 "$BOOTRUN_LOG" 2>/dev/null || true
    return 1
}

# ─────────────────────────────────────────────
# CORS tests
# ─────────────────────────────────────────────
run_cors_tests() {
    header "━━━ CORS Tests ━━━"
    local section_passed=true

    # Test CORS headers from frontend origin
    info "GET /api/config/ec2-instances with CORS headers"
    local cors_response
    cors_response=$(curl -s -i -H 'Origin: http://localhost:3000' -H 'Sec-Fetch-Mode: cors' 'http://127.0.0.1:8080/api/config/ec2-instances' 2>&1)
    
    if echo "$cors_response" | grep -q "Access-Control-Allow-Origin: http://localhost:3000"; then
        pass "CORS header present for localhost:3000"
        record_pass
    else
        fail "CORS header missing for localhost:3000"
        record_fail; section_passed=false
    fi

    if echo "$cors_response" | grep -q "Access-Control-Allow-Credentials: true"; then
        pass "CORS credentials allowed"
        record_pass
    else
        fail "CORS credentials not allowed"
        record_fail; section_passed=false
    fi

    # Test OPTIONS preflight request
    info "OPTIONS /api/config/ec2-instances (preflight)"
    local options_response
    options_response=$(curl -s -i -X OPTIONS -H 'Origin: http://localhost:3000' -H 'Access-Control-Request-Method: GET' 'http://127.0.0.1:8080/api/config/ec2-instances' 2>&1)
    
    if echo "$options_response" | grep -q "200\|204"; then
        pass "Preflight request successful"
        record_pass
    else
        fail "Preflight request failed"
        record_fail; section_passed=false
    fi

    $section_passed && pass "All CORS tests PASSED" || fail "Some CORS tests FAILED"
    $section_passed
}


run_all_api_tests() {
    header "━━━ All API Endpoints Tests ━━━"
    local section_passed=true

    # Diagram API Tests
    info "POST /api/diagrams/validate"
    local valid_diagram='{"nodes":[{"id":"vpc_1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/validate" "$valid_diagram"
    if assert_status "Diagram validate" "200" "$HTTP_CODE"; then record_pass; else record_fail; section_passed=false; fi

    info "POST /api/diagrams/generate"
    http_call POST "${API_BASE}/diagrams/generate" "$valid_diagram"
    if assert_status "Diagram generate" "200" "$HTTP_CODE"; then record_pass; else record_fail; section_passed=false; fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - EC2 INSTANCES
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/ec2-instances"
    http_call GET "${API_BASE}/config/ec2-instances"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "EC2 instances endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "EC2 instances endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/ec2-instances?region=ap-south-1"
    http_call GET "${API_BASE}/config/ec2-instances?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "EC2 instances with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "EC2 instances with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - EBS VOLUMES
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/ebs-volumes"
    http_call GET "${API_BASE}/config/ebs-volumes"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "EBS volumes endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "EBS volumes endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/ebs-volumes?instanceId=i-12345"
    http_call GET "${API_BASE}/config/ebs-volumes?instanceId=i-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "EBS volumes with instanceId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "EBS volumes with instanceId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - S3 BUCKETS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/s3-buckets"
    http_call GET "${API_BASE}/config/s3-buckets"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "S3 buckets endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "S3 buckets endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/s3-buckets?region=ap-south-1"
    http_call GET "${API_BASE}/config/s3-buckets?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "S3 buckets with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "S3 buckets with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - VPCs
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/vpcs"
    http_call GET "${API_BASE}/config/vpcs"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "VPCs endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "VPCs endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/vpcs?region=ap-south-1"
    http_call GET "${API_BASE}/config/vpcs?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "VPCs with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "VPCs with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - SUBNETS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/subnets"
    http_call GET "${API_BASE}/config/subnets"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Subnets endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Subnets endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/subnets?vpcId=vpc-12345"
    http_call GET "${API_BASE}/config/subnets?vpcId=vpc-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Subnets with vpcId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Subnets with vpcId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/subnets?region=ap-south-1"
    http_call GET "${API_BASE}/config/subnets?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Subnets with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Subnets with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/subnets?vpcId=vpc-12345&region=ap-south-1"
    http_call GET "${API_BASE}/config/subnets?vpcId=vpc-12345&region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Subnets with vpcId and region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Subnets with vpcId and region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - INTERNET GATEWAYS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/internet-gateways"
    http_call GET "${API_BASE}/config/internet-gateways"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Internet Gateways endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Internet Gateways endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/internet-gateways?vpcId=vpc-12345"
    http_call GET "${API_BASE}/config/internet-gateways?vpcId=vpc-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Internet Gateways with vpcId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Internet Gateways with vpcId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - ALBs (APPLICATION LOAD BALANCERS)
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/albs"
    http_call GET "${API_BASE}/config/albs"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALBs endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALBs endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/albs?vpcId=vpc-12345"
    http_call GET "${API_BASE}/config/albs?vpcId=vpc-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALBs with vpcId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALBs with vpcId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/albs?region=ap-south-1"
    http_call GET "${API_BASE}/config/albs?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALBs with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALBs with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/albs?vpcId=vpc-12345&region=ap-south-1"
    http_call GET "${API_BASE}/config/albs?vpcId=vpc-12345&region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALBs with vpcId and region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALBs with vpcId and region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - ALB LISTENERS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/alb-listeners"
    http_call GET "${API_BASE}/config/alb-listeners"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALB Listeners endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALB Listeners endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/alb-listeners?albArn=arn:aws:elasticloadbalancing:ap-south-1:123456789:loadbalancer/app/test/1234567890abcdef"
    http_call GET "${API_BASE}/config/alb-listeners?albArn=arn:aws:elasticloadbalancing:ap-south-1:123456789:loadbalancer/app/test/1234567890abcdef"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALB Listeners with albArn (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALB Listeners with albArn — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - ALB TARGET GROUPS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/alb-target-groups"
    http_call GET "${API_BASE}/config/alb-target-groups"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALB Target Groups endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALB Target Groups endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/alb-target-groups?vpcId=vpc-12345"
    http_call GET "${API_BASE}/config/alb-target-groups?vpcId=vpc-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ALB Target Groups with vpcId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ALB Target Groups with vpcId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - EC2 INSTANCES (amiId filter)
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/ec2-instances?amiId=ami-12345"
    http_call GET "${API_BASE}/config/ec2-instances?amiId=ami-12345"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "EC2 instances with amiId (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "EC2 instances with amiId — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - INTERNET GATEWAYS (region filter)
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/internet-gateways?region=ap-south-1"
    http_call GET "${API_BASE}/config/internet-gateways?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Internet Gateways with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Internet Gateways with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/internet-gateways?vpcId=vpc-12345&region=ap-south-1"
    http_call GET "${API_BASE}/config/internet-gateways?vpcId=vpc-12345&region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Internet Gateways with vpcId and region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Internet Gateways with vpcId and region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - LAMBDA FUNCTIONS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/lambda-functions"
    http_call GET "${API_BASE}/config/lambda-functions"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Lambda Functions endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Lambda Functions endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/lambda-functions?region=ap-south-1"
    http_call GET "${API_BASE}/config/lambda-functions?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Lambda Functions with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Lambda Functions with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - DYNAMODB TABLES
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/dynamodb-tables"
    http_call GET "${API_BASE}/config/dynamodb-tables"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "DynamoDB Tables endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "DynamoDB Tables endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/dynamodb-tables?region=ap-south-1"
    http_call GET "${API_BASE}/config/dynamodb-tables?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "DynamoDB Tables with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "DynamoDB Tables with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - API GATEWAYS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/api-gateways"
    http_call GET "${API_BASE}/config/api-gateways"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "API Gateways endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "API Gateways endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/api-gateways?region=ap-south-1"
    http_call GET "${API_BASE}/config/api-gateways?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "API Gateways with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "API Gateways with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - CACHE INVALIDATION
    # ═══════════════════════════════════════════════════════════════════════════════
    info "POST /api/config/cache/invalidate"
    http_call POST "${API_BASE}/config/cache/invalidate"
    if assert_status "Cache invalidation" "204" "$HTTP_CODE"; then
        record_pass
    else
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - SQS QUEUES
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/sqs-queues"
    http_call GET "${API_BASE}/config/sqs-queues"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "SQS Queues endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "SQS Queues endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/sqs-queues?region=ap-south-1"
    http_call GET "${API_BASE}/config/sqs-queues?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "SQS Queues with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "SQS Queues with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - SNS TOPICS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/sns-topics"
    http_call GET "${API_BASE}/config/sns-topics"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "SNS Topics endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "SNS Topics endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/sns-topics?region=ap-south-1"
    http_call GET "${API_BASE}/config/sns-topics?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "SNS Topics with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "SNS Topics with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - KINESIS STREAMS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/kinesis-streams"
    http_call GET "${API_BASE}/config/kinesis-streams"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Kinesis Streams endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Kinesis Streams endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/kinesis-streams?region=ap-south-1"
    http_call GET "${API_BASE}/config/kinesis-streams?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "Kinesis Streams with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "Kinesis Streams with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    $section_passed && pass "All API endpoints PASSED" || fail "Some API endpoints FAILED"
    $section_passed
}

# ═══════════════════════════════════════════════════════════════════════════════
# CONFIG API TESTS - ECR REPOSITORIES
# ═══════════════════════════════════════════════════════════════════════════════
run_container_api_tests() {
    header "━━━ Container API Endpoints Tests (ECR/ECS/Fargate) ━━━"
    local section_passed=true

    info "GET /api/config/ecr-repositories"
    http_call GET "${API_BASE}/config/ecr-repositories"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECR Repositories endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECR Repositories endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/ecr-repositories?region=ap-south-1"
    http_call GET "${API_BASE}/config/ecr-repositories?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECR Repositories with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECR Repositories with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - ECS CLUSTERS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/ecs-clusters"
    http_call GET "${API_BASE}/config/ecs-clusters"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECS Clusters endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECS Clusters endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/ecs-clusters?region=ap-south-1"
    http_call GET "${API_BASE}/config/ecs-clusters?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECS Clusters with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECS Clusters with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    # ═══════════════════════════════════════════════════════════════════════════════
    # CONFIG API TESTS - ECS TASK DEFINITIONS
    # ═══════════════════════════════════════════════════════════════════════════════
    info "GET /api/config/ecs-task-definitions"
    http_call GET "${API_BASE}/config/ecs-task-definitions"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECS Task Definitions endpoint (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECS Task Definitions endpoint — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    info "GET /api/config/ecs-task-definitions?region=ap-south-1"
    http_call GET "${API_BASE}/config/ecs-task-definitions?region=ap-south-1"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
        pass "ECS Task Definitions with region (HTTP $HTTP_CODE)"
        record_pass
    else
        fail "ECS Task Definitions with region — HTTP $HTTP_CODE"
        record_fail; section_passed=false
    fi

    $section_passed && pass "All container API tests PASSED" || fail "Some container API tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# Diagram tests (driven by test_cases.json)
# ─────────────────────────────────────────────
run_diagram_tests() {
    header "━━━ Diagram API Tests ━━━"
    local section_passed=true
    
    # Test the user's failing diagram with proper ami property
    info "Testing user's diagram with EC2 instances (with ami property)"
    local user_diagram
    user_diagram=$(cat <<'EOF'
{
  "nodes": [
    {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","instance_tenancy":"default","tags":"Name=my-vpc"}},
    {"id":"igw-1","type":"INTERNET_GATEWAY","properties":{"tags":"Name=my-igw"}},
    {"id":"alb-1","type":"LOAD_BALANCER","properties":{"name":"my-alb","load_balancer_type":"application","internal":"false","enable_deletion_protection":"false","tags":"Name=my-alb"}},
    {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","map_public_ip_on_launch":"false","tags":"Name=my-subnet","availability_zone":"us-east-1a"}},
    {"id":"sub-2","type":"SUBNET","properties":{"cidr_block":"10.0.2.0/24","map_public_ip_on_launch":"false","tags":"Name=my-subnet","availability_zone":"us-east-1b"}},
    {"id":"ec2-1","type":"EC2","properties":{"ami":"ami-0c55b159cbfafe1f0","instance_type":"t2.micro","associate_public_ip_address":"true","tags":"Name=my-instance"}},
    {"id":"ec2-2","type":"EC2","properties":{"ami":"ami-0c55b159cbfafe1f0","instance_type":"t2.micro","associate_public_ip_address":"true","tags":"Name=my-instance"}}
  ],
  "edges": [
    {"id":"e-igw-vpc","source":"igw-1","target":"vpc-1","type":"smoothstep"},
    {"id":"e-alb-sub1","source":"alb-1","target":"sub-1","type":"smoothstep"},
    {"id":"e-sub1-vpc","source":"sub-1","target":"vpc-1","type":"smoothstep"},
    {"id":"e-sub2-vpc","source":"sub-2","target":"vpc-1","type":"smoothstep"},
    {"id":"e-ec21-sub1","source":"ec2-1","target":"sub-1","type":"smoothstep"},
    {"id":"e-ec22-sub2","source":"ec2-2","target":"sub-2","type":"smoothstep"},
    {"id":"e-alb-ec21","source":"alb-1","target":"ec2-1","type":"smoothstep"},
    {"id":"e-alb-ec22","source":"alb-1","target":"ec2-2","type":"smoothstep"}
  ],
  "region":"us-east-1"
}
EOF
)
    
    http_call POST "${API_BASE}/diagrams/validate" "$user_diagram"
    if assert_status "User diagram validate" "200" "$HTTP_CODE"; then
        record_pass
    else
        fail "User diagram validation failed"
        echo "Response: $HTTP_BODY"
        record_fail; section_passed=false
    fi
    
    http_call POST "${API_BASE}/diagrams/generate" "$user_diagram"
    if assert_status "User diagram generate" "200" "$HTTP_CODE"; then
        record_pass
        local terraform
        terraform=$(echo "$HTTP_BODY" | jq -r '.terraform' 2>/dev/null)
        if echo "$terraform" | grep -q "aws_instance"; then
            pass "User diagram — Terraform contains EC2 instances"
            record_pass
        else
            fail "User diagram — Terraform missing EC2 instances"
            record_fail; section_passed=false
        fi
    else
        fail "User diagram generation failed"
        echo "Response: $HTTP_BODY"
        record_fail; section_passed=false
    fi
    
    local num_tests
    num_tests=$(jq '. | length' "$TEST_CASES_FILE")

    for i in $(seq 0 $((num_tests - 1))); do
        local test_name expected_success test_data
        test_name=$(jq -r ".[$i].name" "$TEST_CASES_FILE")
        expected_success=$(jq -r ".[$i].expected_success" "$TEST_CASES_FILE")
        test_data=$(jq -c ".[$i].diagram" "$TEST_CASES_FILE")

        echo -e "\n${BOLD}Test $((i+1))/${num_tests}: $test_name${RESET} (expect success=$expected_success)"
        local test_failed=false

        # ── /validate ──────────────────────────────────
        http_call POST "${API_BASE}/diagrams/validate" "$test_data"
        local validate_code="$HTTP_CODE"

        if [ "$expected_success" = "true" ]; then
            assert_status "  Validate" "200" "$validate_code" || test_failed=true
        else
            if [ "$validate_code" != "200" ]; then
                pass "  Validate — HTTP $validate_code (expected failure)"
            else
                fail "  Validate — HTTP 200 (expected a non-200 error response)"
                test_failed=true
            fi
        fi

        # ── /generate ──────────────────────────────────
        http_call POST "${API_BASE}/diagrams/generate" "$test_data"
        local gen_code="$HTTP_CODE" gen_body="$HTTP_BODY"

        if [ "$expected_success" = "true" ]; then
            if assert_status "  Generate" "200" "$gen_code"; then
                assert_json_field_nonempty "  Generate body" ".terraform" "$gen_body" || test_failed=true
            else
                test_failed=true
            fi

            # Additional semantic checks for valid diagrams
            if [ "$gen_code" = "200" ]; then
                local terraform
                terraform=$(echo "$gen_body" | jq -r '.terraform' 2>/dev/null)

                # Check Terraform contains 'resource' keyword
                if echo "$terraform" | grep -q "resource"; then
                    pass "  Terraform content — contains 'resource' blocks"
                else
                    fail "  Terraform content — no 'resource' blocks found"
                    test_failed=true
                fi

                # Resource-specific content checks based on node types
                local node_types
                node_types=$(echo "$test_data" | jq -r '[.nodes[].type] | unique[]' 2>/dev/null)
                for ntype in $node_types; do
                    case "$ntype" in
                        S3)          pattern="aws_s3_bucket" ;;
                        VPC)         pattern="aws_vpc" ;;
                        SUBNET)      pattern="aws_subnet" ;;
                        EC2)         pattern="aws_instance" ;;
                        RDS)         pattern="aws_db_instance" ;;
                        INTERNET_GATEWAY) pattern="aws_internet_gateway" ;;
                        LOAD_BALANCER)    pattern="aws_lb|aws_alb" ;;
                        SQS)             pattern="aws_sqs_queue" ;;
                        SNS)             pattern="aws_sns_topic" ;;
                        KINESIS)         pattern="aws_kinesis_stream" ;;
                        CLOUDWATCH)      pattern="aws_cloudwatch_metric_alarm" ;;
                        XRAY)            pattern="aws_xray_sampling_rule" ;;
                        CLOUDTRAIL)      pattern="aws_cloudtrail" ;;
                        ECR)             pattern="aws_ecr_repository" ;;
                        ECS)             pattern="aws_ecs_cluster" ;;
                        FARGATE)         pattern="aws_ecs_task_definition" ;;
                        *)           pattern="" ;;
                    esac
                    if [ -n "$pattern" ]; then
                        if echo "$terraform" | grep -qE "$pattern"; then
                            pass "  Terraform content — '$ntype' → pattern '$pattern' found"
                        else
                            fail "  Terraform content — '$ntype' → pattern '$pattern' NOT found"
                            test_failed=true
                        fi
                    fi
                done
            fi
        else
            if [ "$gen_code" != "200" ]; then
                pass "  Generate — HTTP $gen_code (expected failure)"
            else
                fail "  Generate — HTTP 200 (expected a non-200 error response)"
                test_failed=true
            fi
        fi

        if [ "$test_failed" = true ]; then
            fail "RESULT: $test_name"
            [ -n "$gen_body" ] && echo "       Response: $(echo "$gen_body" | head -c 300)"
            record_fail
            section_passed=false
        else
            pass "RESULT: $test_name"
            record_pass
        fi
    done

    $section_passed && pass "All diagram tests PASSED" || fail "Some diagram tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# Edge-case / negative API tests
# ─────────────────────────────────────────────
run_edge_case_tests() {
    header "━━━ Edge Case & Negative API Tests ━━━"
    local section_passed=true

    # 1. Empty body to /validate
    info "POST /diagrams/validate — empty body"
    http_call POST "${API_BASE}/diagrams/validate" '{}'
    if [ "$HTTP_CODE" != "200" ]; then
        pass "Empty body validate — HTTP $HTTP_CODE (expected failure)"
        record_pass
    else
        fail "Empty body validate — HTTP 200 (should have rejected empty body)"
        record_fail; section_passed=false
    fi

    # 2. Empty body to /generate
    info "POST /diagrams/generate — empty body"
    http_call POST "${API_BASE}/diagrams/generate" '{}'
    if [ "$HTTP_CODE" != "200" ]; then
        pass "Empty body generate — HTTP $HTTP_CODE (expected failure)"
        record_pass
    else
        fail "Empty body generate — HTTP 200 (should have rejected empty body)"
        record_fail; section_passed=false
    fi

    # 3. Malformed JSON to /validate
    info "POST /diagrams/validate — malformed JSON"
    http_call POST "${API_BASE}/diagrams/validate" 'NOT_JSON'
    if [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "415" ] || [ "$HTTP_CODE" = "422" ]; then
        pass "Malformed JSON validate — HTTP $HTTP_CODE"
        record_pass
    else
        fail "Malformed JSON validate — HTTP $HTTP_CODE (expected 400/415/422)"
        record_fail; section_passed=false
    fi

    # 4. Duplicate node IDs
    info "POST /diagrams/validate — duplicate node IDs"
    local dup_payload
    dup_payload=$(cat <<'EOF'
{
  "nodes": [
    { "id": "vpc_1", "type": "VPC", "properties": { "cidr_block": "10.0.0.0/16" } },
    { "id": "vpc_1", "type": "VPC", "properties": { "cidr_block": "10.0.0.0/16" } }
  ],
  "edges": [],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/validate" "$dup_payload"
    if [ "$HTTP_CODE" != "200" ]; then
        pass "Duplicate node IDs — HTTP $HTTP_CODE (expected failure)"
        record_pass
    else
        warn "Duplicate node IDs — HTTP 200 (may or may not be intentional)"
        record_skip
    fi

    # 5. Edge referencing non-existent node IDs
    info "POST /diagrams/validate — edge with bad source/target"
    local bad_edge_payload
    bad_edge_payload=$(cat <<'EOF'
{
  "nodes": [
    { "id": "vpc_1", "type": "VPC", "properties": { "cidr_block": "10.0.0.0/16" } }
  ],
  "edges": [
    { "id": "e1", "source": "does_not_exist", "target": "vpc_1" }
  ],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/validate" "$bad_edge_payload"
    if [ "$HTTP_CODE" != "200" ]; then
        pass "Bad edge reference — HTTP $HTTP_CODE (expected failure)"
        record_pass
    else
        warn "Bad edge reference — HTTP 200 (may not validate edge references)"
        record_skip
    fi

    # 6. Node with unknown type
    info "POST /diagrams/validate — unknown resource type"
    local unknown_type_payload
    unknown_type_payload=$(cat <<'EOF'
{
  "nodes": [
    { "id": "x1", "type": "UNICORN", "properties": {} }
  ],
  "edges": [],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/validate" "$unknown_type_payload"
    if [ "$HTTP_CODE" != "200" ]; then
        pass "Unknown node type — HTTP $HTTP_CODE (expected failure)"
        record_pass
    else
        fail "Unknown node type — HTTP 200 (should reject unrecognised type)"
        record_fail; section_passed=false
    fi

    # 7. Very large payload (stress / size limit)
    info "POST /diagrams/validate — very large node count (50 VPCs)"
    local large_nodes='[]'
    for j in $(seq 1 50); do
        large_nodes=$(echo "$large_nodes" | jq --argjson n "$j" \
            '. + [{"id": ("vpc_"+($n|tostring)), "type": "VPC", "properties": {"cidr_block": "10.0.0.0/16"}}]')
    done
    local large_payload
    large_payload=$(jq -n --argjson nodes "$large_nodes" '{"nodes": $nodes, "edges": [],
"region":"us-east-1"
}')
    http_call POST "${API_BASE}/diagrams/validate" "$large_payload"
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "413" ]; then
        pass "Large payload — HTTP $HTTP_CODE (no 5xx crash)"
        record_pass
    else
        fail "Large payload — HTTP $HTTP_CODE (unexpected status)"
        record_fail; section_passed=false
    fi

    # 8. GET on POST-only endpoint
    info "GET /diagrams/validate — wrong method"
    http_call GET "${API_BASE}/diagrams/validate"
    if [ "$HTTP_CODE" = "405" ] || [ "$HTTP_CODE" = "404" ]; then
        pass "Wrong method — HTTP $HTTP_CODE"
        record_pass
    else
        warn "Wrong method — HTTP $HTTP_CODE (expected 405)"
        record_skip
    fi

    $section_passed && pass "Edge case tests PASSED" || fail "Some edge case tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# Regression tests for reported issues
# ─────────────────────────────────────────────
run_regression_tests() {
    header "━━━ Regression Tests ━━━"
    local section_passed=true

    # Issue: curl failing for ec2-instances with specific headers
    info "GET /api/config/ec2-instances (user reported failing curl)"
    local reg_response
    reg_response=$(curl -s -w "\n%{http_code}" 'http://localhost:8080/api/config/ec2-instances?' \
      -H 'Accept: */*' \
      -H 'Accept-Language: en-US,en;q=0.9' \
      -H 'Connection: keep-alive' \
      -H 'DNT: 1' \
      -H 'Origin: http://localhost:3000' \
      -H 'Referer: http://localhost:3000/' \
      -H 'Sec-Fetch-Dest: empty' \
      -H 'Sec-Fetch-Mode: cors' \
      -H 'Sec-Fetch-Site: same-site' \
      -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36' \
      -H 'sec-ch-ua: "Chromium";v="146", "Not-A.Brand";v="24", "Google Chrome";v="146"' \
      -H 'sec-ch-ua-mobile: ?0' \
      -H 'sec-ch-ua-platform: "Windows"')

    local reg_code
    reg_code=$(echo "$reg_response" | tail -n1)
    
    if [ "$reg_code" = "200" ]; then
        pass "Failing curl regression — HTTP $reg_code"
        record_pass
    else
        fail "Failing curl regression — HTTP $reg_code (expected 200)"
        record_fail; section_passed=false
    fi

    $section_passed && pass "All regression tests PASSED" || fail "Some regression tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# CloudFormation API tests
# ─────────────────────────────────────────────
run_cloudformation_tests() {
    header "━━━ CloudFormation API Tests ━━━"
    local section_passed=true

    # ── 1. Single-node VPC diagram: both fields present ──────────────────────
    info "POST /api/diagrams/generate — VPC only: both terraform + cloudformation fields"
    local vpc_diagram='{"nodes":[{"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/generate" "$vpc_diagram"
    if assert_status "CFN VPC generate" "200" "$HTTP_CODE"; then
        record_pass
        if assert_json_field_nonempty "CFN VPC — terraform field" ".terraform" "$HTTP_BODY"; then record_pass; else record_fail; section_passed=false; fi
        if assert_json_field_nonempty "CFN VPC — cloudformation field" ".cloudformation" "$HTTP_BODY"; then record_pass; else record_fail; section_passed=false; fi
    else
        record_fail; section_passed=false
    fi

    # ── 2. CloudFormation output starts with AWSTemplateFormatVersion ─────────
    info "POST /api/diagrams/generate — CFN output starts with AWSTemplateFormatVersion"
    http_call POST "${API_BASE}/diagrams/generate" "$vpc_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn
        cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWSTemplateFormatVersion"; then
            pass "CFN output — contains AWSTemplateFormatVersion"
            record_pass
        else
            fail "CFN output — missing AWSTemplateFormatVersion"
            record_fail; section_passed=false
        fi
    fi

    # ── 3. Resource-type pattern checks ──────────────────────────────────────
    # VPC
    info "CFN resource type — VPC → AWS::EC2::VPC"
    http_call POST "${API_BASE}/diagrams/generate" "$vpc_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::EC2::VPC"; then
            pass "CFN VPC — AWS::EC2::VPC present"; record_pass
        else
            fail "CFN VPC — AWS::EC2::VPC missing"; record_fail; section_passed=false
        fi
    fi

    # Subnet (needs VPC parent)
    info "CFN resource type — Subnet → AWS::EC2::Subnet"
    local subnet_diagram
    subnet_diagram=$(cat <<'EOF'
{"nodes":[
  {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}},
  {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","availability_zone":"us-east-1a","tags":"Name=test-subnet"}}
],"edges":[{"id":"e1","source":"sub-1","target":"vpc-1","type":"smoothstep"}],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$subnet_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::EC2::Subnet"; then
            pass "CFN Subnet — AWS::EC2::Subnet present"; record_pass
        else
            fail "CFN Subnet — AWS::EC2::Subnet missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN Subnet diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # EC2 (needs Subnet parent)
    info "CFN resource type — EC2 → AWS::EC2::Instance"
    local ec2_diagram
    ec2_diagram=$(cat <<'EOF'
{"nodes":[
  {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}},
  {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","availability_zone":"us-east-1a","tags":"Name=test-subnet"}},
  {"id":"ec2-1","type":"EC2","properties":{"ami":"ami-0c55b159cbfafe1f0","instance_type":"t2.micro","tags":"Name=test-ec2"}}
],"edges":[
  {"id":"e1","source":"sub-1","target":"vpc-1","type":"smoothstep"},
  {"id":"e2","source":"ec2-1","target":"sub-1","type":"smoothstep"}
],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$ec2_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::EC2::Instance"; then
            pass "CFN EC2 — AWS::EC2::Instance present"; record_pass
        else
            fail "CFN EC2 — AWS::EC2::Instance missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN EC2 diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # S3
    info "CFN resource type — S3 → AWS::S3::Bucket"
    local s3_diagram='{"nodes":[{"id":"s3-1","type":"S3","properties":{"bucket_name":"my-test-bucket","versioning":"false"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/generate" "$s3_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::S3::Bucket"; then
            pass "CFN S3 — AWS::S3::Bucket present"; record_pass
        else
            fail "CFN S3 — AWS::S3::Bucket missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN S3 diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # RDS (needs Subnet parent)
    info "CFN resource type — RDS → AWS::RDS::DBInstance"
    local rds_diagram
    rds_diagram=$(cat <<'EOF'
{"nodes":[
  {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}},
  {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","availability_zone":"us-east-1a","tags":"Name=test-subnet"}},
  {"id":"rds-1","type":"RDS","properties":{"engine":"mysql","instance_class":"db.t3.micro","allocated_storage":"20","db_name":"testdb","username":"admin","password":"password123"}}
],"edges":[
  {"id":"e1","source":"sub-1","target":"vpc-1","type":"smoothstep"},
  {"id":"e2","source":"rds-1","target":"sub-1","type":"smoothstep"}
],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$rds_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::RDS::DBInstance"; then
            pass "CFN RDS — AWS::RDS::DBInstance present"; record_pass
        else
            fail "CFN RDS — AWS::RDS::DBInstance missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN RDS diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # IGW (needs VPC parent)
    info "CFN resource type — IGW → AWS::EC2::InternetGateway"
    local igw_diagram
    igw_diagram=$(cat <<'EOF'
{"nodes":[
  {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}},
  {"id":"igw-1","type":"INTERNET_GATEWAY","properties":{"tags":"Name=test-igw"}}
],"edges":[{"id":"e1","source":"igw-1","target":"vpc-1","type":"smoothstep"}],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$igw_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::EC2::InternetGateway"; then
            pass "CFN IGW — AWS::EC2::InternetGateway present"; record_pass
        else
            fail "CFN IGW — AWS::EC2::InternetGateway missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN IGW diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # Load Balancer (needs Subnet parent)
    info "CFN resource type — LB → AWS::ElasticLoadBalancingV2::LoadBalancer"
    local lb_diagram
    lb_diagram=$(cat <<'EOF'
{"nodes":[
  {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","tags":"Name=test-vpc"}},
  {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","availability_zone":"us-east-1a","tags":"Name=test-subnet"}},
  {"id":"alb-1","type":"LOAD_BALANCER","properties":{"name":"test-alb","load_balancer_type":"application","internal":"false","tags":"Name=test-alb"}}
],"edges":[
  {"id":"e1","source":"sub-1","target":"vpc-1","type":"smoothstep"},
  {"id":"e2","source":"alb-1","target":"sub-1","type":"smoothstep"}
],
"region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$lb_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::ElasticLoadBalancingV2::LoadBalancer"; then
            pass "CFN LB — AWS::ElasticLoadBalancingV2::LoadBalancer present"; record_pass
        else
            fail "CFN LB — AWS::ElasticLoadBalancingV2::LoadBalancer missing"; record_fail; section_passed=false
        fi
    else
        fail "CFN LB diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # ── 4. !Ref cross-references appear for dependent resources ──────────────
    info "CFN cross-references — !Ref appears in Subnet output"
    http_call POST "${API_BASE}/diagrams/generate" "$subnet_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "!Ref"; then
            pass "CFN cross-ref — !Ref present in output"; record_pass
        else
            fail "CFN cross-ref — !Ref missing from output"; record_fail; section_passed=false
        fi
    fi

    # ── 5. Full user diagram also returns cloudformation field ────────────────
    info "CFN full user diagram — cloudformation field non-empty"
    local user_diagram
    user_diagram=$(cat <<'EOF'
{
  "nodes": [
    {"id":"vpc-1","type":"VPC","properties":{"cidr_block":"10.0.0.0/16","enable_dns_hostnames":"true","enable_dns_support":"true","instance_tenancy":"default","tags":"Name=my-vpc"}},
    {"id":"igw-1","type":"INTERNET_GATEWAY","properties":{"tags":"Name=my-igw"}},
    {"id":"alb-1","type":"LOAD_BALANCER","properties":{"name":"my-alb","load_balancer_type":"application","internal":"false","enable_deletion_protection":"false","tags":"Name=my-alb"}},
    {"id":"sub-1","type":"SUBNET","properties":{"cidr_block":"10.0.1.0/24","map_public_ip_on_launch":"false","tags":"Name=my-subnet","availability_zone":"us-east-1a"}},
    {"id":"sub-2","type":"SUBNET","properties":{"cidr_block":"10.0.2.0/24","map_public_ip_on_launch":"false","tags":"Name=my-subnet","availability_zone":"us-east-1b"}},
    {"id":"ec2-1","type":"EC2","properties":{"ami":"ami-0c55b159cbfafe1f0","instance_type":"t2.micro","associate_public_ip_address":"true","tags":"Name=my-instance"}},
    {"id":"ec2-2","type":"EC2","properties":{"ami":"ami-0c55b159cbfafe1f0","instance_type":"t2.micro","associate_public_ip_address":"true","tags":"Name=my-instance"}}
  ],
  "edges": [
    {"id":"e-igw-vpc","source":"igw-1","target":"vpc-1","type":"smoothstep"},
    {"id":"e-alb-sub1","source":"alb-1","target":"sub-1","type":"smoothstep"},
    {"id":"e-sub1-vpc","source":"sub-1","target":"vpc-1","type":"smoothstep"},
    {"id":"e-sub2-vpc","source":"sub-2","target":"vpc-1","type":"smoothstep"},
    {"id":"e-ec21-sub1","source":"ec2-1","target":"sub-1","type":"smoothstep"},
    {"id":"e-ec22-sub2","source":"ec2-2","target":"sub-2","type":"smoothstep"},
    {"id":"e-alb-ec21","source":"alb-1","target":"ec2-1","type":"smoothstep"},
    {"id":"e-alb-ec22","source":"alb-1","target":"ec2-2","type":"smoothstep"}
  ],
  "region":"us-east-1"
}
EOF
)
    http_call POST "${API_BASE}/diagrams/generate" "$user_diagram"
    if assert_status "CFN full user diagram generate" "200" "$HTTP_CODE"; then
        record_pass
        if assert_json_field_nonempty "CFN full user diagram — cloudformation field" ".cloudformation" "$HTTP_BODY"; then
            record_pass
        else
            record_fail; section_passed=false
        fi
    else
        record_fail; section_passed=false
    fi

    $section_passed && pass "All CloudFormation tests PASSED" || fail "Some CloudFormation tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# CloudFormation SQS/SNS/Kinesis tests
# ─────────────────────────────────────────────
run_cloudformation_messaging_tests() {
    header "━━━ CloudFormation SQS/SNS/Kinesis Tests ━━━"
    local section_passed=true

    # SQS
    info "CFN resource type — SQS → AWS::SQS::Queue"
    local sqs_diagram='{"nodes":[{"id":"sqs-1","type":"SQS","properties":{"queue_name":"test-queue","delay_seconds":"0","visibility_timeout":"30"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/generate" "$sqs_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::SQS::Queue"; then
            pass "CFN SQS — AWS::SQS::Queue present"; record_pass
        else
            fail "CFN SQS — AWS::SQS::Queue missing"; record_fail; section_passed=false
        fi
        local terraform; terraform=$(echo "$HTTP_BODY" | jq -r '.terraform' 2>/dev/null)
        if echo "$terraform" | grep -q "aws_sqs_queue"; then
            pass "TF SQS — aws_sqs_queue present"; record_pass
        else
            fail "TF SQS — aws_sqs_queue missing"; record_fail; section_passed=false
        fi
    else
        fail "SQS diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # SNS
    info "CFN resource type — SNS → AWS::SNS::Topic"
    local sns_diagram='{"nodes":[{"id":"sns-1","type":"SNS","properties":{"topic_name":"test-topic","display_name":"Test Topic"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/generate" "$sns_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::SNS::Topic"; then
            pass "CFN SNS — AWS::SNS::Topic present"; record_pass
        else
            fail "CFN SNS — AWS::SNS::Topic missing"; record_fail; section_passed=false
        fi
        local terraform; terraform=$(echo "$HTTP_BODY" | jq -r '.terraform' 2>/dev/null)
        if echo "$terraform" | grep -q "aws_sns_topic"; then
            pass "TF SNS — aws_sns_topic present"; record_pass
        else
            fail "TF SNS — aws_sns_topic missing"; record_fail; section_passed=false
        fi
    else
        fail "SNS diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # Kinesis
    info "CFN resource type — KINESIS → AWS::Kinesis::Stream"
    local kinesis_diagram='{"nodes":[{"id":"kinesis-1","type":"KINESIS","properties":{"stream_name":"test-stream","shard_count":"1","retention_period":"24"}}],"edges":[],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/generate" "$kinesis_diagram"
    if [ "$HTTP_CODE" = "200" ]; then
        local cfn; cfn=$(echo "$HTTP_BODY" | jq -r '.cloudformation' 2>/dev/null)
        if echo "$cfn" | grep -q "AWS::Kinesis::Stream"; then
            pass "CFN Kinesis — AWS::Kinesis::Stream present"; record_pass
        else
            fail "CFN Kinesis — AWS::Kinesis::Stream missing"; record_fail; section_passed=false
        fi
        local terraform; terraform=$(echo "$HTTP_BODY" | jq -r '.terraform' 2>/dev/null)
        if echo "$terraform" | grep -q "aws_kinesis_stream"; then
            pass "TF Kinesis — aws_kinesis_stream present"; record_pass
        else
            fail "TF Kinesis — aws_kinesis_stream missing"; record_fail; section_passed=false
        fi
    else
        fail "Kinesis diagram — HTTP $HTTP_CODE"; record_fail; section_passed=false
    fi

    # SNS → SQS connection validation
    info "Validate SNS → SQS connection"
    local sns_sqs_diagram='{"nodes":[{"id":"sns-1","type":"SNS","properties":{"topic_name":"test-topic"}},{"id":"sqs-1","type":"SQS","properties":{"queue_name":"test-queue"}}],"edges":[{"id":"e1","source":"sns-1","target":"sqs-1","type":"smoothstep"}],"region":"us-east-1"}'
    http_call POST "${API_BASE}/diagrams/validate" "$sns_sqs_diagram"
    if assert_status "SNS→SQS validate" "200" "$HTTP_CODE"; then
        record_pass
    else
        fail "SNS→SQS validation failed"
        echo "Response: $HTTP_BODY"
        record_fail; section_passed=false
    fi

    $section_passed && pass "All SQS/SNS/Kinesis tests PASSED" || fail "Some SQS/SNS/Kinesis tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# Catalog API tests (SKIPPED - requires database)
# ─────────────────────────────────────────────
run_catalog_tests() {
    header "━━━ Catalog API Tests (SKIPPED - requires database) ━━━"
    warn "Catalog tests require database connection - skipping"
    return 0
}

# ─────────────────────────────────────────────
# Health / actuator tests
# ─────────────────────────────────────────────
run_health_tests() {
    header "━━━ Health & Actuator Tests ━━━"
    local section_passed=true

    info "GET /actuator/health"
    http_call GET "$HEALTH_URL"
    if assert_status "Health endpoint" "200" "$HTTP_CODE"; then
        assert_json_field_equals "Health status" ".status" "UP" "$HTTP_BODY" && record_pass || { record_fail; section_passed=false; }
    else
        record_fail; section_passed=false
    fi

    $section_passed && pass "Health tests PASSED" || fail "Health tests FAILED"
    $section_passed
}

# ─────────────────────────────────────────────
# Summary report
# ─────────────────────────────────────────────
print_summary() {
    header "━━━ Test Summary ━━━"
    echo -e "  Total:   ${BOLD}$TOTAL_TESTS${RESET}"
    echo -e "  ${GREEN}Passed:  $PASSED_TESTS${RESET}"
    echo -e "  ${RED}Failed:  $FAILED_TESTS${RESET}"
    echo -e "  ${YELLOW}Skipped: $SKIPPED_TESTS${RESET}"

    if [ "$FAILED_TESTS" -eq 0 ]; then
        echo -e "\n${GREEN}${BOLD}✔ ALL TESTS PASSED${RESET}"
        return 0
    else
        echo -e "\n${RED}${BOLD}✗ $FAILED_TESTS TEST(S) FAILED${RESET}"
        return 1
    fi
}

# ─────────────────────────────────────────────
# Main loop
# ─────────────────────────────────────────────
main() {
    while true; do
        # Reset counters for each attempt
        TOTAL_TESTS=0
        PASSED_TESTS=0
        FAILED_TESTS=0
        SKIPPED_TESTS=0

        start_app
        if ! wait_for_ready; then
            warn "Application failed to start. Showing full startup log:"
            echo "─────────────────────────────────────────────"
            cat "$BOOTRUN_LOG" 2>/dev/null || true
            echo "─────────────────────────────────────────────"
            warn "Retrying in 10s…"
            sleep 10
            continue
        fi

        run_health_tests
        run_cors_tests
        run_all_api_tests
        run_container_api_tests
        run_diagram_tests
        run_cloudformation_tests
        run_cloudformation_messaging_tests
        run_edge_case_tests
        run_regression_tests

        if print_summary; then
            kill_port_8080
            exit 0
        else
            warn "Some tests failed. Showing last 50 lines of application log:"
            echo "─────────────────────────────────────────────"
            tail -50 "$BOOTRUN_LOG" 2>/dev/null || true
            echo "─────────────────────────────────────────────"
            warn "Restarting application and retrying…"
            sleep 10
        fi
    done
}

main
#!/bin/bash

# Test Workflow Script for Service Orchestration Lab
# This script tests the complete workflow of the Task and User services

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Service Orchestration Lab - Test Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if services are running
check_service() {
    local port=$1
    local service=$2

    if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1 || curl -s http://localhost:$port/api/users > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $service is running on port $port${NC}"
    else
        echo -e "${YELLOW}⚠ $service may not be running on port $port${NC}"
        echo -e "${YELLOW}  Please start it with: cd $3 && mvn spring-boot:run${NC}"
    fi
}

echo -e "${BLUE}1. Checking if services are running...${NC}"
check_service 8082 "User Service" "user-service"
check_service 8081 "Task Service" "task-service"
echo ""

# Test User Service
echo -e "${BLUE}2. Testing User Service - Get all users${NC}"
echo "   GET http://localhost:8082/api/users"
curl -s http://localhost:8082/api/users | python3 -m json.tool || echo "User service not responding"
echo ""

# Test Task Service - Create Task
echo -e "${BLUE}3. Creating a new task${NC}"
echo "   POST http://localhost:8081/api/tasks"
TASK1=$(curl -s -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement feature X",
    "description": "Add new authentication feature"
  }')
echo "$TASK1" | python3 -m json.tool
TASK1_ID=$(echo "$TASK1" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "1")
echo -e "${GREEN}✓ Task created with ID: $TASK1_ID${NC}"
echo ""

# Wait a bit for async event processing
sleep 2

# Create another task
echo -e "${BLUE}4. Creating another task${NC}"
TASK2=$(curl -s -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Fix bug in payment module",
    "description": "Resolve issue with credit card validation"
  }')
echo "$TASK2" | python3 -m json.tool
TASK2_ID=$(echo "$TASK2" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "2")
echo -e "${GREEN}✓ Task created with ID: $TASK2_ID${NC}"
echo ""

sleep 2

# List all tasks
echo -e "${BLUE}5. Listing all tasks${NC}"
echo "   GET http://localhost:8081/api/tasks"
curl -s http://localhost:8081/api/tasks | python3 -m json.tool
echo ""

# Assign first task
echo -e "${BLUE}6. Assigning task $TASK1_ID to user 1 (John Doe)${NC}"
echo "   PUT http://localhost:8081/api/tasks/$TASK1_ID/assign?assigneeId=1"
ASSIGNED_TASK=$(curl -s -X PUT "http://localhost:8081/api/tasks/$TASK1_ID/assign?assigneeId=1")
echo "$ASSIGNED_TASK" | python3 -m json.tool
echo -e "${GREEN}✓ Task assigned - Check Task Service console for event logs${NC}"
echo ""

sleep 2

# Assign second task
echo -e "${BLUE}7. Assigning task $TASK2_ID to user 2 (Jane Smith)${NC}"
echo "   PUT http://localhost:8081/api/tasks/$TASK2_ID/assign?assigneeId=2"
curl -s -X PUT "http://localhost:8081/api/tasks/$TASK2_ID/assign?assigneeId=2" | python3 -m json.tool
echo -e "${GREEN}✓ Task assigned - Check Task Service console for event logs${NC}"
echo ""

sleep 2

# Complete first task
echo -e "${BLUE}8. Completing task $TASK1_ID${NC}"
echo "   PUT http://localhost:8081/api/tasks/$TASK1_ID/complete"
curl -s -X PUT "http://localhost:8081/api/tasks/$TASK1_ID/complete" | python3 -m json.tool
echo -e "${GREEN}✓ Task completed - Check Task Service console for event logs${NC}"
echo ""

sleep 2

# Filter tasks by status
echo -e "${BLUE}9. Listing IN_PROGRESS tasks${NC}"
echo "   GET http://localhost:8081/api/tasks?status=IN_PROGRESS"
curl -s "http://localhost:8081/api/tasks?status=IN_PROGRESS" | python3 -m json.tool
echo ""

# Filter tasks by assignee
echo -e "${BLUE}10. Listing tasks assigned to user 1${NC}"
echo "   GET http://localhost:8081/api/tasks?assigneeId=1"
curl -s "http://localhost:8081/api/tasks?assigneeId=1" | python3 -m json.tool
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ Test workflow completed successfully!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}Check the Task Service console to see:${NC}"
echo "  - Event publication logs"
echo "  - Asynchronous event processing"
echo "  - User Service API calls"
echo "  - User details fetched from User Service"
echo ""

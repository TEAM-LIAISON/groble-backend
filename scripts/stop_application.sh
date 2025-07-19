#!/bin/bash
echo "Stopping application..."

# ECS 서비스 스케일다운 (옵션)
# aws ecs update-service --cluster groble-cluster --service groble-prod-service --desired-count 0

echo "Application stopped successfully"
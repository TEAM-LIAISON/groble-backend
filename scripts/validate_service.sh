#!/bin/bash
echo "Validating service deployment..."

# ECS 서비스 상태 확인
SERVICE_STATUS=$(aws ecs describe-services \
  --cluster groble-cluster \
  --services groble-prod-service \
  --query 'services[0].status' \
  --output text)

if [ "$SERVICE_STATUS" != "ACTIVE" ]; then
  echo "Service is not ACTIVE. Current status: $SERVICE_STATUS"
  exit 1
fi

# 실행 중인 태스크 수 확인
RUNNING_COUNT=$(aws ecs describe-services \
  --cluster groble-cluster \
  --services groble-prod-service \
  --query 'services[0].runningCount' \
  --output text)

DESIRED_COUNT=$(aws ecs describe-services \
  --cluster groble-cluster \
  --services groble-prod-service \
  --query 'services[0].desiredCount' \
  --output text)

if [ "$RUNNING_COUNT" -ne "$DESIRED_COUNT" ]; then
  echo "Running count ($RUNNING_COUNT) does not match desired count ($DESIRED_COUNT)"
  exit 1
fi

# 로드밸런서 헬스체크 (있는 경우)
LB_DNS=$(aws elbv2 describe-load-balancers \
  --names groble-load-balancer \
  --query 'LoadBalancers[0].DNSName' \
  --output text 2>/dev/null || echo "")

if [ ! -z "$LB_DNS" ]; then
  echo "Testing load balancer health..."
  for i in {1..5}; do
    if curl -f -s "https://$LB_DNS/health" > /dev/null; then
      echo "Load balancer health check passed"
      break
    elif [ $i -eq 5 ]; then
      echo "Load balancer health check failed"
      exit 1
    else
      echo "Retrying health check... ($i/5)"
      sleep 10
    fi
  done
fi

echo "Service validation completed successfully"
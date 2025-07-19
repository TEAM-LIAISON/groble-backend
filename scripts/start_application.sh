#!/bin/bash
echo "Starting application deployment..."

# 태스크 정의 등록
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://final-task-definition.json \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "New task definition registered: $TASK_DEF_ARN"

# ECS 서비스 업데이트
aws ecs update-service \
  --cluster groble-cluster \
  --service groble-prod-service \
  --task-definition $TASK_DEF_ARN \
  --force-new-deployment

echo "ECS service update initiated"

# 배포 완료 대기
aws ecs wait services-stable \
  --cluster groble-cluster \
  --services groble-prod-service

echo "Application started successfully"
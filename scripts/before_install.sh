#!/bin/bash
echo "Preparing for installation..."

# AWS CLI 설정 확인
aws --version

# ECS 클러스터 상태 확인
aws ecs describe-clusters --clusters groble-cluster

echo "Pre-installation checks completed"
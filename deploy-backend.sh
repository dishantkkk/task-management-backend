#!/bin/bash

# === CONFIGURATION ===
DOCKERHUB_USERNAME="dishantkkk"
BACKEND_IMAGE="$DOCKERHUB_USERNAME/task-management:latest"
INFRA_DIR="./infra"
K8S_DIR="./k8s"

# === STEP 1: Deploy Infra Services via Kompose YAMLs ===
echo "📦 Deploying infrastructure services (Redis, Kafka, Zookeeper, Schema Registry)..."
kubectl apply -f $INFRA_DIR/

# === STEP 2: Build & Push Backend Docker Image ===
echo "🔨 Building Spring Boot JAR..."
./mvnw clean package -DskipTests

echo "🐳 Building Docker image for backend..."
docker build -t $BACKEND_IMAGE .

echo "🚀 Pushing backend Docker image to DockerHub..."
docker push $BACKEND_IMAGE

# === STEP 3: Deploy Backend to Kubernetes ===
echo "☸️ Deploying backend to Kubernetes..."
kubectl apply -f $K8S_DIR/

# === STEP 4: Wait for Backend Pod ===
echo "⏳ Waiting for backend pod to be ready..."
kubectl wait --for=condition=ready pod -l app=task-management --timeout=60s

# === STEP 5: Port-Forward Backend Service ===
echo "🔌 Port-forwarding backend service on http://localhost:8080..."
kubectl port-forward service/task-management-service 8080:8080 > backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > backend.pid

# === DONE ===
echo "✅ Backend deployed and accessible at http://localhost:8080"
echo "🛑 Run 'kill \$(cat backend.pid)' to stop port-forward"

#!/bin/bash

# === CONFIGURATION ===
DOCKERHUB_USERNAME="dishantkkk"
BACKEND_IMAGE="$DOCKERHUB_USERNAME/task-management:latest"
INFRA_DIR="./infra"
K8S_DIR="./k8s"

# === STEP 1: Deploy Infra Services via Kompose YAMLs ===
echo "📦 Deploying infrastructure services (Redis, Kafka, Zookeeper, Schema Registry)..."
kubectl apply -f "$INFRA_DIR/"

# === STEP 2: Build & Push Backend Docker Image ===
echo "🔨 Building Spring Boot JAR..."
./mvnw clean package -DskipTests

echo "🐳 Building Docker image for backend..."
docker build -t "$BACKEND_IMAGE" .

echo "🚀 Pushing backend Docker image to DockerHub..."
docker push "$BACKEND_IMAGE"

# === STEP 3: Deploy Backend to Kubernetes ===
echo "☸️ Deploying backend to Kubernetes..."
kubectl apply -f "$K8S_DIR/"

# === STEP 4: Wait for Backend Pod to Be Ready ===
echo "⏳ Waiting for backend pod to be ready..."
kubectl wait --for=condition=ready pod -l app=task-management --timeout=90s

# Add delay just in case Spring Boot is still starting inside the container
echo "🕐 Waiting 10 more seconds for Spring Boot to fully boot up..."
sleep 10

# === STEP 5: Port-Forward Backend Service ===
echo "🔌 Starting port-forward: service/task-management-service → localhost:8080 ..."
kubectl port-forward service/task-management-service 8080:8080 > backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > backend.pid

# === STEP 6: Confirm Port is Active ===
echo "🔍 Verifying that backend is accessible..."
sleep 2
if lsof -i :8080 >/dev/null; then
  echo "✅ Backend is now accessible at http://localhost:8080"
else
  echo "❌ Port 8080 is not open. Port-forward may have failed. Check backend.log for details."
fi

# === DONE ===
echo "🚀 Deployment complete."
echo "🛑 To stop backend port-forward, run: kill \$(cat backend.pid)"

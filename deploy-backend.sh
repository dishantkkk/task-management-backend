#!/bin/bash

# === CONFIGURATION ===
DOCKERHUB_USERNAME="dishantkkk"
VERSION=$(date +%Y%m%d%H%M%S)
BACKEND_IMAGE="$DOCKERHUB_USERNAME/task-management:$VERSION"
INFRA_DIR="./infra"
K8S_DIR="./k8s"
DEPLOYMENT_YAML="$K8S_DIR/backend-deployment.yaml"

# === STEP 1: Deploy Infra Services ===
echo "📦 Deploying infrastructure services (Redis, Kafka, Zookeeper, Schema Registry, etc)..."
kubectl apply -f "$INFRA_DIR/"

# === STEP 2: Build & Push Backend Docker Image ===
echo "🔨 Building Spring Boot JAR..."
./mvnw clean package -DskipTests

echo "🐳 Building Docker image for backend..."
docker build -t "$BACKEND_IMAGE" .

echo "🚀 Pushing backend Docker image to DockerHub..."
docker push "$BACKEND_IMAGE"

# === STEP 3: Update Deployment YAML with Versioned Image ===
echo "📝 Updating deployment YAML with image: $BACKEND_IMAGE"
sed -i.bak "s|image: $DOCKERHUB_USERNAME/task-management:.*|image: $BACKEND_IMAGE|" "$DEPLOYMENT_YAML"

# === STEP 4: Deploy Backend to Kubernetes ===
echo "☸️ Deploying backend to Kubernetes..."
kubectl apply -f "$K8S_DIR/"

# === STEP 5: Wait for Backend Pod to Be Ready ===
echo "⏳ Waiting for backend pod to be ready..."
kubectl wait --for=condition=ready pod -l app=task-management --timeout=90s

echo "🕐 Waiting 10 more seconds for Spring Boot to fully start..."
sleep 10

# === STEP 6a: Port-Forward Backend Service ===
echo "🔌 Starting port-forward for backend on http://localhost:8080..."
kubectl port-forward service/task-management-service 8080:8080 > backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > backend.pid

# === STEP 6b: Port-Forward Elasticsearch Service ===
echo "🔌 Starting port-forward for Elasticsearch on http://localhost:9200..."
kubectl port-forward service/elasticsearch 9200:9200 > elasticsearch.log 2>&1 &
ES_PID=$!
echo $ES_PID > elasticsearch.pid

# === STEP 7: Confirm Both Ports Are Open ===
sleep 2
echo "🔍 Verifying port 8080 (backend)..."
if lsof -i :8080 >/dev/null; then
  echo "✅ Backend is accessible at http://localhost:8080"
else
  echo "❌ Port 8080 not active. Check backend.log"
fi

echo "🔍 Verifying port 9200 (Elasticsearch)..."
if lsof -i :9200 >/dev/null; then
  echo "✅ Elasticsearch is accessible at http://localhost:9200"
else
  echo "❌ Port 9200 not active. Check elasticsearch.log"
fi

# === DONE ===
echo "✅ Deployment complete with image tag: $VERSION"
echo "🛑 To stop port-forwards:"
echo "   kill \$(cat backend.pid)"
echo "   kill \$(cat elasticsearch.pid)"

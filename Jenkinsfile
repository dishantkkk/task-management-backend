pipeline {
  agent any

  environment {
    DOCKERHUB_USERNAME = 'dishantkkk'
    VERSION = "${new Date().format('yyyyMMddHHmmss')}"
    IMAGE_NAME = "${DOCKERHUB_USERNAME}/task-management:${VERSION}"
    INFRA_DIR = 'infra'
    K8S_DIR = 'k8s'
    DEPLOYMENT_YAML = "${K8S_DIR}/backend-deployment.yaml"
  }

  stages {

    stage('Checkout Code') {
      steps {
        echo "📥 Cloning repo..."
        git branch: 'main',
            url: 'https://github.com/dishantkkk/task-management-backend.git',
            credentialsId: 'github'
      }
    }

    stage('Build Backend (Maven)') {
      steps {
        echo '🔨 Building Spring Boot JAR with parallel Maven threads...'
        sh './mvnw clean package -DskipTests -T 4'
      }
    }

    stage('Build & Tag Docker Image') {
      steps {
        echo "🐳 Building Docker image: ${IMAGE_NAME}"
        sh "docker build --no-cache -t $IMAGE_NAME ."
      }
    }

    stage('Push to DockerHub') {
      steps {
        echo "🚀 Pushing image to DockerHub..."
        withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push $IMAGE_NAME
          """
        }
      }
    }

    stage('Patch Deployment YAML') {
      steps {
        echo "📝 Injecting versioned image into K8s YAML..."
        sh "sed -i.bak 's|image: ${DOCKERHUB_USERNAME}/task-management:.*|image: ${IMAGE_NAME}|' $DEPLOYMENT_YAML"
      }
    }

    stage('Deploy Infra (once only if needed)') {
      when {
        expression { return env.BRANCH_NAME == 'main' }
      }
      steps {
        echo "📦 Deploying infra YAMLs (Kafka, Redis, etc)..."
        sh "kubectl apply -f $INFRA_DIR/"
      }
    }

    stage('Deploy Backend to K8s') {
      steps {
        echo "☸️ Applying updated deployment..."
        sh "kubectl apply -f $K8S_DIR/"
      }
    }

    stage('Wait for Pod Ready') {
      steps {
        echo '⏳ Waiting for Spring Boot pod readiness...'
        sh 'kubectl wait --for=condition=ready pod -l app=task-management --timeout=120s'
      }
    }
  }

  post {
    success {
      echo "✅ Successfully deployed version: ${VERSION}"
    }
    failure {
      echo "❌ Build failed. Please check logs."
    }
  }
}

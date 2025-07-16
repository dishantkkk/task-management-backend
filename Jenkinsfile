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
    stage('Clone GitHub Repo') {
      steps {
        echo "📥 Cloning repo..."
        git branch: 'main',
            url: 'https://github.com/dishantkkk/task-management-backend.git',
            credentialsId: 'github'
      }
    }

    stage('Build Spring Boot App') {
      steps {
        echo '🔨 Building Spring Boot JAR...'
        sh './mvnw clean package -DskipTests'
      }
    }

    stage('Build Docker Image') {
      steps {
        echo "🐳 Building Docker image: ${IMAGE_NAME}"
        sh "docker build -t $IMAGE_NAME ."
      }
    }

    stage('Push Docker Image to DockerHub') {
      steps {
        echo "🚀 Pushing image to DockerHub: ${IMAGE_NAME}"
        withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push $IMAGE_NAME
          """
        }
      }
    }

    stage('Update Image in Deployment YAML') {
      steps {
        echo "📝 Updating deployment YAML with image: ${IMAGE_NAME}"
        sh "sed -i.bak 's|image: ${DOCKERHUB_USERNAME}/task-management:.*|image: ${IMAGE_NAME}|' $DEPLOYMENT_YAML"
      }
    }

    stage('Deploy Infra Services') {
      steps {
        echo "📦 Applying infrastructure YAMLs..."
        sh "kubectl apply -f $INFRA_DIR/"
      }
    }

    stage('Deploy Backend to Kubernetes') {
      steps {
        echo "☸️ Deploying backend to Kubernetes..."
        sh "kubectl apply -f $K8S_DIR/"
      }
    }

    stage('Wait for Backend Pod') {
      steps {
        echo '⏳ Waiting for pod readiness...'
        sh 'kubectl wait --for=condition=ready pod -l app=task-management --timeout=120s'
      }
    }

  }

  post {
    success {
      echo "✅ Deployment complete with image tag: ${VERSION}"
    }
    failure {
      echo "❌ Build failed!"
    }
  }
}

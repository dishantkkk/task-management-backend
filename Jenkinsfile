pipeline {
    agent any

    environment {
        IMAGE_NAME = "dishantkkk/task-management"
        SONARQUBE_ENV = "SonarQubeScanner"
    }

    tools {
        maven 'Maven 3'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git credentialsId: 'github-cred', url: 'https://github.com/dishantkkk/task-management-backend.git'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar -Dsonar.login=${SONAR_AUTH_TOKEN}'
                }
            }
            environment {
                SONAR_AUTH_TOKEN = credentials('sonarqube-token')
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Docker Login & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    '''
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                sh '''
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully ✅'
        }
        failure {
            echo 'Pipeline failed ❌'
        }
    }
}

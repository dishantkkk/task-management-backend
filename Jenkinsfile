pipeline {
    agent any

    environment {
        IMAGE_NAME = "dishantkkk/task-management"
        SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONARQUBE = "SonarQubeScanner"
    }

    tools {
        maven 'Maven 3'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
              steps {
                sh 'mvn clean install -DskipTests=false'
              }
            }

        stage('Test with Coverage') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh "${env.SONAR_SCANNER_HOME}/bin/sonar-scanner \
                            -Dsonar.projectKey=task-management \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                }
            }
            environment {
                SONAR_AUTH_TOKEN = credentials('sonarqube-token')
            }
        }

        stage('Quality Gate') {
              steps {
                timeout(time: 2, unit: 'MINUTES') {
                  waitForQualityGate abortPipeline: true
                }
              }
            }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME
                    '''
                }
            }
        }

        stage('Infra deployment') {
            steps {
                sh '''
                    kubectl apply -f infra/
                '''
            }
        }

        stage('Deploy to Minikube') {
            steps {
                sh '''
                    kubectl apply -f k8s/
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

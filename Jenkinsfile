pipeline {
    agent any

    environment {
        IMAGE_NAME = "dishantkkk/task-management"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        FULL_IMAGE_NAME = "${IMAGE_NAME}:${IMAGE_TAG}"
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
                sh 'mvn clean package -DskipTests=false'
                sh 'echo "Built files:" && ls -lh target'
            }
        }

        stage('Test with Coverage') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_AUTH_TOKEN = credentials('sonarqube-token')
            }
            steps {
                withSonarQubeEnv("${SONARQUBE}") {
                    sh """
                        ${env.SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=task-management \
                        -Dsonar.sources=src/main/java \
                        -Dsonar.tests=src/test/java \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.login=$SONAR_AUTH_TOKEN \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
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
                sh 'docker build -t $FULL_IMAGE_NAME .'
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $FULL_IMAGE_NAME
                    '''
                }
            }
        }

        stage('Deploy Infra') {
            steps {
                echo "Deploying infra resources from infra/ folder"
                sh 'kubectl apply -f infra/'
            }
        }

        stage('Deploy App (k8s)') {
            steps {
                echo "Deploying app resources from k8s/ folder"
                sh 'kubectl apply -f k8s/'
            }
        }
    }

    post {
        always {
            echo "Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully'
        }
        failure {
            echo '❌ Pipeline failed'
        }
    }
}

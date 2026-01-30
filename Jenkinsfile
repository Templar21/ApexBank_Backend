pipeline {
    agent any

    environment {
        API_URL = "http://localhost:8080/api/health" // CHANGE to your real API endpoint
    }

    stages {

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('Start Backend') {
            steps {
                sh '''
                    nohup mvn spring-boot:run > backend.log 2>&1 &
                    sleep 20
                '''
            }
        }

        stage('Call Backend API') {
            steps {
                sh '''
                    echo "Calling API: $API_URL"
                    curl -X GET "$API_URL" \
                         -H "Accept: application/json"
                '''
            }
        }
    }
}

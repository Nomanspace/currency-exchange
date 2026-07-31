pipeline {
	agent any
	
	stages {
		stage('Clone') {
			steps {
				sh 'echo "Repo cloned by Jenkins"'
			}
		}
	
	
		stage('Build Docker image') {
			steps {
				sh 'docker build -t currency-exchange-app .'
			}
		}
		
		stage('Deploy') {
			steps {
				sh 'docker compose down || true'
				sh 'docker compose up -d'
			}
		}
	}
}
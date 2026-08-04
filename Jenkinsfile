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
				sh 'echo "POSTGRES_DB=currencyexchange" > .env'
				sh 'echo "POSTGRES_USER=postgres" >> .env'
				sh 'echo "POSTGRES_PASSWORD=postgres" >> .env'
				sh 'docker compose down || true'
				sh 'docker compose up -d --build'
			}
		}
	}
}
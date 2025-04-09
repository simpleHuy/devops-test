pipeline {
  
  agent any
  
  stages {
  
    stage("build") {
      steps {
        echo 'building the appliation...'
      }
    }

    stage("test") {
      steps {
        echo 'testing the application...'
      }
    }
  }

  post {
    success {
      githubNotify context: 'Jenkins', status: 'SUCCESS', description: 'Build passed'
    }
    failure {
      githubNotify context: 'Jenkins', status: 'FAILURE', description: 'Build failed'
    }
  }
}

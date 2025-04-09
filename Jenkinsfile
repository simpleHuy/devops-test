pipeline {
  
  agent any
  stages {
    stage("test") {
      when {
        changeRequest()
      }
      steps {
        echo 'testing the application...'
      }
    }
    stage("build") {
      steps {
        echo 'building the appliation...'
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

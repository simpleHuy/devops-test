pipeline {
  
  agent {
    label 'docker-agent'
  }
  
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

    stage("deploy") {
      steps {
        echo 'deploying the application...'
        echo 'Deploying automation Deploy-staging'
      }
    }
  
  }

}

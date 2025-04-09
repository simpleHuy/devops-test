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

}

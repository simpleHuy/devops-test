{
    // lab 01:
    // pipeline {
    //     agent any

    //     environment {
    //         SERVICES = """
    //             spring-petclinic-admin-server
    //             spring-petclinic-api-gateway
    //             spring-petclinic-config-server
    //             spring-petclinic-customers-service
    //             spring-petclinic-discovery-server
    //             spring-petclinic-genai-service
    //             spring-petclinic-vets-service
    //             spring-petclinic-visits-service
    //         """
    //         CODECOV_TOKEN = credentials('codecov_token')
    //         COVERAGE_THRESHOLD = 70
    //     }

    //     stages {
    //         stage('Checkout') {
    //             steps {
    //                 checkout scm
    //             }
    //         }

    //         stage('Detect Changed Services') {
    //             steps {
    //                 script {
    //                     sh 'git fetch origin main:refs/remotes/origin/main'

    //                     def changedFiles = sh(
    //                         script: "git diff --name-only origin/main HEAD",
    //                         returnStdout: true
    //                     ).trim().split("\n")

    //                     echo "🔍 Changed files:\n${changedFiles.join('\n')}"

    //                     def changedServices = []
    //                     for (service in SERVICES.trim().split()) {
    //                         for (file in changedFiles) {
    //                             if (file.startsWith(service + "/")) {
    //                                 changedServices << service
    //                                 break
    //                             }
    //                         }
    //                     }

    //                     if (changedServices.isEmpty()) {
    //                         echo "✅ No service changes detected. Skipping build."
    //                         currentBuild.result = 'SUCCESS'
    //                         return
    //                     } else {
    //                         echo "📦 Changed services: ${changedServices}"
    //                         env.CHANGED_SERVICES = changedServices.join(',')
    //                     }
    //                 }
    //             }
    //         }

    //         stage('Test & Coverage') {
    //             when {
    //                 expression {
    //                     return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES.trim()
    //                 }
    //             }
    //             steps {
    //                 script {
    //                     def services = env.CHANGED_SERVICES.split(',')
    //                     for (svc in services) {
    //                         echo "🧪 Testing: ${svc}"

    //                         sh "./mvnw -pl ${svc} -am clean verify"

    //                         junit "**/${svc}/target/surefire-reports/*.xml"

    //                         // Parse coverage report (e.g., jacoco.xml)
    //                         def coverageFile = "${svc}/target/site/jacoco/jacoco.xml"
    //                         if (fileExists(coverageFile)) {
    //                             def coverageXml = readFile(coverageFile)
                                
    //                             // Extract all LINE counter elements
    //                             def lineCoveredMatches = (coverageXml =~ /counter type="LINE".*?covered="(\d+)"/)
    //                             def lineMissedMatches = (coverageXml =~ /counter type="LINE".*?missed="(\d+)"/)
                                
    //                             // Check if we have matches
    //                             if (lineCoveredMatches.count > 0 && lineMissedMatches.count > 0) {
    //                                 // Sum up all covered and missed lines
    //                                 def totalCovered = 0
    //                                 def totalMissed = 0
                                    
    //                                 // Process all matches
    //                                 for (int i = 0; i < lineCoveredMatches.count; i++) {
    //                                     totalCovered += lineCoveredMatches[i][1].toDouble()
    //                                 }
                                    
    //                                 for (int i = 0; i < lineMissedMatches.count; i++) {
    //                                     totalMissed += lineMissedMatches[i][1].toDouble()
    //                                 }
                                    
    //                                 // Calculate coverage percentage
    //                                 def lineCoverage = (totalCovered / (totalCovered + totalMissed)) * 100
                                    
    //                                 echo "📊 ${svc} Line Coverage: ${String.format('%.2f', lineCoverage)}%"
    //                                 // Enforce coverage threshold
    //                                 if (lineCoverage < COVERAGE_THRESHOLD.toDouble()) {
    //                                     error("❌ Coverage check failed for ${svc}: ${String.format('%.2f', lineCoverage)}% < ${COVERAGE_THRESHOLD}%")
    //                                 } else {
    //                                     echo "✅ Coverage check passed for ${svc}"
    //                                 }
    //                             } else {
    //                                 error("❌ Coverage data not found in ${coverageFile}")
    //                             }

    //                         } else {
    //                             echo "⚠️ Coverage file not found for ${svc}. Skipping coverage check."
    //                         }

    //                         // Upload coverage to Codecov
    //                         sh '''
    //                             curl -s https://codecov.io/bash -o codecov.sh
    //                             bash codecov.sh -t $CODECOV_TOKEN -F $svc -s $svc/target
    //                         '''
    //                     }
    //                 }
    //             }
    //         }

    //         stage('Build') {
    //             when {
    //                 expression {
    //                     return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES.trim()
    //                 }
    //             }
    //             steps {
    //                 script {
    //                     def services = env.CHANGED_SERVICES.split(',')
    //                     for (svc in services) {
    //                         echo "🏗️ Building (no tests): ${svc}"
    //                         sh "./mvnw -pl ${svc} -am package -DskipTests"
    //                     }
    //                 }
    //             }
    //         }
    //     }

    //     post {
    //         always {
    //             echo "🔚 Pipeline finished: ${currentBuild.result}"
    //         }
    //         success {
    //             echo "✅ All changed services built, tested, and coverage uploaded."
    //         }
    //         failure {
    //             echo "❌ Build failed. Check logs and coverage for details."
    //         }
    //     }
    // }
}

// lab 02:
pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        BRANCH_NAME = env.BRANCH_NAME
        REPOSITORY_PREFIX="scider/devops"
        
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Map of service names to their parameter branch values
                    def services = [
                        'spring-petclinic-admin-server': params['admin-server'],
                        'spring-petclinic-api-gateway': params['api-gateway'],
                        'spring-petclinic-config-server': params['config-server'],
                        'spring-petclinic-customers-service': params['customer-service'],
                        'spring-petclinic-discovery-server': params['discovery-server'],
                        'spring-petclinic-vets-service': params['vets-service'],
                        'spring-petclinic-visits-service': params['visit-service']
                    ]
                    
                    // For each service, check if we need to use a different branch
                    services.each { service, branch ->
                        if (branch != 'main') {
                            echo "Using branch ${branch} for ${service}"
                            // Checkout specific branch for this module if it's not main
                            sh "git checkout ${branch} -- ${service} || echo 'Failed to checkout ${branch} for ${service}'"
                        }
                    }
                }
            }
        }
        
        stage('Build Images') {
            steps {
                services.each { service, branch ->
                    script {
                        echo "Building Docker image for ${service} on branch ${branch}"
                        // Build the Docker image
                        sh """
                            cd ${service}
                            ../mvnv clean install -P buildDocker -DskipTests
                        """
                        sh """
                            docker tag ${REPOSITORY_PREFIX}/${serviceArtifactId} ${REPOSITORY_PREFIX}/${serviceArtifactId}:${COMMIT_ID}
                            docker tag ${REPOSITORY_PREFIX}/${serviceArtifactId} ${REPOSITORY_PREFIX}/${serviceArtifactId}:${branch}-${COMMIT_ID}
                            echo "Tagged ${serviceArtifactId} with ${COMMIT_ID} and ${branch}-${COMMIT_ID}"
                        """

                        // Check if the image was built successfully
                        def imageExists = sh(script: "docker images -q ${REPOSITORY_PREFIX}/${serviceArtifactId}:${COMMIT_ID}", returnStatus: true) == 0
                        if (!imageExists) {
                            error "Docker image for ${service} was not built successfully."
                        } else {
                            echo "Docker image for ${service} built successfully."
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            sh "docker logout"
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
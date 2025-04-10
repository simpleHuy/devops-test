pipeline {
    agent any

    environment {
        SERVICES = """
            spring-petclinic-admin-server
            spring-petclinic-api-gateway
            spring-petclinic-config-server
            spring-petclinic-customers-service
            spring-petclinic-discovery-server
            spring-petclinic-genai-service
            spring-petclinic-vets-service
            spring-petclinic-visits-service
        """
        CODECOV_TOKEN = credentials('codecov_token')
        COVERAGE_THRESHOLD = 0
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    sh 'git fetch origin test'

                    def changedFiles = sh(
                        script: "git diff --name-only origin/test...HEAD",
                        returnStdout: true
                    ).trim().split("\n")

                    echo "🔍 Changed files:\n${changedFiles.join('\n')}"

                    def changedServices = []
                    for (service in SERVICES.trim().split()) {
                        for (file in changedFiles) {
                            if (file.startsWith(service + "/")) {
                                changedServices << service
                                break
                            }
                        }
                    }

                    if (changedServices.isEmpty()) {
                        echo "✅ No service changes detected. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        return
                    } else {
                        echo "📦 Changed services: ${changedServices}"
                        env.CHANGED_SERVICES = changedServices.join(',')
                    }
                }
            }
        }

        stage('Test & Coverage') {
            when {
                expression {
                    return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES.trim()
                }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    for (svc in services) {
                        echo "🧪 Testing: ${svc}"

                        sh "./mvnw -pl ${svc} -am clean verify"

                        junit "**/${svc}/target/surefire-reports/*.xml"

                        // Parse coverage report (e.g., jacoco.xml)
                        def coverageFile = "${svc}/target/site/jacoco/jacoco.xml"
                        if (fileExists(coverageFile)) {
                            def coverageXml = readFile(coverageFile)

                            def lineCoveredMatch = (coverageXml =~ /counter type="LINE".*?covered="(\d+)"/)
                            def lineMissedMatch = (coverageXml =~ /counter type="LINE".*?missed="(\d+)"/)
                            
                            if (lineCoveredMatch.find() && lineMissedMatch.find()) {
                                def lineCovered = lineCoveredMatch[0][1].toDouble()
                                def lineMissed = lineMissedMatch[0][1].toDouble()
                                def lineCoverage = (lineCovered / (lineCovered + lineMissed)) * 100
                                
                                echo "📊 ${svc} Line Coverage: ${String.format('%.2f', lineCoverage)}%"
                                // Enforce coverage threshold
                                if (lineCoverage < COVERAGE_THRESHOLD.toDouble()) {
                                    error("❌ Coverage check failed for ${svc}: ${String.format('%.2f', lineCoverage)}% < ${COVERAGE_THRESHOLD}%")
                                } else {
                                    echo "✅ Coverage check passed for ${svc}"
                                }
                            }
                            else {
                                error("❌ Coverage data not found in ${coverageFile}")
                            }

                        } else {
                            echo "⚠️ Coverage file not found for ${svc}. Skipping coverage check."
                        }

                        // Upload coverage to Codecov
                        sh '''
                            curl -s https://codecov.io/bash -o codecov.sh
                            bash codecov.sh -t $CODECOV_TOKEN -F $svc -s $svc/target
                        '''
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression {
                    return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES.trim()
                }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    for (svc in services) {
                        echo "🏗️ Building (no tests): ${svc}"
                        sh "./mvnw -pl ${svc} -am package -DskipTests"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "🔚 Pipeline finished: ${currentBuild.result}"
        }
        success {
            echo "✅ All changed services built, tested, and coverage uploaded."
        }
        failure {
            echo "❌ Build failed. Check logs and coverage for details."
        }
    }
}
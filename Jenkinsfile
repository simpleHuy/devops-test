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
                    sh 'git fetch origin main'

                    def changedFiles = sh(
                        script: "git diff --name-only origin/main...HEAD",
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

                        sh "./mvnw -pl ${svc} -am clean verify jacoco:report"

                        junit "**/${svc}/target/surefire-reports/*.xml"

                        publishHTML(target: [
                            reportName           : "JaCoCo - ${svc}",
                            reportDir            : "${svc}/target/site/jacoco",
                            reportFiles          : 'index.html',
                            allowMissing         : true,
                            keepAll              : true,
                            alwaysLinkToLastBuild: true
                        ])

                        // 📊 Check JaCoCo line coverage >= 70%
                        def xmlPath = "${svc}/target/site/jacoco/jacoco.xml"
                        def coverageXml = readFile(xmlPath)
                        def parser = new XmlParser()
                        def report = parser.parseText(coverageXml)
                        def lineCounter = report.counter.find { it.@type == 'LINE' }
                        def covered = lineCounter.@covered.toInteger()
                        def missed = lineCounter.@missed.toInteger()
                        def total = covered + missed
                        def lineCoverage = (covered * 100.0 / total)

                        echo "📈 ${svc} Line Coverage: ${String.format('%.2f', lineCoverage)}%"

                        if (lineCoverage < 70.0) {
                            error("❌ Coverage check failed for ${svc}: ${String.format('%.2f', lineCoverage)}% < 70%")
                        } else {
                            echo "✅ Coverage check passed for ${svc}"
                        }
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

                        sh "./mvnw -pl ${svc} -am clean verify jacoco:report"

                        junit "**/${svc}/target/surefire-reports/*.xml"

                        publishHTML(target: [
                            reportName           : "JaCoCo - ${svc}",
                            reportDir            : "${svc}/target/site/jacoco",
                            reportFiles          : 'index.html',
                            allowMissing         : true,
                            keepAll              : true,
                            alwaysLinkToLastBuild: true
                        ])
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

pipeline {
    agent any
    
    parameters {
        string(name: 'admin-server', defaultValue: 'main', description: 'Branch to build for admin-server')
        string(name: 'api-gateway', defaultValue: 'main', description: 'Branch to build for api-gateway')
        string(name: 'config-server', defaultValue: 'main', description: 'Branch to build for config-server')
        string(name: 'customer-service', defaultValue: 'main', description: 'Branch to build for customer-service')
        string(name: 'discovery-server', defaultValue: 'main', description: 'Branch to build for discovery-server')
        string(name: 'vets-service', defaultValue: 'main', description: 'Branch to build for vets-service')
        string(name: 'visit-service', defaultValue: 'main', description: 'Branch to build for visit-service')
    }
    
    environment {
        DOCKER_HUB_CREDS = credentials('rVFUdFHuKL')
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        REPOSITORY_PREFIX = "scider/devops"
    }
    
    stages {

        stage('Test Docker') {
            steps {
                sh'echo "Testing Docker installation..."'
                sh 'docker --version'
                sh 'docker info'
            }
        }

        stage('Initialize') {
            steps {
                script {
                    // Define the services map at the pipeline level
                    // This makes it available throughout the pipeline
                    services = [
                        'spring-petclinic-admin-server': params['admin-server'],
                        'spring-petclinic-api-gateway': params['api-gateway'],
                        'spring-petclinic-config-server': params['config-server'],
                        'spring-petclinic-customers-service': params['customer-service'],
                        'spring-petclinic-discovery-server': params['discovery-server'],
                        'spring-petclinic-vets-service': params['vets-service'],
                        'spring-petclinic-visits-service': params['visit-service']
                    ]
                }
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
                script {
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
                script {
                    services.each { service, branch ->
                        echo "Building Docker image for ${service} on branch ${branch}"
                        
                        // Build the Docker image
                        sh """
                            cd ${service}
                            ../mvnw clean install -P buildDocker -Ddocker.image.prefix=${REPOSITORY_PREFIX} -DskipTests
                        """
                        
                        // Tag the image
                        sh """
                            docker tag ${REPOSITORY_PREFIX}/${service} ${REPOSITORY_PREFIX}/${service}:${COMMIT_ID}
                            docker tag ${REPOSITORY_PREFIX}/${service} ${REPOSITORY_PREFIX}/${service}:${branch}-${COMMIT_ID}
                            echo "Tagged ${service} with ${COMMIT_ID} and ${branch}-${COMMIT_ID}"
                        """

                        // Check if the image was built successfully
                        def imageExists = sh(script: "docker images -q ${REPOSITORY_PREFIX}/${service}:${COMMIT_ID}", returnStatus: true) == 0
                        if (!imageExists) {
                            error "Docker image for ${service} was not built successfully."
                        } else {
                            echo "Docker image for ${service} built successfully."
                        }
                    }
                }
            }
        }

        stage('Push Images') {
            steps {
                script {
                    // Login to Docker Hub
                    sh """
                        echo "${DOCKER_HUB_CREDS_PSW}" | docker login -u "${DOCKER_HUB_CREDS_USR}" --password-stdin
                        echo "Logged in to Docker Hub"
                    """
                    
                    services.each { service, branch ->
                        echo "Pushing Docker image for ${service} on branch ${branch}"
                        
                        // Push the images
                        sh """
                            docker push ${REPOSITORY_PREFIX}/${service}:${COMMIT_ID}
                            docker push ${REPOSITORY_PREFIX}/${service}:${branch}-${COMMIT_ID}
                            echo "Pushed ${service} with ${COMMIT_ID} and ${branch}-${COMMIT_ID}"
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
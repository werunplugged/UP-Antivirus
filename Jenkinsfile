pipeline {
    agent {
        kubernetes {
            label 'k8s-dev'
            defaultContainer 'agent'
        }
    }

    options {
        ansiColor('xterm')
        skipDefaultCheckout(false)
        disableConcurrentBuilds()
    }

    environment {
        REPO = sh(script: 'echo $GIT_URL | awk -F/ \'{print $NF}\' | sed -e "s/.git$//"', returnStdout: true).trim()
        GIT_ORG = sh(script: 'echo $GIT_URL | awk -F/ \'{print $4}\'', returnStdout: true).trim()
        SHORT_HASH = sh(script: 'git rev-parse --short=6 HEAD', returnStdout: true).trim()
        BRANCH_SHORT = sh(script: 'echo $BRANCH_NAME | sed -e "s/origin\\///" -e "s/feature\\///" -e "s/bugfix\\///"', returnStdout: true).trim()
        REGISTRY = 'ghcr.io'
        SIGNING_JOB_PATH = 'DeploymentHelper/SignAPK'
    }

    stages {
        stage('Pre-check Branch') {
            steps {
                script {
                    def isManualBuild = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size() > 0

                    if (env.BRANCH_NAME != 'main' && !isManualBuild) {
                        echo "⏭️ Skipping automatic build for branch '${env.BRANCH_NAME}'. Only 'main' branch builds automatically."
                        currentBuild.result = 'NOT_BUILT'
                        currentBuild.description = "⏭️ Skipped: Auto-build only for main branch"
                        error("Build skipped - not main branch and not manual trigger")
                    }

                    echo "✅ Proceeding with build for branch: ${env.BRANCH_NAME}"
                }
            }
        }

        stage('Initialize Variables') {
            steps {
                withFolderProperties {
                    script {
                        env.ORGANIZATION = env.GIT_ORG
                        echo "🏢 Organization: ${env.ORGANIZATION}"

                        def repoLower = env.REPO.toLowerCase()
                        
                        env.DOCKER_TAG = 'latest'
                        env.DOCKER_IMAGE_BASE = "${env.REGISTRY}/${env.GIT_ORG}/${repoLower}"
                        env.DOCKER_IMAGE_FULL = "${env.DOCKER_IMAGE_BASE}:${env.DOCKER_TAG}"

                        echo "🐳 Docker image will be: ${env.DOCKER_IMAGE_FULL}"
                    }
                }
            }
        }

        stage('Check Changes') {
            steps {
                script {
                    def dockerfileChanged = sh(
                        script: 'git diff --name-only HEAD~1 HEAD | grep -q "Dockerfile" && echo "true" || echo "false"',
                        returnStdout: true
                    ).trim() == 'true'
                    env.DOCKERFILE_CHANGED = dockerfileChanged.toString()
                    echo "📝 Dockerfile changed: ${env.DOCKERFILE_CHANGED}"
                }
            }
        }

        stage('🔐 Authenticate to Docker Registry') {
            when {
                environment name: 'DOCKERFILE_CHANGED', value: 'true'
            }
            steps {
                script {
                    withCredentials([[$class: 'VaultTokenCredentialBinding', addrVariable: 'VAULT_ADDR', credentialsId: 'jenkins_vault_github_token', tokenVariable: 'VAULT_TOKEN', vaultAddr: 'https://vault-dev.unpluggedsystems.app']]) {
                        def credentialsPath = "/up-secrets/unplugged/jenkins/up_service_installer/credentials/docker/${env.REGISTRY}/${env.ORGANIZATION}"
                        def dockerCredentials = sh(script: "vault kv get -field=credentials_id ${credentialsPath}", returnStdout: true).trim()

                        if (!dockerCredentials) {
                            error "No Docker credentials found in Vault for ${env.REGISTRY}/${env.ORGANIZATION}."
                        }

                        env.DOCKER_CREDENTIALS_ID = dockerCredentials
                        echo "✅ Retrieved Docker credentials from Vault: ${dockerCredentials}"
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                environment name: 'DOCKERFILE_CHANGED', value: 'true'
            }
            steps {
                withFolderProperties {
                    withCredentials([
                        string(credentialsId: 'jfrog_npm_api_key', variable: 'JFROG_API_KEY'),
                        string(credentialsId: 'jfrog_npm_virtual_registry', variable: 'NPM_CONFIG_REGISTRY')
                    ]) {
                        script {
                            echo "🔨 Building Docker image: ${env.DOCKER_IMAGE_FULL}"

                            docker.withRegistry("https://${env.REGISTRY}", env.DOCKER_CREDENTIALS_ID) {
                                def dockerImage = docker.build(
                                    "${env.DOCKER_IMAGE_FULL}",
                                    "--build-arg NPM_CONFIG_REGISTRY=\${NPM_CONFIG_REGISTRY} --build-arg JFROG_API_KEY=\${JFROG_API_KEY} --no-cache ."
                                )

                                dockerImage.push('latest')
                                dockerImage.push(env.SHORT_HASH)
                                echo "✅ Docker image built and pushed with tags: latest, ${env.SHORT_HASH}"
                                env.DOCKER_IMAGE_BUILT = 'true'
                            }
                        }
                    }
                }
            }
        }

        stage('Android Build Pipeline') {
            steps {
                script {
                    def dockerImage = "${env.DOCKER_IMAGE_BASE}:latest"
                    echo "🐳 Selected Docker image: ${dockerImage}"

                    def buildResult = build job: 'DeploymentHelper/AndroidBuilder',
                        parameters: [
                            string(name: 'DOCKER_IMAGE', value: dockerImage),
                            string(name: 'REPO_NAME', value: env.REPO),
                            string(name: 'BRANCH_NAME', value: env.BRANCH_NAME),
                            string(name: 'BRANCH_SHORT', value: env.BRANCH_SHORT),
                            string(name: 'GIT_URL', value: env.GIT_URL),
                            string(name: 'PARENT_BUILD_NUMBER', value: env.BUILD_NUMBER),
                            string(name: 'PARENT_JOB_NAME', value: env.JOB_NAME),
                            string(name: 'SIGNING_JOB_PATH', value: env.SIGNING_JOB_PATH)
                        ],
                        wait: true,
                        propagate: true

                    if (buildResult.result == 'SUCCESS') {
                        echo "✅ Android build completed successfully"

                        copyArtifacts projectName: 'DeploymentHelper/AndroidBuilder',
                                     selector: specific("${buildResult.number}"),
                                     filter: 'build-results.json',
                                     optional: true

                        if (fileExists('build-results.json')) {
                            def results = readJSON file: 'build-results.json'
                            env.ANDROID_BUILDER_NUMBER = results.androidBuilderNumber
                            env.DEBUG_SIGNED = results.debugSigned
                            env.RELEASE_SIGNED = results.releaseSigned
                            env.NEW_VERSION_NAME = results.version
                            env.DEBUG_JFROG_URL = results.debugJFrogUrl ?: ''
                            env.DEBUG_JFROG_FILE_NAME = results.debugJFrogFileName ?: ''
                            env.RELEASE_JFROG_URL = results.releaseJFrogUrl ?: ''
                            env.RELEASE_JFROG_FILE_NAME = results.releaseJFrogFileName ?: ''
                            env.HAS_DEBUG_JFROG_UPLOAD = results.hasDebugJFrogUpload ?: 'false'
                            env.HAS_RELEASE_JFROG_UPLOAD = results.hasReleaseJFrogUpload ?: 'false'

                            echo "📊 Build Results:"
                            echo "   Version: ${env.NEW_VERSION_NAME}"
                            echo "   Debug Signed: ${env.DEBUG_SIGNED}"
                            echo "   Release Signed: ${env.RELEASE_SIGNED}"
                            echo "   Debug JFrog: ${env.HAS_DEBUG_JFROG_UPLOAD}"
                            echo "   Release JFrog: ${env.HAS_RELEASE_JFROG_UPLOAD}"
                        } else {
                            echo "⚠️ build-results.json not found"
                        }
                    } else {
                        error "❌ Android build failed: ${buildResult.result}"
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                echo "✅ Pipeline completed successfully"

                currentBuild.description = """
                    ✅ Success | v${env.NEW_VERSION_NAME ?: 'unknown'}
                    🐛 Debug: ${env.DEBUG_SIGNED == 'true' ? '✅' : '❌'}
                    🚀 Release: ${env.RELEASE_SIGNED == 'true' ? '✅' : '❌'}
                    📦 JFrog: ${(env.HAS_DEBUG_JFROG_UPLOAD == 'true' || env.HAS_RELEASE_JFROG_UPLOAD == 'true') ? '✅' : '⏭️'}
                """.stripIndent()
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed"
                currentBuild.description = "❌ Build Failed"
            }
        }
        always {
            cleanWs(notFailBuild: true)
        }
    }
}
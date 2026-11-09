pipeline {
    agent {
        kubernetes {
            cloud 'K8S-DEV'
            inheritFrom 'k8s-dev'
            defaultContainer 'agent'
        }
    }

    parameters {
        booleanParam(name: 'BUILD_DEVELOPMENT', defaultValue: true, description: 'Build the development environment')
        booleanParam(name: 'BUILD_STAGING', defaultValue: true, description: 'Build the staging environment')
        booleanParam(name: 'BUILD_PRODUCTION', defaultValue: false, description: 'Build the production environment (signs with the production key)')
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
                        // Stays SUCCESS: NOT_BUILT or error() shows as a failed check on GitHub.
                        currentBuild.description = "⏭️ Skipped: Auto-build only for main branch"
                        env.SKIP_BUILD = 'true'
                        return
                    }


                    echo "✅ Proceeding with build for branch: ${env.BRANCH_NAME}"
                }
            }
        }

        stage('Initialize Variables') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                withFolderProperties {
                    script {
                        env.ORGANIZATION = env.GIT_ORG
                        echo "🏢 Organization: ${env.ORGANIZATION}"

                        def repoLower = env.REPO.toLowerCase()
                        
                        // One image per Dockerfile content, shared by every branch that has it.
                        env.DOCKER_TAG = 'df-' + sh(script: 'git hash-object Dockerfile', returnStdout: true).trim().take(12)
                        env.DOCKER_IMAGE_BASE = "${env.REGISTRY}/${env.GIT_ORG}/${repoLower}"
                        env.DOCKER_IMAGE_FULL = "${env.DOCKER_IMAGE_BASE}:${env.DOCKER_TAG}"

                        echo "🐳 Docker image will be: ${env.DOCKER_IMAGE_FULL}"
                    }
                }
            }
        }

        stage('🔐 Authenticate to Docker Registry') {
            when { expression { env.SKIP_BUILD != 'true' } }
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

        stage('Check Image') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                script {
                    def exists = false
                    docker.withRegistry("https://${env.REGISTRY}", env.DOCKER_CREDENTIALS_ID) {
                        exists = sh(script: "docker manifest inspect ${env.DOCKER_IMAGE_FULL} > /dev/null 2>&1", returnStatus: true) == 0
                    }
                    env.BUILD_IMAGE = (!exists).toString()
                    echo "🐳 ${env.DOCKER_IMAGE_FULL}: ${exists ? 'already built' : 'not built yet'}"
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                environment name: 'BUILD_IMAGE', value: 'true'
            }
            steps {
                withFolderProperties {
                    script {
                        echo "🔨 Building Docker image: ${env.DOCKER_IMAGE_FULL}"

                        docker.withRegistry("https://${env.REGISTRY}", env.DOCKER_CREDENTIALS_ID) {
                            def dockerImage = docker.build("${env.DOCKER_IMAGE_FULL}", "--no-cache .")

                            dockerImage.push(env.DOCKER_TAG)
                            // Only the default branch moves :latest, so a branch build never replaces it.
                            if (env.BRANCH_NAME in ['main', 'master']) { dockerImage.push('latest') }
                            echo "✅ Docker image built and pushed: ${env.DOCKER_TAG}"
                            env.DOCKER_IMAGE_BUILT = 'true'
                        }
                    }
                }
            }
        }

        stage('Android Build Pipeline') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                script {
                    def dockerImage = env.DOCKER_IMAGE_FULL
                    echo "🐳 Selected Docker image: ${dockerImage}"

                    // One run builds every checked environment (dev -> staging -> production), all from
                    // this commit. Unset development/staging count as checked; production is opt-in.
                    boolean isMain = env.BRANCH_NAME == 'main'
                    boolean isManualBuild = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size() > 0
                    def envs = []
                    if (params.BUILD_DEVELOPMENT != false) { envs << 'development' }
                    if (params.BUILD_STAGING != false)     { envs << 'staging' }
                    if (isManualBuild ? params.BUILD_PRODUCTION == true : isMain) { envs << 'production' }
                    if (!envs) {
                        error('❌ No environment selected. Check at least one of development, staging, production.')
                    }
                    def sha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def summary = []

                    // Order dev -> staging -> production with propagate: true: a failure stops the
                    // run, so production is never built from code that just failed in dev.
                    for (flavor in envs) {
                        echo "🏗️ Building ${flavor} from ${sha.take(8)}"
                        def buildResult = build job: 'DeploymentHelper/AndroidBuilderTest',
                            parameters: [
                                string(name: 'DOCKER_IMAGE', value: dockerImage),
                                string(name: 'REPO_NAME', value: env.REPO),
                                string(name: 'BRANCH_NAME', value: env.BRANCH_NAME),
                                string(name: 'BRANCH_SHORT', value: env.BRANCH_SHORT),
                                string(name: 'GIT_URL', value: env.GIT_URL),
                                string(name: 'PARENT_BUILD_NUMBER', value: env.BUILD_NUMBER),
                                string(name: 'PARENT_JOB_NAME', value: env.JOB_NAME),
                                string(name: 'SIGNING_JOB_PATH', value: env.SIGNING_JOB_PATH),
                                string(name: 'FLAVOR', value: flavor),
                                string(name: 'GIT_SHA', value: sha)
                            ],
                            wait: true,
                            propagate: true

                        def line = "${flavor}: AndroidBuilderTest #${buildResult.number}"
                        copyArtifacts projectName: 'DeploymentHelper/AndroidBuilderTest',
                                     selector: specific("${buildResult.number}"),
                                     filter: 'build-results.json',
                                     optional: true
                        if (fileExists('build-results.json')) {
                            def results = readJSON file: 'build-results.json'
                            line += " | v${results.version}"
                            line += " | debug ${results.debugSigned == 'true' ? '✅' : '⏭️'} release ${results.releaseSigned == 'true' ? '✅' : '❌'}"
                            sh 'rm -f build-results.json'
                        }
                        echo "✅ ${line}"
                        summary << line
                    }
                    env.BUILT_ENVIRONMENTS = envs.join(', ')
                    env.BUILD_SUMMARY = summary.join('<br>')
                }
            }
        }
    }
    post {
        success {
            script {
                if (env.SKIP_BUILD != 'true') {
                    echo "✅ Pipeline completed successfully"

                    currentBuild.description = "✅ ${env.BUILT_ENVIRONMENTS ?: '-'}<br>${env.BUILD_SUMMARY ?: ''}"
                }
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
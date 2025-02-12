@Library(['build', 'general']) _

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timestamps()
        ansiColor('xterm')
    }
    
    triggers {
        cron('*/5 * * * 1-5')
    }

    environment {
        SERVICE = 'service.json'
        GIT_REPO_OWNER = 'MODifikaTOR18'
        GIT_REPO_NAME = 'python-helloworld'
        DOCKER_REGISTRY = "ghcr.io/${GIT_REPO_OWNER.toLowerCase()}"
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    log.info ("Starting preparation steps...")
                    log.info ("Current environment:")
                    log.info ("Change id: ${env.CHANGE_ID}")
                    log.info ("Apps: ${env.SERVICE}")
                    log.info ("Branch: ${env.BRANCH_NAME}")
                    PR_ID = prID(env.BRANCH_NAME)
                    log.info ("Pull request ID: ${PR_ID}")
                    apps = readJSON file: env.SERVICE
                    log.info ("Build app is ${apps}")

                    TAG_STACK = sh(returnStdout: true, script: "git tag --sort version:refname | tail -1").trim()
                    
                    env.TAG = (TAG_STACK.isEmpty()) ? 'latest' : TAG_STACK
                    log.info ("Current tag for build is: ${env.TAG}")
                    PR_DIFF=[]
                    BUILD_APPS=[:]
                    
                    PR_DIFFS=sh(script: 'git show --pretty=format: --name-only', returnStdout: true).trim()
                    log.info ("Pull request diffs:\n${PR_DIFFS}")
                    if (!PR_ID.isEmpty()) {
                        log.warning ("Pull request id not found")
                        def response = httpRequest url: "https://api.github.com/repos/${GIT_REPO_OWNER}/${GIT_REPO_NAME}/pulls/${PR_ID}/files",
                                                    customHeaders: [[name: 'Accept', value: 'application/vnd.github+json'],[name: 'X-GitHub-Api-Version', value: '2022-11-28']],
                                                    authentication: 'GitHub_ID'

                        log.info ("Request status Github API: ${response.status}")
                        PR_DIFF_DATA = readJSON text: response.content
                        log.info ("Request answer contents: ${PR_DIFF_DATA}")
                        PR_DIFF_DATA.each { item ->
                            log.info ("PR_DIFF item filename: ${item.filename}")
                            PR_DIFF.add(item.filename)
                        }
                        log.info ("PR diff: ${PR_DIFF}")
                        apps.each { app, path ->
                            CHANGE = isChanged(PR_DIFF, path)
                            log.info ("Change check for ${PR_DIFF} and ${path}: ${CHANGE}")
                            if (CHANGE) {
                                BUILD_APPS.put(app, path)
                                isPR_DIFFS = true
                            }
                        }
                    } else {
                        BUILD_APPS = apps
                        isPR_DIFFS = false
                    }
                    
                    log.info ("Build apps: ${BUILD_APPS}")
                    Integer PARALLEL_EXECUTE_COUNT = 1
                    builtApps = [:]
                    buildStages = stagePrepare ('image', BUILD_APPS, PARALLEL_EXECUTE_COUNT, builtApps)
                    log.info ("Prepared!")
                }
            }
        }

        stage ('Create image') {
            when { allOf {
                expression { BRANCH_NAME == 'master' }
                expression { !BUILD_APPS.isEmpty() }
                }
            }
            steps {
                script {
                    log.info ("Starting image create...")
                    Integer PARALLEL_EXECUTE_COUNT = 1
                    buildStages.each { stage ->
                        parallel stage
                    }
                    log.info ("Apps were built: ${builtApps}")
                    log.info ("Images were created.")
                }
            }
        }

        stage ('Push image') {
            when { allOf {
                expression { BRANCH_NAME == 'master' }
                expression { !builtApps.isEmpty() }
                }
            }
            steps {
                script {
                    log.info ("Beginning to push images to GitHub...")
                    log.info ("Tag added to images: ${env.TAG}")
                    pushedApps = []
                    withCredentials([usernamePassword(credentialsId: 'GitHub_ID', passwordVariable: 'pass', usernameVariable: 'user')]) {
                        sh "echo ${pass} | docker login ${DOCKER_REGISTRY} -u ${user} --password-stdin"
                        log.info("Successfully logged in.")
                    }
                    builtApps.each { app, path ->
                        sh "docker tag ${app}:${TAG} ${DOCKER_REGISTRY}/${app}:${TAG}"
                        sh "docker push ${DOCKER_REGISTRY}/${app}:${TAG}"
                        pushedApps.add(app)
                        log.info ("Successfully pushed docker image ${app} to Docker registry.")
                    }
                    log.info ("Images were pushed to GitHub!")
                }
            }
        }

        stage ('Load image into K8s Kind') {
            when { allOf {
                expression { BRANCH_NAME == 'master' }
                expression { !builtApps.isEmpty() }
                }
            }
            steps {
                script {
                    log.info ("Started to load image into Kubernetes Kind")
                    builtApps.each { app, path ->
                        sh """
                            docker save -o /tmp/app.tar "${app}:${TAG}"
                            chmod 0644 /tmp/app.tar
                            cd /tmp/jenkins/rev_ssh
                            pwd
                            scp -i diplom.pem -o StrictHostKeyChecking=no ../../app.tar ubuntu@\$(cat remote_ip.txt | tr -d '\n\r '):/tmp/app.tar
                            ssh -i diplom.pem -o StrictHostKeyChecking=no ubuntu@\$(cat remote_ip.txt | tr -d '\n\r ') /jenkins/casc/rev_ssh/kind_load.sh
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
        success {
            script {
                if (!pushedApps.isEmpty()) {
                    PUBLISHED = true
                } else {
                    PUBLISHED = false
                }
            }
            withCredentials([string(credentialsId: 'botSecret', variable: 'TOKEN'), string(credentialsId: 'chatId', variable: 'CHAT_ID')]) {
                sh  ("""
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHAT_ID} -d parse_mode=markdown -d text='*${env.JOB_NAME}* : *Branch*: ${env.GIT_BRANCH} : *From pull request*: ${isPR_DIFFS} : *Build* : OK *Published* = ${PUBLISHED}'
                """)
            }
        }

        aborted {
            withCredentials([string(credentialsId: 'botSecret', variable: 'TOKEN'), string(credentialsId: 'chatId', variable: 'CHAT_ID')]) {
                sh  ("""
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHAT_ID} -d parse_mode=markdown -d text='*${env.JOB_NAME}* : POC *Branch*: ${env.GIT_BRANCH} *Build* : `Aborted` *Published* = `Aborted`'
                """)
                }
        }

        failure {
            withCredentials([string(credentialsId: 'botSecret', variable: 'TOKEN'), string(credentialsId: 'chatId', variable: 'CHAT_ID')]) {
                sh  ("""
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHAT_ID} -d parse_mode=markdown -d text='*${env.JOB_NAME}* : POC  *Branch*: ${env.GIT_BRANCH} *Build* : `not OK` *Published* = `No`'
                """)
            }
        }
    }
}
// Comment for PR-2
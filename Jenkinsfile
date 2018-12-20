pipeline {
  agent {
    label "jenkins-maven"
  }
  environment {
    ORG = 'nuxeo-sandbox'
    APP_NAME = 'jenkinsx-quickstart-nuxeo-poc'
    CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
  }
  stages {
    stage('CI Build and push snapshot') {
      when {
        branch 'PR-*'
      }
      environment {
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
      }
      steps {
        container('maven') {
          sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
          sh "mvn install"
          sh "export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"
          dir('charts/preview') {
            sh "make preview"
            sh "jx preview --app $APP_NAME --dir ../.."
          }
        }
      }
    }
    stage('CI Build feature branch against Mongo') {
      when {
        branch 'feature-*'
      }
      environment {
        APP_NAME = 'mongodb'
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "mongo-$BRANCH_NAME".toLowerCase()
      }
      steps {
        container('maven') {
          dir('charts/junits-mongo') {
            sh "make mongo"
            sh "PREVIEW_NAMESPACE=${PREVIEW_NAMESPACE} jx preview --app $APP_NAME --dir ../.."
          }
          sh "touch /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.core=mongodb >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.mongodb.server=mongodb://preview-mongodb:27017 >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.mongodb.dbname=vcstest >> /root/nuxeo-test-vcs.properties"  
          sh "sleep 30000"
          sh "mvn clean package -Pcustomdb,mongodb"
        }
      }  
    }
    stage('Build Release') {
      when {
        branch 'master'
      }
      steps {

        container('maven') {
          
          // ensure we're not on a detached head
          sh "git checkout master"
          sh "git config --global credential.helper store"
          sh "jx step git credentials"

          // try to override bad maven global settings mounted as read-only
         
          // so we can retrieve the version in later steps
          sh "echo \$(jx-release-version) > VERSION"
          sh "mvn -X versions:set -DnewVersion=\$(cat VERSION)"
          sh "jx step tag --version \$(cat VERSION)"
          sh " mvn clean install -X"
          sh "export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
        }
      }
    }
    stage('Promote to Environments') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          dir('charts/jenkinsx-quickstart-nuxeo-poc') {
            sh "jx step changelog --version v\$(cat ../../VERSION)"

            // release the helm chart
            sh "jx step helm release"

            // promote through all 'Auto' promotion Environments
            sh "jx promote -b --all-auto --timeout 1h --version \$(cat ../../VERSION)"
          }
        }
      }
    }
  }
  post {
        always {
          cleanWs()
        }
  }
}

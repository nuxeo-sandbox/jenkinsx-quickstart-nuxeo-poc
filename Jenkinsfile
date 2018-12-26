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
    stage('CI Build junits in feature branch against Mongo') {
      when {
        branch 'feature-*'
      }
      environment {
        APP_NAME = 'mongodb'
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
      }
      steps {
        container('maven') {
          dir('charts/junits') {
            sh "make mongo"
            sh "jx preview --app $APP_NAME --namespace=${BRANCH_NAME} --dir ../.."
          }
          sh "touch /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.core=mongodb >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.mongodb.server=mongodb://preview-${APP_NAME}.${BRANCH_NAME}.svc.cluster.local >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.mongodb.dbname=vcstest >> /root/nuxeo-test-vcs.properties"  
          sh "mvn clean package -Pcustomdb,mongodb"
          sh "kubectl delete namespace ${BRANCH_NAME}"
        }
      }  
    }
    stage('CI Build junits in feature branch against Postgres') {
      when {
        branch 'feature-*'
      }
      environment {
        APP_NAME = "postgresql-$BRANCH_NAME"
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
      }
      steps {
        container('maven') {
         dir('charts/junits') {
            sh "helm install --name ${APP_NAME} --namespace=${BRANCH_NAME} -f values-postgresql.yaml stable/postgresql --version  3.1.0"
          }
          sh "touch /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.vcs.db=PostgreSQL >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.vcs.server=${APP_NAME}-postgresql.${BRANCH_NAME}.svc.cluster.local >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.vcs.database=vctests >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.vcs.user=nuxeo >> /root/nuxeo-test-vcs.properties"
          sh "echo nuxeo.test.vcs.password=nuxeo >> /root/nuxeo-test-vcs.properties"  
          sh "mvn clean package -Pcustomdb,pgsql"
          sh "helm del --purge ${APP_NAME}"
          sh "kubectl delete namespace ${BRANCH_NAME}"
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

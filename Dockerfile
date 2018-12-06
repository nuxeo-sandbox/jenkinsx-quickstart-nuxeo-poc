FROM nuxeo:10.2

RUN cat /etc/nuxeo/nuxeo.conf.template > $NUXEO_CONF
WORKDIR /tmp 
ADD jenkinsx-quickstart-nuxeo-poc-package/target/jenkinsx-quickstart-nuxeo-poc-package-*.zip /tmp/jenkinsx-quickstart-nuxeo-poc-package.zip
RUN nuxeoctl mp-install nuxeo-web-ui /tmp/jenkinsx-quickstart-nuxeo-poc-package.zip --nodeps

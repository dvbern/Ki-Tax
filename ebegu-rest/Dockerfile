#FROM docker-registry.dvbern.ch/devops/docker/appserv/wildfly/21.0.1-jdk11-alpine
FROM docker-registry.dvbern.ch/kibon/kibon-wildfly@sha256:01683e302f1f707f0ef9b34359cb24c88c8e973b8beda0b93e01a59225f335b2
RUN rm /opt/jboss/wildfly/standalone/deployments/ebegu-rest.war # remove as soon as we are back on "real" base image

ARG KIBON_POM_VERSION

ENV MARIADB_DRIVER_VERSION=2.3.0

LABEL product=kibon

# Setup as root user (otherwise we get permission issues of copied files and non-standalone wildfly directories)
USER root


###################
# Configure JBoss #
###################

#Sprache einstellen
ENV LANG=de_CH.UTF-8

#RUN mkdir ${JBOSS_HOME}/customization
WORKDIR ${JBOSS_HOME}/customization

#Sentry Jar bereitstellen fuer cli
COPY target/for-docker-build/sentry.jar /customization/sentry.jar

#Run CLI commands
#COPY customization/ebegu-customization.cli ebegu-customization.cli
#RUN ${JBOSS_HOME}/bin/jboss-cli.sh --file=ebegu-customization.cli

#Wildfly Admin user einfuegen
#RUN ${JBOSS_HOME}/bin/add-user.sh ebegu-admin jbtwLs --silent

# Data directory (e.g. for a file based Lucene-Index)
ENV KIBON_DATADIR=${JBOSS_HOME}/standalone/data/kibon-data
# All volumes should be initialised. Otherwise they will get root-only permissions upon creation of the container
#RUN mkdir -p "${KIBON_DATADIR}"

###########
# cleanup #
###########

#RUN rm -rf  ${JBOSS_HOME}/customization
#RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

###########
# Runtime #
###########

#JBOSS JVM configuration und properties in container kopieren
COPY --from=docker-registry.dvbern.ch/kibon/wildfly-dockerconfiguration:76d48905ec38ba6f9b854dd1a7f8bbf9cefcc4e4 /copy /


#Applikation hinzufuegen
ADD target/ebegu-rest-${KIBON_POM_VERSION}.war /opt/jboss/wildfly/standalone/deployments/
ADD target/for-docker-build/kibon-belogin-connector-war.war /opt/jboss/wildfly/standalone/deployments/

#mariadb driver aus unserem base wildfly image downgraden
#ADD https://nexus.dvbern.ch/nexus/service/local/repositories/central/content/org/mariadb/jdbc/mariadb-java-client/${MARIADB_DRIVER_VERSION}/mariadb-java-client-$MARIADB_DRIVER_VERSION.jar \
#		"${JBOSS_HOME}/modules/org/mariadb/main/mariadb-java-client.jar"


#ADD CERT of ngnix

#RUN keytool -cacerts  -noprompt -storepass changeit -importcert -alias local-kibon.dvbern.ch -file /tmp/local-kibon.dvbern.ch.cer \
#	&& 	rm -rf  /tmp/local-kibon.dvbern.ch.cer

#ADD CERT OF BERN FOR GERES TEST WEBSERVICE
#RUN keytool -cacerts  -noprompt -storepass changeit -importcert -alias CERT-001-BE-CH-Issuing-CA -file /tmp/test-gers.cer \
#	&& rm -rf  /tmp/test-geres.cer


#RUN chown -R app:app ${JBOSS_HOME}/standalone \
#	&& chmod +r ${JBOSS_HOME}/ebegu-docker.properties \
#	&& chmod +r ${JBOSS_HOME}/rkb1-svbern-sts-ks-u.jks \
#	&& chmod +r ${JBOSS_HOME}/rkh8-svbern-sts-a.jks \
#	&& chmod +r ${JBOSS_HOME}/rri5-svbern-sts-ks-a.jks \
#	&& chmod +r ${JBOSS_HOME}/rbr1-svbern-sts-ks-t.jks

#ADD entry script that sets some bindings
#ADD entry.sh /
RUN chmod +x /entry.sh

#ADD entry-ha.sh /
RUN chmod +x /entry-ha.sh

WORKDIR $JBOSS_HOME

# properties beim serverstart mitgeben
WORKDIR ${JBOSS_HOME}
ENTRYPOINT ["/bin/sh", "/entry-ha.sh"]

#Healthcheck
HEALTHCHECK --interval=5m --timeout=3s CMD wget --quiet --tries=2  --no-check-certificate --spider -U DockerHealthCheck http://$(hostname -i || echo '127.0.0.1'):8080/ebegu/api/v1/swagger.json  || exit 1


EXPOSE 8080 8443 9990

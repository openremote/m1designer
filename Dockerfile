FROM centos:centos7

RUN yum -y update; yum -y install java-1.8.0-openjdk-devel

ADD *.jar /opt/orc3/

EXPOSE 8080
EXPOSE 9292

ENV DEV_MODE false
ENV WEBSERVER_DOCUMENT_ROOT jar:file:/opt/orc3/openremote-beta-editor-client.jar!/

CMD /usr/bin/java -cp '/opt/orc3/*' org.openremote.beta.server.Server

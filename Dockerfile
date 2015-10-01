FROM centos:centos7

RUN yum -y update; yum -y install java-1.8.0-openjdk-devel

EXPOSE 8080

ENV DEV_MODE false
ENV WEBSERVER_DOCUMENT_ROOT /opt/orc3/or-controller-client.jar
ENV WEBSERVER_ADDRESS 0.0.0.0

ADD *.jar /opt/orc3/

CMD /usr/bin/java -cp '/opt/orc3/*' org.openremote.server.Server

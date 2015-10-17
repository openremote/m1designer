# OpenRemote Controller & Web Editor/Console

* Live demo: [http://orc3.christianbauer.name/](http://orc3.christianbauer.name/)

Concept
---

Combining UI/UX ideas of [Origami/Quartz UI Composer](http://facebook.github.io/origami/tutorials/)
and [Node-RED](http://nodered.org/) with an [Apache Camel](http://camel.apache.org/) backend.

Development
---

* Install JDK 1.8

* Run GWT super dev mode code-server: `./gradlew gwtSuperDev`

* Run server: `./gradlew serverRun`

* Open [http://localhost:8080/](http://localhost:8080/) in browser

Build tested archives
---

    ./gradlew clean build

All JARs can be found in `build/libs/`.

Optimize for production:

* Disable client DEBUG logging in `src/main/resources/org/openremote/Client.gwt.xml`
* Disable server DEBUG logging in `src/main/resources/logback.xml`

Run production
---

    WEBSERVER_DOCUMENT_ROOT='/Users/cb/work/openremote/gh/or-controller/build/libs/or-controller-client.jar' \
    WEBSERVER_ADDRESS=0.0.0.0 \
    WEBSERVER_PORT=8080 \
    WEBSERVER_ALLOW_ORIGIN=http://localhost:8080 \
    DEV_MODE=false \
    java -cp 'build/libs/*' org.openremote.server.Server

For quick turnaround when debugging client in production, recompile only the client portion between server restarts:

    ./gradlew assembleClient

Build image and run Docker container
---

    docker stop orc3
    docker rm orc3
    docker rmi orc3:latest
    cp Dockerfile build/libs/
    docker build -t orc3:latest build/libs/
    docker run -d --name=orc3 -p 8006:8080 -v /etc/localtime:/etc/localtime orc3

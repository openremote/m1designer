# OR3 Controller/Designer

* Live demo: [http://orc3.christianbauer.name/](http://orc3.christianbauer.name/)

Concept
---

Combining UI/UX ideas of [Origami/Quartz UI Composer](http://facebook.github.io/origami/tutorials/)
and [Node-RED](http://nodered.org/) with an [Apache Camel](http://camel.apache.org/) backend.

Development
---

* Install JDK 1.8

* Checkout `orc3-zwave` project and follow its build instructions

* Edit `gradle.properties` and set `zwaveProjectDirectory` to path of `orc3-zwave` project

* Run GWT super dev mode code-server: `./gradlew gwtSuperDev`

* Run server: `./gradlew serverRun`

* Open [http://localhost:8080/](http://localhost:8080/) in browser

Build tested archives
---

    ./gradlew clean check build

All JARs can be found in `build/libs/`.

Run production
---

    WEBSERVER_DOCUMENT_ROOT='jar:file:/Users/cb/work/openremote/gh/or3-controller/build/libs/or3-controller-client.jar!/' \
    WEBSERVER_ADDRESS=0.0.0.0 \
    WEBSERVER_PORT=8080 \
    WEBSOCKET_ADDRESS=0.0.0.0 \
    WEBSOCKET_PORT=9292 \
    DEV_MODE=false \
    java -cp 'build/libs/*' org.openremote.beta.server.Server

If you change the WebSocket host or port, you must adjust `index.html`.

Build image and run Docker container
---

    docker stop orc3
    docker rm orc3
    docker rmi orc3:latest
    cp Dockerfile build/libs/
    docker build -t orc3:latest build/libs/
    docker run -d --name=orc3 -p 8006:8080 -p 9292:9292 -v /etc/localtime:/etc/localtime orc3

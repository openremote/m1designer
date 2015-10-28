# OpenRemote Controller & Web Editor/Console

Live demo: [http://orc3.christianbauer.name/](http://orc3.christianbauer.name/)

Concept
---

Combining UI/UX ideas of [Origami/Quartz UI Composer](http://facebook.github.io/origami/tutorials/) and [Node-RED](http://nodered.org/) with an [Apache Camel](http://camel.apache.org/) backend.

Development
---

* Install JDK 1.8

* Run GWT super dev mode code-server: `./gradlew gwtSuperDev`

* Run server: `./gradlew serverRun` or start `org.openremote.server.Server#main()` in your IDE

* Open [http://localhost:8080/](http://localhost:8080/) in browser

* Edit (but don't commit) the TODO in `ShellPresenter.java` to open editor immediately on page load

* The application uses a temporary in-memory database with sample data. If you like to switch to an external database instance, set `DATABASE_CONNECTION_URL=jdbc:h2:file:</some/database/file/path>` or `jdbc:h2:tcp://localhost/mem:test` for an already running external database instance (`java -jar h2.jar`).

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
    CREATE_DATABASE_SCHEMA=true \
    java -cp 'build/libs/*' org.openremote.server.Server

If `CREATE_DATABASE_SCHEMA` is enabled, the application will drop and recreate its database on startup. Set to `false` or remove the option if you want to keep all data in the file `or-controller-database.mv.db`. You may have to remove the database after incompatible code changes by removing the file `or-controller-database.mv.db` and recreating it.

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

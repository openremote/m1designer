# OpenRemote Controller

This is a pre-alpha version of the OpenRemote v3 controller, please use only for testing and development. Note that no access control or security has been implemented, do not expose the controller on a public network.

Development
---

* Install JDK 1.8

* Run GWT super dev mode code-server: `./gradlew gwtSuperDev`

* Run server: `./gradlew serverRun` or start `org.openremote.server.Server#main()` in your IDE

* Open [http://localhost:8080/](http://localhost:8080/) in browser

* Open [http://localhost:8080/#shell](http://localhost:8080/#shell) in browser to open the editor shell on page load

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

If `CREATE_DATABASE_SCHEMA` is enabled, the application will drop and recreate its database on startup. Set to `false` (when you start the server the second time, for example) or remove the option if you want to keep all data in the file `or-controller-database.mv.db`. You may have to remove the database after incompatible code changes by removing the file `or-controller-database.mv.db` and recreating it.

For quick turnaround when debugging client in production, recompile only the client portion between server restarts:

    ./gradlew assembleClient
    

Build and publish Docker image
------------------------------

    ./gradlew clean build
    # (Disable DEBUG logging, etc...)

    cp Dockerfile build/libs/
    docker build -t openremote/demo-m1-designer:latest build/libs/

    # Optional
    docker login
    docker push openremote/demo-m1-designer:latest
    
Run Docker container
--------------------
    docker run -d \
     --name=demodesigner \
     -p 8080:8080 \
     -v /etc/localtime:/etc/localtime \
     --device=/dev/ttyUSB0 \
     openremote/demo-m1-designer:latest

Environment Variables
---

|Name | Default Value | Description|
|---|---|---|
|`DEV_MODE`|`true`|If enabled, the application is optimized for quick development turnaround and debug logging.|
|`WEBSERVER_ADDRESS`|`127.0.0.1`|The network address the webserver is listening on, use `0.0.0.0` to listen on all interfaces.|
|`WEBSERVER_PORT`|`8080`| The TCP port of the webserver.|
|`WEBSERVER_DOCUMENT_ROOT`|`src/main/webapp`|Location of `index.html` and other static web resources. JAR files are directly supported.|
|`WEBSERVER_DOCUMENT_CACHE_SECONDS`|`300`|Maximum age of served static web resources, unless their name contains `nocache` or dev mode is enabled.|
|`WEBSERVER_ALLOW_ORIGIN`|`http://localhost:8080`|Allow origin CORS response header.|
|`TRANSACTION_SERVER_ID`|`MyOpenRemoteController123`|A unique identifier (for transaction logging) of your application, must be less than 52 characters long. If you run several instances of the application (even in several VMs) with the same database, you must set this.|
|`DATABASE_PRODUCT`|`H2`|Database SQL dialect, currently only `H2` is supported.|
|`DATABASE_CONNECTION_URL`|`jdbc:h2:file:./or-controller-database`|Database connection configuration, if dev mode is enabled, the default is `jdbc:h2:mem:test`.|
|`DATABASE_USERNAME`|`sa`|Database connection username.|
|`DATABASE_PASSWORD`|(empty)|Database connection password.|
|`DATABASE_MIN_POOL_SIZE`|`5`|Minimum number of database connections in the pool.|
|`DATABASE_MAX_POOL_SIZE`|`25`|Maximum number of database connections in the pool.|
|`DATABASE_STATEMENT_CACHE_SIZE`|`20`|SQL prepared statement cache size.|
|`CREATE_DATABASE_SCHEMA`|`false`|Drop/recreate SQL database schema in database on startup, always enabled if dev mode is enabled.|
|`IMPORT_SAMPLE_FLOWS`|`false`|Import test/example data into database on startup, always enabled if dev mode is enabled.|
|`START_SAMPLE_FLOWS`|`true`|If test/example flows have been imported, start them immediately when the application boots.|

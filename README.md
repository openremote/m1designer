# Beta Editor

* Live demo: [http://orc3.christianbauer.name/](http://orc3.christianbauer.name/)

Development
---

* Install JDK 1.8

* Install Android SDK

* Run GWT super dev mode code-server: `./gradlew web:gwtSuperDev`

* Run server: `./gradlew web:serverRun`

* Open [http://localhost:8080/](http://localhost:8080/) in browser

Build archives
---

    ./gradlew clean build

Run Docker container
---

    docker stop orc3
    docker rm orc3
    docker rmi orc3:latest
    docker build -t orc3:latest build/libs/
    docker run -d --name=orc3 -p 8006:8080 -v /etc/localtime:/etc/localtime orc3

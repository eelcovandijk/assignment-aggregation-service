# Running the assignement

prebuild packages in the dist directory.

1 First start the backend-services

``` docker-compose up ```

2.1 run the applications fat jar with a Java 11 runtime:

`java -jar ./dist/assessment-aggration-service-1.0.0-SNAPSHOT-runner.jar`

2.2 or go native with:
(only on Linux)

```./dist/assessment-aggration-service-1.0.0-SNAPSHOT-runner```

2.3 or build the docker image

See ```src/main/docker/Dockerfile.jvm```

2.4 or start in dev mode

```./mvnw compile quarkus:dev```

3 call api 

the aggregator api is available by default with host: `localhost` and port `8082`.
`http://localhost:8082/aggregation?track=121&shipments=4&pricing=AA`

It connects to the backend services on `http://localhost:8080`



# getting-started

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar -DskipTests
```

The application is now runnable using `java -jar target/assessment-aggration-service-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative -DskipTests
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/assessment-aggration-service-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

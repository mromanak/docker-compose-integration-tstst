# Tstst?

it was supposed to be `docker-compose-integration-tests` but i was too lazy to fix it at the outset, and now im trying
to convince myself its funny so i dont feel obligated to fix it now

# docker-compose-integration-tstst

This repository is an exploration of how to run integration tests against an application that has inconvenient external
dependencies (e.g. a SQL database). I wanted this project to meet the following requirements:

* **The user should not need to manually install a long list of required tools beforehand.** I want to address the pain
  point where a developer is asked to maintain a complicated matrix of software tools (often at a specific version) per
  project. Ideally, this project will only require up-front installation of Java and Docker.
* **The integration tests should not use dependency mocking.** I want to address the pain point where a suite of unit
  tests using dependency mocking is treated as a replacement for a suite of integration tests. Ideally, this project
  will allow a user to verify the behavior of the application against a real Postgres database.
* **The integration tests should be runnable with a single command.** I want to address the pain point where the
  developer must perform time-consuming or arcane setup before integration tests can be run. Ideally, this project will
  expose a single Gradle task which does all of the following:
    * Ensure the Docker image for the application is built and up-to-date
    * Start the application and all of its external dependencies in Docker
    * Run the full suite of integration tests against the application
    * Reset the database to its initial state between tests to ensure proper test isolation
    * Tear down any Docker containers created in the setup step
* **The project should use Gradle as its build tool.** I want to address the pain point of using Gradle. I have
  maintained several Java projects that use different build tools, and Gradle is the one that has caused the most
  headaches. Ideally, implementing this concept in the build tool I find most difficult to work with will provide a
  basic framework that can be copied in build tools thast I find easier to use.

All commands in this README were tested on MacOS because that's what I, personally, need. I suspect the commands will
work on Linux as-is (as-are?) and that the Windows equivalents would not be difficult to formulate. However, I haven't
tested those suspicions, and I don't plan to at the moment.

# Prerequisites

You will need the following software installed to run this project:

* `java` (version 11+)
* `docker`
* `docker-compose`

# Running the Integration Tests

To run all tests in the project, including the integration tests, simply run Gradle's conventional `test` task, like so:

```shell
./gradlew test
```

To ensure complete test isolation, each test in `ItemControllerDockerIntegrationTest` creates and tears down its own
Docker stack. Because this is fairly heavyweight behavior, I've created two alternate Gradle tasks to allow unit tests
and integration tasks to be run independently:

* The `unitTest` task only runs test classes and methods annotated with `@Category(UnitTest.class)`. (Currently, there
  are unit tests, but that would be the way to do it if there were.)
* The `dockerIntegrationTest` task only runs test classes and methods annotated with
  `@Category(DockerIntegrationTest.class)`

# How it Actually Works

## Gradle Tasks

Most of the Docker magic in this project is handled by Gradle, using
[gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin). This plugin is imported on line 2 of
[build.gradle](build.gradle#L2). The only slightly awkward aspect of the plugin is the need to manually import its
custom task types, which is done on [lines 32-33](build.gradle#L32).

### prepareDockerfile

Lines 35-41 of [build.gradle](build.gradle#L35) define a
[Dockerfile task] (https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/Dockerfile.html)
that uses [gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin) to create a Dockerfile at
`build/docker/Dockerfile`. The Dockerfile has the following instructions:

* Use `openjdk:slim` as the base image for our Docker image
* Copy the Spring Boot jar from `build/docker/` into the `/app/` directory on the image
* Expose the port (8080) that the API will run on to the host machine
* On startup, run the Spring Boot jar

### prepareDockerCode

Lines 43-48 of [build.gradle](build.gradle#L43) define a
[Copy task](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html) that builds the project's Spring Boot
jar and copies it to `build/docker/docker-compose-integration-tstst.jar`. When Gradle runs the Dockerfile during the
`buildDockerImage` task, it will use `build/docker/` as its root directory, and will throw an error if any Dockerfile
commands try to use relative paths to escape from that root directory. Therefore, this setup task is necessary to ensure
that all the code needed to build the Docker Image exists in the correct place.

### buildDockerImage

Lines 43-48 of [build.gradle](build.gradle#L43) define a
[DockerBuildImage](https://bmuschko.github.io/gradle-docker-plugin/api/com/bmuschko/gradle/docker/tasks/image/DockerBuildImage.html)
that depends on the `prepareDockerfile` and `prepareDockerCode` tasks to build the Docker image and push it to the local
repository under the name `dcit`. The image receives two tags: `latest` and the version number that is set on
[line 10](build.gradle#L10).

### test

Lines 57-59 of [build.gradle](build.gradle#L57) extend the default `test` task to depend on `buildDockerImage`.

### unitTest

Lines 61-65 of [build.gradle](build.gradle#L61) create a new test task that only runs test classes and methods annotated
with `@Category(UnitTest.class)`.

### dockerIntegrationTest

Lines 67-72 of [build.gradle](build.gradle#67) create a new test task that depends on `buildDockerImage` and only runs
test classes and methods annotated with `@Category(DockerIntegrationTest.class)`.

## JUnit Setup

Once the Docker image is built, the work of standing up the Docker resources is handled by
[docker-compose-rule](https://github.com/palantir/docker-compose-rule). Note that `docker-compose-rule` rule is written
for the JUnit4 API (`org.junit`), but Spring Boot includes dependencies on the JUnit5 API (`org.junit.jupiter.api`).
Some care is necessary to avoid mixing and matching the two APIs when writing tests. It would be ideal to make a
JUnit5 `Extension` that wraps `docker-compose-rule` and try to remove the JUnit4 API from the project, but that's not
where my focus is at the moment.

Looking at
[ItemControllerDockerIntegrationTest](java/com/mromanak/dockercomposeintegrationtstst/controller/ItemControllerDockerIntegrationTest.java)
, the setup to get the Docker containers running is as follows:

* [Lines 31-38]([ItemControllerDockerIntegrationTest](java/com/mromanak/dockercomposeintegrationtstst/controller/ItemControllerDockerIntegrationTest.java#31)
  configure the local `DockerMachine` with some environment variables. `DockerMachine` does not a method to set an env
  file, so each environment variable must be set individually.
* [Lines 41-47]([ItemControllerDockerIntegrationTest](java/com/mromanak/dockercomposeintegrationtstst/controller/ItemControllerDockerIntegrationTest.java#41)
  define the `DockerComposeRule` that will handle standing up the Docker resources. The rule will:
    * Use the `DockerMachine` defined above.
    * Create a Docker Compose stack using.
      [dockerIntegrationTestStack.yml](src/test/resources/dockerIntegrationTestStack.yml) as its template.
    * Wait for Postgres and the Spring Boot API to be healthy before running the test methods. (See
      [PostgresHealthChecks](src/testjava/com/mromanak/dockercomposeintegrationtstst/utils/health/PostgresHealthChecks.java)
      and
      [SpringActuatorHealthChecks](src/test/java/com/mromanak/dockercomposeintegrationtstst/utils/health/SpringActuatorHealthChecks.java)
      for the actual checks used to accomplish this.)
* The use of the `@Rule` annotation on
  [line 40](java/com/mromanak/dockercomposeintegrationtstst/controller/ItemControllerDockerIntegrationTest.java#40)
  indicates that the Docker compose rule should be applied for each test method. This is good for test isolation, since
  each test will start from a completely blank Postgres database, however it also introduces a lot of time spent waiting
  for Docker resources to be created and destroyed. An integration test that only needed the Docker resources to be
  applied once for the entire test class could replace the `@Rule` annotation with `@ClassRule`.

## Running a Test Stack Application Manually

To build the Docker image and stand up a test application stack manually, run the following commands:

```shell
./gradlew clean buildDockerImage 
docker-compose -f docker/testStack.yml --env-file docker/test.env up
```

This will stand up three Docker containers:

* [Postgres](https://hub.docker.com/_/postgres) (port `5432`)
* [Adminer](https://hub.docker.com/_/adminer) (port `8081`)
* The Spring Boot application (port `8080`)

Note that in [test.env](docker/test.env), `DDL_MODE` (and by proxy `spring.jpa.hibernate.ddl-auto`, as defined in
[application.yml](src/main/resources/application.yml)) is set to `create`. This will cause the Spring Boot application
to create the SQL tables it needs on startup or to fail if it cannot do so. This is useful behavior for test deployments
where Postgres is constantly being spun up and down alongside the Spring Boot application. In a production
deployment where Postgres is persistent, a more reasonable value of `DDL_MODE` would be `validate`. That will check that
the tables exist and have the expected schema rather than trying to create them every time.

### Checking the Test Stack's Database

Navigate to [http://localhost:8081/](http://localhost:8081/) in a browser to view the Adminer GUI. Connect Adminer to
the Postgres server running in Docker by using the following settings:

| Field | Value |
|-------|-------|
| **System** | `PostgreSQL` |
| **Server** | `host.docker.internal` |
| **Username** | The value of `POSTGRES_USER` set in [test.env](docker/test.env) |
| **Password** | The value of `POSTGRES_PASSWORD` set in [test.env](docker/test.env) |
| **Database** | The value of `POSTGRES_DB` set in [test.env](docker/test.env) |

### Making API Requests to the Test Stack

The Spring Boot application provides an extremely simplified REST API that performs CRUD operations on
[Items](java/com/mromanak/dockercomposeintegrationtstst/model/jpa/Item.java).

The following `curl` command will create an Item with ID 123 (and parrot back the current state of that Item in the
response):

```shell
curl --request POST 'http://localhost:8080/repository/item/123' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Perfectly Generic Object",
    "description": "A featureless cube."
}'
```

Additionally, after running the above command:

* A GET request to `/repository/item/123` will return the current state of the Item
* A DELETE request to `/repository/item/123` will delete the item
* All changes made by the API will be reflected to the `item` table in Postgres, which can be verified using Adminer as
  described above.

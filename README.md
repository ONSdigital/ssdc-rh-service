# ssdc-rh-service

## Introduction

This service stores Case & UAC update messages from PubSub into Firestore. It has an endpoint that
takes a UAC hash and provides an EQLaunch Token.

The firestore allows it to be 'super fast' at reading data out for fast launching.

## Building

To run all the tests, checks, formatting, and build the image:

```shell
make build
```

To skip the tests and just run the checks and build the docker image:

```shell
make build-no-test
```

To just run the auto formatting:

```shell
make format
```

## Locally Running and Debugging

The easiest way to run the RH service is dockerised, with our full stack services locally by following the README
in [ssdc-rm-docker-dev](https://github.com/ONSdigital/ssdc-rm-docker-dev).

If you need to run the service locally in the IDE or from the command line (e.g. with local code changes or to debug),
use docker-dev to start the RM services, and
then remove the `rh-service` container (so it does not conflict) with `docker rm -f rh-service`.

Some environment configuration is required to point the RH service at the docker-dev backing services.

### In Intellij IDEA

A [run configuration file](.run/Run%20Application%20(docker-dev).run.xml) is provided for Intellij to configure it to
use the docker-dev dependencies, simply select this run configuration named `Run Application (docker-dev)`, and you
should be able to run or debug the service within Intellij.

### Anywhere Else

Manually set these variables in your run environment to point the service at the docker-dev dependencies

```shell
EXCEPTIONMANAGER_CONNECTION_HOST=localhost
EXCEPTIONMANAGER_CONNECTION_PORT=8666
FIRESTORE_EMULATOR_HOST=localhost:8542
firestore.project-id=our-project
spring.cloud.gcp.pubsub.emulator-host=localhost:8538
spring.cloud.gcp.pubsub.project-id=our-project
```
# Reactive Crypto Market Making Demo

This project demonstrates a reactive approach to cryptocurrency market making. It consists of several modules including `grpc-tools`, `price-capture`, `order-service`, and `observer-ui-client`.

## Prerequisites

- Java 21
- Maven
- Docker

## Building the Project

To compile the project, run the following command:
```sh
mvn clean install
```
## Running the Quarkus apps in Docker
```sh
mvn quarkus:image-build -pl price-capture,order-service,observer-ui-client
```

```sh
docker compose up
```
http://localhost:8080

## Running the Quarkus apps locally
```sh
docker compose -f 'docker-compose.dev.yaml' up
```
http://localhost:8080


## Service interaction sequence diagram

![Squence diagram](docs/service-interaction-sequence.png)

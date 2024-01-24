# Reactive Quarkus Crypto Capture Demo

This is Quarkus ecosystem exploration project utilizing reactive programming, reactive Panache, gRPC, Kafka and Quarkus. 

It consists of several modules including:
- `grpc-tools` - shared dependency for gRPC protos and converters 
- `price-capture` - captures price data from cryptocurrency exchange and publishes it to Kafka
- `order-service` - gRPC server, consumes price data from Kafka and persists Orders to a postgres database 
- `observer-ui-client` - web UI, exposes a REST API to query Orders and stream real time updates via Server Sent Events, gRPC client

## Prerequisites
- Java 21
- Maven
- Docker

## Build and run 
To compile the project, run the following command:
```sh
mvn clean install
```

### Running the applications locally
Launch Redpanda Kafka cluster with Console UI
```sh
docker compose -f 'docker-compose.dev.yaml' up
```
Launch Quarkus instances in dev mode on host machine
```sh
mvn quarkus:dev -pl price-capture
mvn quarkus:dev -pl order-service
mvn quarkus:dev -pl observer-ui-client
```
Experimental! to launch all at once:
```sh
mvn -T 3 quarkus:dev -pl price-capture,order-service,observer-ui-client
```
Visit http://localhost:8080 to see web page hosted by observer-ui-client

### Running all applications inside Docker
```sh
mvn install -Dquarkus.container-image.build=true -DskipTests
```
```sh
docker compose up
``` 

Visit http://localhost:8080 to see web page hosted by observer-ui-client

## Service interaction sequence diagram

![Squence diagram](docs/service-interaction-sequence.png)

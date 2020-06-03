
ARGOS Supply Chain Notary Parent Project [![Build Status](https://cloud.drone.io/api/badges/argosnotary/argos-parent/status.svg)](https://cloud.drone.io/argosnotary/argos-parent) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=argosnotary_argos-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=argosnotary_argos-parent)
============
 
## Documentation
 
 All documentation can be found on our [website](https://argosnotary.github.io/docs/00_overview/10_overview).
 
## Contributing

See the [contribution guidelines](https://argosnotary.github.io/docs/80_contributing/10_contributing)

## Code of Conduct

See the [Code of Conduct](https://argosnotary.github.io/docs/80_contributing/20_code_of_conduct)

## Modules
-   argos4j
-   argos-docker
-   argos-domain
-   argos-jenkins-base
-   argos-jenkins-plugin
-   argos-service
-   argos-service-adapter-in-rest
-   argos-service-adapter-out-mongodb
-   argos-service-api
-   argos-service-domain
-   argos-test
   
 
### argos4j
Java client library for creating,signing and sending link files to the
Argos service.

### argos-docker
Docker compose file and Docker files used for running the Argos service
locally and in the drone build pipeline.

### argos-domain
Core domain entities shared between the argos4j and the argos service
modules.

### argos-jenkins-base
Jenkins docker base image used in argos-docker

### argos-jenkins-plugin
Plugin for jenkins that uses argos4j library to post signed link files
with each build step to the argos service.

### argos-service
[Spring Boot](https://spring.io/projects/spring-boot) Java service to
expose the REST API

### argos-service-adapter-in-rest
Incoming adapter implementing the
[open api](https://swagger.io/specification/) REST specification. This
api is defined in the argos-service-api module. 
( See [architecture paragraph](#architecture) )

### argos-service-adapter-out-mongodb
Outgoing adapter using mongo db to implement the repository interfaces
defined in the argos-service-domain module. ( See [architecture
paragraph](#architecture) )
### argos-service-api
[Open api](https://swagger.io/specification/) specification for the
Argos Service endpoints.

### argos-service-domain
Domain entities and interfaces specifically for the argos service.

### argos-test

Integration test module to run integration tests locally or as step in a
drone pipeline.


## How to run
See [developer documentation](docs/DEVELOPER.md)

See [contributing to Argos Notary](https://argosnotary.github.io/docs/80_contributing/10_contributing)


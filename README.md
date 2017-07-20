# zuul-microservices

Learning how to use Spring Cloud and Netflix Zuul.

## Requirements
* Java 8
* Maven 3
* Docker

## Usage
Build and start a MySQL database, a User Microservice, and a Zuul Service Docker container

    $ ./scripts/deploy.sh

Use `curl` or [Postman](https://www.getpostman.com/) to interact with the API

Retrieve all users

    $ curl http://localhost:8080/users/
    
Create a user

    $ curl \
      -H "Content-Type: application/json" \
      -X POST -d '{"firstName":"ben","lastName":"robinson"}' \
      http://localhost:8080/users/new

Retrieve a single user

    $ curl http://localhost:8080/users/{id}
    
Delete all users

    $ curl -X DELETE http://localhost:8080/users/delete

## What happens
We are reverse-proxying traffic on `localhost:8080` to our microservice on `localhost:9090`. Zuul logs any incoming HTTP requests
which can be seen while running `docker logs -f zuul-service`. The request is forwarded and processed by the microservice 
and then we add a custom header to the HTTP response called "X-BMROBIN" which has the followed of a random integer.

While this example is admittedly extremely rudimentary it is a simple demonstration of the capabilities that could
be leveraged by Zuul to add advanced pre and post-processing to HTTP requests and responses, as well as load balancing and routing.

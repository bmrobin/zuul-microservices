#!/usr/bin/env bash

mvn clean package
docker-compose stop
docker-compose rm -f
docker images -q spring-zuul/zuul-service | xargs docker rmi
docker images -q spring-zuul/user-service | xargs docker rmi
docker-compose up -d db
sleep 15
docker-compose up -d

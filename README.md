# Elasticsearch example

Demo project for clothes smart search

## Using technologies 
 - Java
 - Spring Boot 
 - WebFlux
 - Elasticsearch
 - Test Containers

## Dependencies
   Required starting elasticsearch on port 9200

## Usage
 - start elasticsearch docker image locally: `docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.12.0
   `
 - build: `./gradlew clean assemble`
 - tests: `./gradlew clean test`
 - run: `./gradlew bootRun` or `./gradlew clean assemble && cd build/libs && java -jar elasticsearch-example-{VERSION}.jar`
 - docker: `docker build -t elasticsearch-example . && docker run -p 8080:8080 elasticsearch-example`

## Links
[index settings](src/main/resources/elasticsearch/clothesIndexSettings.json)

[repository](https://github.com/1000001rtem/elasticsearch-example)

## Contacts
Author: Eremin Artem

Site: [1000001rtem.com](http://1000001rtem.com)

Email: 1000001rtem@gmail.com
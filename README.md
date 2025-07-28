# Master Project - Lukas Freiseisen

## How to Install:
Prerequisites:
* docker installed

Run ```docker compose up --detach``` in the repository root, and you are good to go
OR use ```quickstart.sh -a``` on unix systems
OR use ```quickstart.bat -a``` on windows

Make sure everything is running correctly by executing ```docker ps --format 'table {{.ID}}\t{{.Image}}\t{{.Names}}'```.
You should see something like this:
```
CONTAINER ID   IMAGE                                      NAMES
4336763d8346   master-project-dashboard                   master-project-dashboard-1
881e48ad205d   master-project-model-generator             master-project-model-generator-1
4be9f189e1bd   master-project-db-service                  master-project-db-service-1
1070caef068d   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-9
ecf3db748e53   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-7
e8c152d0b413   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-1
6159c053c276   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-2
accbcfb9b181   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-4
91089768513a   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-8
5effffa62bf3   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-5
4d3e9d08ae8a   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-6
bf415d25033c   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-3
2ce23535d737   master-project-otel-to-declare-converter   master-project-otel-to-declare-converter-10
5997adad9542   postgres:latest                            master-project-database-1
7319d2694fc2   rabbitmq:3-management-alpine               master-project-rabbitmq-server-1
```

You can change the number of worker threads by changing the value of environment variable ```NR_THREADS``` of the service ```model-generator``` 

<img src=".readme_resources/screenshot_model-generator_docker_compose.png" alt="sc_model_gen_dc" width="100%">

## How to Populate the Database
The db-initializer will automatically populate the database, if configured correctly.
Here is an example configuration:

```yaml
  db-initializer:
    build:
      context: java
      dockerfile: ./Dockerfile
      args:
        ARTIFACT_ID: db-initializer
    restart: on-failure
    environment:
      FILE_PATH_DYNATRACE: "/test-data/traces_spans.zip"
      FILE_PATH_JAEGER: "/test-data/traces-jaeger.zip"
      FILE_PATH_TRAIN_TICKET_SAMPLED: >
        /test-data/train-ticket-sample-00.tar.gz,
        /test-data/train-ticket-sample-01.tar.gz,
        /test-data/train-ticket-sample-02.tar.gz,
        /test-data/train-ticket-sample-03.tar.gz
    volumes:
      - ./test-data:/test-data
```

Run "docker compose -f docker-compose.db-initializer.yml up --detach" in the repository root.

Depending on the size of your data set, this may take a while.

If you run into a persistence error, make sure that the column constraintTemplate of the table declare has the type varchar(500), for some reason, this is initialized with varchar(250), although it is configured as varchar(500) in the jpa class.

<img src=".readme_resources/screenshot_declare.png" alt="declare" width="60%"><img src=".readme_resources/screenshot_declare_jpa.png" alt="declare_jpa" width="40%">

## How to Use
When you have a system running with a populated database.

TODO!

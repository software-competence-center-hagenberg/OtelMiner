# OTel Miner

OTel Miner is an open-source framework to incrementally discover Prob Declare models from OpenTelemetry (OTel) traces.<br>
This project is the product of the following master's thesis: **TODO: add link**

---

## Before You Get Started

You should be at least familiar with docker and OpenTelemetry and have basic knowledge on process mining. Basic knowledge on Linear Temporal Logic and Prob Declare is of advantage. 

---

## Background Information

1. OpenTelementry (https://opentelemetry.io)
2. Process Mining (https://processmining.org/, https://link.springer.com/chapter/10.1007/978-3-642-28108-2_19)
3. Linear Temporal Logic (https://en.wikipedia.org/wiki/Linear_temporal_logic)
4. Declare (https://pure.tue.nl/ws/portalfiles/portal/2815085/200811543.pdf)
5. Prob Declare (https://www.sciencedirect.com/science/article/abs/pii/S0306437922000345)

---

## Related Work

1. Declare4Py (https://github.com/ivanDonadello/Declare4Py/tree/main)
2. Declare Miner (https://ais.win.tue.nl/declare/declare-miner/index.html)
3. Rule Miner (https://rulemining.org/)
3. Prom (https://promtools.org/)

---

## Technology Used

1. Docker (https://www.docker.com/)
2. RabbitMQ (https://rabbitmq.com)
3. Spring Boot (https://spring.io/projects/spring-boot)
4. OCaml (https://ocaml.org/)
5. Dune (https://dune.build/)
5. Next.js (https://nextjs.org/)
6. PostgreSQL (https://www.postgresql.org/)

---

## Architecture

The system consists of the following parts:
- Dashboard: The frontend UI written TypeScript with the framework Next.js
- Model-Generator: The core service that coordinates the communication between Dashboard and DB-Service and the model generation.
- OTEL-to-DECLARE-Converter: The worker services that discover Declare models from OTel traces. They communicate with Model-Generator via AMQP.
- DB-Service: A database access service, that defines the data model via JPA, communicates with the Database via JDBC, and exposes REST APIs to the Model-Generator to retrieve and persist data.
- DB-Initializer: A helper service used for populating the database, its configuration is described in "How to Populate Database"
- Database: A Postgres Database
- RabbitMQ-Server: The Message Broker used for AMQP communication between Model-Generator and the OTEL-to-DECLARE-Converter instances.

![architecture.png](.readme_resources/architecture.png)

The next sequence diagram shows the general sequence of generating a prob declare model. 
The user starts the generation from the Dashboard, which makes a REST call to the Model-Generator.
The Model-Generator creates an empty Prob Declare Model and then returns the ID to the DASHBOARD. 
Then the Dashboard starts polling the model.

The Model-Generator then initializes the Trace Cache Manager, and spawns n generation threads (NR_THREADS).

The Trace Cache Manager starts polling the persisted traces page-wise from the DB-Service with a default page size of 100.

Meanwhile, the model generator threads wait for the Trace Cache Manager to give them a trace. 
Once the trace is retrieved, the processing of said trace is planned via a CompletableFuture and a Runnable. 
Then the trace is put in the AMQP queue of its corresponding Trace Format. 
Then the thread goes idle.

The OTEL-to-DECLARE-Converter receives the trace, converts it to a DECLARE model and publishes the result in the result-queue.

The result is picked up by the RabbitMQ Listener in the Model-Generator, which finds the corresponding CompletableFuture and completes it with the DECLARE model as a result.

This is where the generation thread that planned the future wakes up again, processes the result and hands it over to the model updater thread. 
Then the generation thread checks if there are traces left to be processed and restarts its cycle if there are or ends if there are no traces left.

Meanwhile, the model updater thread updates the prob declare model by adding new DECLARE constraints and calculating their probabilities and recalculating the probabilities of all existing constraints.

![Sequence_Diagram_Model_Generation.png](.readme_resources/Sequence_Diagram_Model_Generation.png)

This diagram shows the detailed process flow of the model generation in the Model-Generator with all the different threads.

![Model-Generator-sequence.png](.readme_resources/Model-Generator-sequence.png)

This diagram shows the general process flow in the OTEL-to-DECLARE-Converter.

![flowchart-otel-to-declare-converter.png](.readme_resources/flowchart-otel-to-declare-converter.png)

### Data Model

![data-model-erd.png](.readme_resources/data-model-erd.png)

---

## How to Install:

Prerequisites:
* docker installed

Run `docker compose up --detach` in the repository root, and you are good to go
OR use `quickstart.sh -a` on unix systems
OR use `quickstart.bat -a` on windows

Make sure everything is running correctly by executing `docker ps --format 'table {{.ID}}\t{{.Image}}\t{{.Names}}'`.
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

You can change the number of worker threads by changing the value of environment variable `NR_THREADS` of the service `model-generator` 

<img src=".readme_resources/screenshot_model-generator_docker_compose.png" alt="sc_model_gen_dc" width="100%">

---

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

### Supported Trace Formats

#### OTEL

#### JAEGER

#### DYNATRACE

#### RESOURCE_SPANS

---

## How to Use

### How to navigate the data

When you have a system running with a populated database, visit http://localhost:3000 in your browser of choice to access the dashboard.
You should see something like the screenshot below. This is the data overview table. It shows the persisted source files with the total number of traces and the number of nodes a trace may contain.
![dashboard_overview](.readme_resources/screenshot_dashboard_overview.png)
When selecting a row, the trace details table is opened below and the `GENERATE PB MODEL` button is enabled.
![dashboard_trace-details](.readme_resources/screenshot_dashboard_trace_details.png)
The trace details table, when clicked on a row, opens a view of the parsed JSON of the raw trace on the lower left. There, a button `GENERATE MDOEL` exists, when clicked on, it sends the trace to the backend to generate a DECLARE model from the trace. Next the result is polled. When received, it is displayed as an accordeon in the lower right. This accordion element groups the constraints by type. E.g.: CHAIN_SUCCESSION(A,B) ([](A <-> OB)). 
![dashboard_trace_declare](.readme_resources/screenshot_trace_declare.png)

### How to Generate a PROB DECLARE Model

When clicking the button `GENERATE PB MODEL`, the prob declare view is opened. Here you can see a button `SHOW EXISTING MODELS`, which retrieves the existing models from the backend when clicked and displays them in the table below.
Then there are the Buttons `START`, `PAUSE`, `RESUME`, `ABORT`, `RESET`, and `CLOSE`. These buttons are there to control the model generation, to reset the state and to close the view and return to the data overview.
Below them are the buttons  `SHOW GENERATION OPTIONS`, `SHOW STATISTICS`, `SHOW SEEDING OPTIONS`, and `SHOW RAW DATA`, which will be explained in the latter.

![dashboard_pb-declare](.readme_resources/dashboard_pb-declare.png)

To start the generation with the default options, press start, the model will appear as shown in the screenshot below. On the left side, the probability of each constraint is displayed, and on the right side the DECLARE constraint.

![dashboard_model-gen-started](.readme_resources/screenshot_dashboard_model_gen_started.png)


The button `SHOW STATISTICS` enables the view of two line charts and a bar chart. The first line chart shows all the traces by probability and the second one filters out all the probabilities below a configurable threshold. The bar chat shows the spread of probability ranges ([0.0,0.1[, [0.1,0.2[, [0.2,0.3[, [0.3,0.4[, [0.4,0.5[, [0.5,0.6[, [0.6,0.7[, [0.7,0.8[, [0.8,0.9[, [0.9,1[) and the DECLARE constraints which have a probility of 1..
![dashboard_statistics](.readme_resources/screenshot_dashboard_statistics.png)
![dashboard_statistics_1](.readme_resources/screenshot_dashboard_statistics-1.png)

There are also advanced generation options and seeding options, make sure that you take a look at the architecture and the data model first before you have a look at them.

**If you are not familiar with the architecture and the underlying data model, skip this part for now**<br>

#### Generation Options

`SHOW GENERATION OPTIONS`, reveals all the options the user has for generation. There, the paging options determine with which db page of the table trace, the model generation will start
![dashboard_generation-options](.readme_resources/screenshot_dashboard_generation_options.png)

#### Seeding

---

## Known Bugs

---

## Acknowledgements

---

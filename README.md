# Master Project - Lukas Freiseisen

## install:
Prerequisites:
* docker installed

run "docker compose up --detach" in the repository root, and you are good to go


## how to generate traces
run "docker compose start otel-generator" 

## how to run the preprocessor
* run "docker compose -f docker-compose.preprocessor.yml up --detach

Note: only run whole file the first time, later omit db-initializer
If troubles occur, run in the following order one after another:
database -> db-initializer -> rabbitmq-server -> db-service -> model-generator -> otel-preprocessor -> frontend-preprocessor

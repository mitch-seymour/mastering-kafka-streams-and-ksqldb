# About
This code corresponds with Chapter 2 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This includes two "Hello, world" style applications: one implemented using the high-level DSL, and the other implemented using the Processor API.

[book]: https://www.kafka-streams-book.com/

# Running Locally
The only dependency for running these examples is [Docker Compose][docker].

[docker]: https://docs.docker.com/compose/install/

Once Docker Compose is installed, you can start the local Kafka cluster using the following command:

```sh
$ docker-compose up
```

Now, follow either the **DSL example** or **Processor API example** instructions below, depending on which version of the demo you want to run.

## DSL example

You can run the high-level DSL example with the following command:
```sh
$ ./gradlew runDSL --info
```

Once the dependencies are downloaded and the application is running (this may take a few minutes the first time you run the app, but will be much faster during subsequent runs), following the instructions under the __Producing Test Data__ section at the bottom of this README.

## Processor API example

You can run the low-level Processor API example with the following command:
```sh
$ ./gradlew runProcessorAPI --info
```

Once the dependencies are downloaded and the application is running (this may take a few minutes the first time you run the app, but will be much faster during subsequent runs), following the instructions under the __Producing Test Data__ section below.

# Producing Test Data
Once the Kafka Streams application is running (either the DSL or Processor API version), open a new shell tab and produce some data to the source topic (`users`).

```sh
$ docker-compose exec kafka bash

$ kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic users
```

This will drop you in a prompt:

```sh
>
```

Now, type a few words, followed by `<ENTER>`.

```sh
>world
>izzy
```

You will see the following output if running the DSL example:
```sh
(DSL) Hello, world
(DSL) Hello, izzy
```

or slightly different output if running the Processor API example:
```sh
(Processor API) Hello, world
(Processor API) Hello, izzy
```

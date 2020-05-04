# Running Locally
The only dependency for running these examples is [Docker][docker]. Everything else is executed within a sandbox image.

[docker]: https://www.docker.com/products/docker-desktop

Run the following commands to 1) mount the code inside of the sandbox and 2) pre-create the `hello-world` topic (this is the topic we will produce test data to shortly).

```sh
# you must be in this directory since we're mounting the app code inside of a container
$ cd /path/to/mastering-kafka-streams-and-ksqldb/chapter-2/hello-streams/

# mount the code into a Docker container and run
$ docker run --name ch2-sandbox \
  -v "$(pwd)":/app \
  -w /app \
  -ti magicalpipelines/cp-sandbox:latest  bash -c "\
  confluent local start kafka; \
  ./scripts/create-topics.sh; \
  bash"
```

Now, follow either the **DSL example** or **Processor API example** instructions below, depending on which version of the demo you want to run.

## DSL example

Once you're logged into the sandbox container, you can run the high-level DSL example with the following command:
```sh
$ ./gradlew run --info
```

Once the dependencies are downloaded and the application is running, following the instructions under the __Producing Test Data__ section at the bottom of this README.

## Processor API example

Once you're logged into the sandbox container, you can run the low-level Processor API example with the following command:
```sh
$ ./gradlew runProcessorApi --info
```

Once the dependencies are downloaded and the application is running, following the instructions under the __Producing Test Data__ section below.

# Producing Test Data
Once the Kafka Streams application is running (either the DSL or Processor API version), produce some data to the source topic (`hello-world`) using the `kafka-console-producer` included in the sandbox.

```sh
docker exec -ti ch2-sandbox bash -c "\
  kafka-console-producer \
  --broker-list localhost:9092 \
  --topic hello-world"
```

This will drop you in a prompt:

```sh
>
```

Now, type a few words, followed by `<ENTER>`.

```sh
>world
>moon
```

You will see the following output if running the DSL example:
```sh
(DSL) Hello, world
(DSL) Hello, moon
```

or slightly different output if running the Processor API example:
```sh
(Processor API) Hello, world
(Processor API) Hello, moon
```

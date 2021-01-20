# Advanced State Management
This code corresponds with Chapter 6 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. The code examples here make up a small percentage of the topics we actually cover in this chapter so I highly recommend reading that chapter for a more complete understanding of advanced state management.

[book]: https://www.kafka-streams-book.com/

# Running Locally
The only dependency for running these examples is [Docker Compose][docker].

[docker]: https://docs.docker.com/compose/install/

Once Docker Compose is installed, you can start the local Kafka cluster using the following command:

```sh
$ docker-compose up
```

This tutorial includes a few different topologies. The brief description for each topology, including the command for running the topology, is shown below:

- [An example topology](chapter-06/advanced-state-management/src/main/java/com/magicalpipelines/TopicConfigsExample.java) that applies custom configurations to changelog topics

  ```sh
  ./gradlew runTopicConfigsExample
  ```
  
- [An example topology](chapter-06/advanced-state-management/src/main/java/com/magicalpipelines/LruFixedSizedStoreExample.java) that uses a fixed-size LRU cache as its state store:

  ```sh
  ./gradlew runLruFixedSizedStoreExample
  ```
  
- [An example topology](chapter-06/advanced-state-management/src/main/java/com/magicalpipelines/TombstoneExample.java) that processes tombstones
  
  ```sh
  ./gradlew runTombstoneExample
  ```

# Producing Test Data
Once your application is running, you can produce some test data using the following command:

```sh
./scripts/produce-test-data.sh
```

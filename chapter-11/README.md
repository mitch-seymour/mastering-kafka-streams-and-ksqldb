# Intermediate Stream Processing with ksqlDB
This code corresponds with Chapter 11 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial picks up where the Chapter 10 tutorial leaves off. We can continue building our Netflix change tracking application by exploring more advanced topics, including:

- Using joins to combine and enrich data
- Performing aggregations
- Executing pull queries (i.e. point lookups) against materialized views using the CLI
- Working with built-in ksqlDB functions (scalar, aggregate, and table functions)
- Creating user-defined functions using Java

[book]: https://www.kafka-streams-book.com/

# Building the Custom ksqlDB Functions
Before you start the ksqlDB server (see the _Running Locally_ section later in this README), you should first build any of the custom functions you want to experiment with so that the UDF JARs can be mounted into the ksqlDB server container (which is defined in the _docker-compose.yml_ file). The following custom ksqlDB functions are currently included in our examples:

## Custom UDF (remove_stop_words)
The custom function in the [udf/](udf/) directory is what we built in Chapter 11. It's purpose is to remove stop words from a string of text. To build this UDF, run the following commands:

```sh
$ cd udf/
$ ./gradlew build --info
```

# Running Locally
We're deploying the following components with Docker compose:

- Zookeeper
- Kafka
- ksqlDB server (with Kafka Connect running in embedded mode)
- ksqlDB CLI
- Schema Registry
- Postgres
- Elasticsearch


Feel free to checkout the following config files:

- [ksqlDB Server config][ksqldb-server-config]
- [ksqlDB CLI config][ksqldb-cli-config]


[ksqldb-server-config]: files/ksqldb-server/server.properties
[ksqldb-cli-config]: files/ksqldb-cli/cli.properties

Once you're ready to start everything, run the following command:

```sh
$ docker-compose up -d
```

Once the services are running, open another tab and log into the ksqlDB CLI using the following command:

```sh
$ docker-compose exec ksqldb-cli \
    ksql http://ksqldb-server:8088 \
     --config-file /etc/ksqldb-cli/cli.properties
```

If you see a `Could not connect to the server` error in the CLI, wait a few seconds and try again. ksqlDB can take several seconds to start.

Since this tutorial continues where Chapter 10 left off, we need to initialize the topics, streams, and tables from the previous chapter.. For your convenience, [the relevant SQL statements][sql] have been mounted into the image, so you can use ksqlDB's `RUN SCRIPT` statement to execute them.

```
ksql> RUN SCRIPT '/etc/sql/init.sql' ;
```

[sql]: files/sql/init.sql


Now, you can run each of the queries mentioned in the book from the CLI. Also, the entire set of queries needed for this chapter have been saved to the following file:

- [files/sql/all.sql](files/sql/all.sql)


Once you're finished, tear everything down using the following command:

```sh
docker-compose down
```


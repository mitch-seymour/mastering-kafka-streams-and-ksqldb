# Stream Processing Basics with ksqlDB
This code corresponds with Chapter 10 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial shows how to perform basic stream processing tasks with ksqlDB, including:

- Creating streams and tables
- Filtering data using simple boolean conditions, wildcards, and range filters
- Reshaping data by flattening complex or nested structures
- Using projection to select a subset of the available fields
- Use conditional expressions to handle NULL values
- Creating derived streams and tables, and writing the results back to Kafka

[book]: https://www.kafka-streams-book.com/

# Running Locally
We're deploying the following components with Docker compose:

- Zookeeper
- Kafka
- ksqlDB server (with Kafka Connect running in embedded mode)
- ksqlDB CLI
- Schema Registry


Feel free to checkout the following config files:

- [ksqlDB Server config][ksqldb-server-config]
- [ksqlDB CLI config][ksqldb-cli-config]

Once you're ready to start everything, run the following command:

[script]: files/ksqldb-server/run.sh

```sh
$ docker-compose up -d
```

[ksqldb-server-config]: files/ksqldb-server/server.properties
[ksqldb-cli-config]: files/ksqldb-cli/cli.properties
[connect-config]: files/ksqldb-server/connect.properties

Once the services are running, open another tab and log into the ksqlDB CLI using the following command:

```sh
$ docker-compose exec ksqldb-cli  ksql http://ksqldb-server:8088
```

If you see a `Could not connect to the server` error in the CLI, wait a few seconds and try again. ksqlDB can take several seconds to start.

Now, you can run each of the queries mentioned in the book from the CLI. Also, the entire set of queries needed for this chapter have been saved to the following file:

- [files/sql/all.sql](files/sql/all.sql)

If you'd like to run all of the queries in the above file, simply execute the following statement from the CLI:

```
ksql> RUN SCRIPT '/etc/sql/all.sql';
```

Once you're finished, tear everything down using the following command:

```sh
docker-compose down
```

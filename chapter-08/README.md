# Getting Started with ksqlDB
This code corresponds with Chapter 8 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial shows how to use the official ksqlDB Docker images and run some basic queries.

[book]: https://www.kafka-streams-book.com/

# Running Locally
We're deploying the following components with Docker compose:

- Zookeeper
- Kafka
- ksqlDB server
- ksqlDB CLI

Feel free to checkout the [ksqlDB Server config][ksqldb-server-config] to see how we're configuring the ksqlDB server in this tutorial. The configuration is pretty basic in this introductory tutorial, but we'll expand on this in later chapters :)

[ksqldb-server-config]: files/ksqldb-server/ksql-server.properties

When you're ready, you can start the above services by running the following command:

```sh
$ docker-compose up
```

[ksqldb-server-config]: files/ksqldb-server/ksql-server.properties
[connect-config]: files/ksqldb-server/connect.properties

Once the services are running, open another tab and pre-create the Kafka topic needed for this tutorial:

```sh
docker-compose exec kafka kafka-topics \
    --bootstrap-server localhost:9092 \
    --topic users \
    --replication-factor 1 \
    --partitions 4 \
    --create
```

Once that is done, log into the ksqlDB CLI using the following command:

```sh
$ docker-compose exec ksqldb-cli ksql http://ksqldb-server:8088
```

If you see a `Could not connect to the server` error in the CLI, wait a few seconds and try again. ksqlDB can take several seconds to start.

Now, you're ready to run the ksqlDB statements for our hello, world tutorial:

```sql
CREATE STREAM users (
    ROWKEY INT KEY,
    USERNAME VARCHAR
) WITH (
    KAFKA_TOPIC='users',
    VALUE_FORMAT='JSON'
);

INSERT INTO users (username) VALUES ('izzy');
INSERT INTO users (username) VALUES ('elyse');
INSERT INTO users (username) VALUES ('mitch');

SET 'auto.offset.reset'='earliest';

SELECT 'Hello, ' + USERNAME AS GREETING
FROM users
EMIT CHANGES;
```

You should see output similar to the following:

```sql
+--------------------+
|GREETING            |
+--------------------+
|Hello, izzy         |
|Hello, elyse        |
|Hello, mitch        |
```

Once you're finished, tear everything down using the following command:

```sh
docker-compose down
```

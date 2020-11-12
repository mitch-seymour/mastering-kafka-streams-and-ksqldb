# Data Integration with ksqlDB and Kafka Connect
This code corresponds with Chapter 9 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial shows how to use ksqlDB and Kafka Connect to build data pipelines, which route data from one source (e.g. Postgres) to an external sink (e.g. Elasticsearch).

[book]: https://www.kafka-streams-book.com/

# Running Locally
We're deploying the following components with Docker compose:

- Zookeeper
- Kafka
- ksqlDB server (with Kafka Connect running in embedded mode)
- ksqlDB CLI
- Schema Registry
- Postgres
- Elasticsearch


Feel free to checkout the [ksqlDB Server config][ksqldb-server-config] and [Kafka Connect config][connect-config]. Also, the connectors will be installed using `confluent-hub` from this [startup script][script], so if you'd like to install / experiment with different connectors other than the Postgres and Elasticsearch connectors (which are used for this tutorial), feel free to update the script.

Once you're ready to start everything, run the following command:

[script]: files/ksqldb-server/run.sh

```sh
$ docker-compose up
```

[ksqldb-server-config]: files/ksqldb-server/ksql-server.properties
[connect-config]: files/ksqldb-server/connect.properties

Once the services are running, open another tab and log into the ksqlDB CLI using the following command:

```sh
$ docker-compose exec ksqldb-cli  ksql http://ksqldb-server:8088
```

If you see a `Could not connect to the server` error in the CLI, wait a few seconds and try again. ksqlDB can take several seconds to start.

Now, you're ready to run the ksqlDB statements to setup the connectors. The statements we need to run can be found [here][sql]. Either copy and paste each statement into the CLI, or run:

```sql
ksql> RUN SCRIPT '/etc/sql/all.sql';
```

You should see output similar to the following:

```sql
-----------------------------------
 Created connector postgres-source
-----------------------------------
--------------------------------------
 Created connector elasticsearch-sink
--------------------------------------
```

Now, our Postgres database was [pre-populated with a table][pg] called _titles_, and three separate rows.

```sh
docker exec -ti postgres psql -c "select * from titles"

 id |      title
----+-----------------
  1 | Stranger Things
  2 | Black Mirror
  3 | The Office
```

Therefore, verifying that the connectors worked simply involves checking to see if the Postgres data made it's way to Elasticsearch. We can verify by opening a third tab, and running a simple query against the Elasticsearch container:

[pg]: https://github.com/mitch-seymour/mastering-kafka-streams-and-ksqldb-private/blob/master/chapter-09.1/files/postgres/init.sql

```sh
$ docker-compose exec elasticsearch \
  curl -XGET 'localhost:9200/titles/_search?format=json&pretty'
```

If all goes well, you should see the following output:

```json
{
  "took" : 2,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 3,
      "relation" : "eq"
    },
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "titles",
        "_type" : "changes",
        "_id" : "1",
        "_score" : 1.0,
        "_source" : {
          "id" : 1,
          "title" : "Stranger Things"
        }
      },
      {
        "_index" : "titles",
        "_type" : "changes",
        "_id" : "3",
        "_score" : 1.0,
        "_source" : {
          "id" : 3,
          "title" : "The Office"
        }
      },
      {
        "_index" : "titles",
        "_type" : "changes",
        "_id" : "2",
        "_score" : 1.0,
        "_source" : {
          "id" : 2,
          "title" : "Black Mirror"
        }
      }
    ]
  }
}
```

[sql]: files/ksqldb-cli/all.sql

Feel free to experiment by inserting new rows into Postgres and re-running the Elasticsearch query. Once you're finished, tear everything down using the following command:

```sh
docker-compose down
```

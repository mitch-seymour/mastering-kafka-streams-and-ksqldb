# About
This code corresponds with Chapter 1 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This includes a simple demonstration of how to run a local Kafka cluster using Docker, create / describe Kafka topics, and produce / consume data from Kafka topics.

[book]: https://www.kafka-streams-book.com/

# Running Locally
The only dependency for running these examples is [Docker Compose][docker].

[docker]: https://docs.docker.com/compose/install/

Once Docker Compose is installed, you can start the local Kafka cluster using the following command:

```sh
$ docker-compose up
```

Now, log into the broker, since this is where we will be running our commands.
```sh
$ docker-compose exec kafka bash
```

## Create a topic
Once you're logged into the broker, run the following command to create a topic called `users`:

```sh
$ kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --topic users \
    --partitions 4 \
    --replication-factor 1
```

## Describe a topic
Once you've created the `users` topic, you can describe it using the following command:

```sh
$ kafka-topics \
    --bootstrap-server localhost:9092 \
    --describe \
    --topic users
```

You should see output similar to the following:
```sh
Topic: users	PartitionCount: 4	ReplicationFactor: 1	Configs:
	Topic: users	Partition: 0	Leader: 1	Replicas: 1	Isr: 1
	Topic: users	Partition: 1	Leader: 1	Replicas: 1	Isr: 1
	Topic: users	Partition: 2	Leader: 1	Replicas: 1	Isr: 1
	Topic: users	Partition: 3	Leader: 1	Replicas: 1	Isr: 1
``` 

## Produce data to a topic
The following command will allow you to produce data to the `users` topic that we created earlier. Run the following to be dropped into a prompt:

```sh
$ kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --property key.separator=, \
    --property parse.key=true \
    --topic users
```

Once you are in the prompt, produce a few records. Keys and values are separated by `,`, and you'll need to hit `<Enter>` on your keyboard after each row.

```sh
1,mitch
2,elyse
3,isabelle
4,sammy
```

When you are finished, press `Control-C` on your keyboard to exit the prompt.

## Consuming data from a topic
Run the following command to consume the data we just produced to the `users` topic in the section above.

```sh
$ kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic users \
    --from-beginning
```

You should see the following output:
```sh
mitch
isabelle
sammy
elyse
```

## Cleanup
Once you are done, log out of the broker by typing `exit` and run the following command to tear down the local Kafka cluster:

```sh
docker-compose down
```

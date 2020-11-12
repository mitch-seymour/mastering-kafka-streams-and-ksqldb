# Video game leader board
This code corresponds with Chapter 4 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial covers **Stateful processing** in Kafka Streams. Here, we demonstrate many stateful operators in Kafka Streams' high-level DSL by building an application a video game leaderboard.


[book]: https://www.kafka-streams-book.com/

# Running Locally
The only dependency for running these examples is [Docker Compose][docker].

[docker]: https://docs.docker.com/compose/install/

Once Docker Compose is installed, you can start the local Kafka cluster using the following command:

```sh
$ docker-compose up
```

Now, to run the Kafka Streams application, simply run:

```
./gradlew run --info
```

# Producing Test Data
Once your application is running, you can produce some test data to see it in action. Since our video game leaderboard application reads from multiple topics (`players`, `products`, and `score-events`), we have saved example records for each topic in the `data/` directory. To produce data into each of these topics, open a new tab in your shell and run the following commands.

```sh
# log into the broker, which is where the kafka console scripts live
$ docker-compose exec kafka bash

# produce test data to players topic
$ kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic players \
  --property 'parse.key=true' \
  --property 'key.separator=|' < players.json

# produce test data to products topic
$ kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic products \
  --property 'parse.key=true' \
  --property 'key.separator=|' < products.json

# produce test data to score-events topic
$ kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic score-events < score-events.json
```

# Query the API
This application exposes the video game leaderboard results using Kafka Streams interactive queries feature. The API is listening on port `7000`. Note the following examples use `jq` to prettify the output. If you don't have `jq` installed, either [install it][jq] or remove that part of the command.

[jq]: https://stedolan.github.io/jq/download/

### Get all leaderboard entries, grouped by game (i.e. _productId_)

```sh
curl -s localhost:7000/leaderboard | jq '.'

# example output (truncated)
{
  "1": [
    {
      "playerId": 3,
      "productId": 1,
      "playerName": "Isabelle",
      "gameName": "Super Smash Bros",
      "score": 4000
    },
    ...
  ],
  "6": [
    {
      "playerId": 3,
      "productId": 6,
      "playerName": "Isabelle",
      "gameName": "Mario Kart",
      "score": 9000
    },
    ...
  ]
}
```

### Get the leaderboard for a specific game (i.e. _productId_)
```sh
curl -s localhost:7000/leaderboard/1 | jq '.'

# example output
[
  {
    "playerId": 3,
    "productId": 1,
    "playerName": "Isabelle",
    "gameName": "Super Smash Bros",
    "score": 4000
  },
  {
    "playerId": 2,
    "productId": 1,
    "playerName": "Mitch",
    "gameName": "Super Smash Bros",
    "score": 2000
  },
  {
    "playerId": 1,
    "productId": 1,
    "playerName": "Elyse",
    "gameName": "Super Smash Bros",
    "score": 1000
  }
]
```

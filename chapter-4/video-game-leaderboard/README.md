# Video game leader board
This code corresponds with Chapter 4 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial covers **Stateful processing** in Kafka Streams. Here, we demonstrate many stateful operators in Kafka Streams' high-level DSL by building an application a video game leaderboard.


[book]: https://www.kafka-streams-book.com/

# Running Locally
The only dependency for running these examples is [Docker][docker]. Everything else is executed within a sandbox image. To mount and run the code for this tutorial in the sandbox image, simply run the following commands:

[docker]: https://www.docker.com/products/docker-desktop

```bash
# make sure you're in same directory as this README
cd /path/to/chapter-4/video-game-leaderboard

# mount and run the code
docker run --name ch4-sandbox \
  -v "$(pwd)":/app \
  -w /app \
  -p 7000:7000 \
  -ti magicalpipelines/cp-sandbox:latest  bash -c "\
    confluent local start schema-registry; \
    ./scripts/create-topics.sh; \
    ./gradlew run --info"
```

It may take a couple of minutes to start the first time you run the above command.

# Producing Test Data
Once your application is running, you can produce some test data to see it in action. Since our video game leaderboard application reads from multiple topics (`players`, `products`, and `score-events`), we have saved example records for each topic in the `data/` directory. To produce data into each of these topics, open a new tab in your shell and run the following commands.

```bash
docker exec -ti ch4-sandbox \
    bash -c "kafka-console-producer \
    --broker-list localhost:9092  \
    --topic players \
    --property 'parse.key=true' \
    --property 'key.separator=|' < data/players.json"
```

```bash
docker exec -ti ch4-sandbox \
    bash -c "kafka-console-producer \
    --broker-list localhost:9092  \
    --topic products \
    --property 'parse.key=true' \
    --property 'key.separator=|' < data/products.json"
```

```bash
docker exec -ti ch4-sandbox \
    bash -c "kafka-console-producer \
    --broker-list localhost:9092 \
    --topic score-events < data/score-events.json"
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

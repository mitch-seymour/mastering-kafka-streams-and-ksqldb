# Digital Twin Application
This code corresponds with Chapter 7 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. This tutorial covers **the Processor API** (sometimes called _PAPI_). Here, we build a digital twin service for IoT applications (in this case, an offshore wind farm) in order to demonstrate how to:

- Add source, sink, and stream processors
- Add and connect state stores
- Schedule periodic functions called punctuators
- Use alternative methods for cleaning up state
- Combine the high-level DSL with the Processor API
- Use processors and transformers

The goal of this Kafka Streams application is to stream sensor data from wind turbines and generate a shutdown signal whenever high windspeeds are detected. This code could easily be repurposed for other types of automations, or to even build a full blown service comparable to [Amazon's device shadow service][amazon-service] (_device shadow_ is another name for _digital twin_).

[book]: https://www.kafka-streams-book.com/
[amazon-service]: https://docs.aws.amazon.com/iot/latest/developerguide/iot-device-shadows.html

# Running Locally
The only dependency for running these examples is [Docker Compose][docker].

[docker]: https://docs.docker.com/compose/install/

Once Docker Compose is installed, you can start the local Kafka cluster using the following command:

```sh
$ docker-compose up
```

Now, to run the Kafka Streams application, simply run:

```
$ ./gradlew run --info
```

# Producing Test Data
Once your application is running, you can produce some test data to see it in action. Since our digital twin application reads from multiple topics (`reported-state-events`, `desired-state-events`), we have saved example records for each topic in the `data/` directory. First, produce some data to the `reported-state-events` topic using the following command:

```sh
$ docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic reported-state-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < reported-state-events.json"
```

One of the example records included a windspeed of 68mph, which exceeds our threshold for safe operating conditions (our threshold is 65mph):

```sh
{
  "timestamp": "2020-11-23T09:02:01.000Z",
  "wind_speed_mph": 68,
  "power": "ON",
  "type": "REPORTED"
}
```

Therefore, we should be able to query the Kafka Streams-backed service using `curl` to retrieve the device shadow for this device (which has an ID of 1, as indicated by the record key), and the desired state should show as `OFF` since our Kafka Streams application generated a shutdown signal.

```sh
$ curl localhost:7000/devices/1

# output
{
"desired": {
  "timestamp": "2020-11-23T09:02:01.000Z",
  "windSpeedMph": 68,
  "power": "OFF",
  "type": "DESIRED"
},
"reported": {
  "timestamp": "2020-11-23T09:02:01.000Z",
  "windSpeedMph": 68,
  "power": "ON",
  "type": "REPORTED"
}
```

The idea is that the corresponding device will query Kafka Streams regularly to synchronize it's state. In this case, a wind turbine would see that `desired.power` is set to `OFF`, shutdown power to it's blades, and report back it's actual state, causing `reported.power` to also be set to `OFF`.

We can also manually update the state of this device (i.e. by turning it on) by producing the following record to the `desired-state-events` topic:

```sh
{
  "timestamp": "2020-11-23T09:12:00.000Z",
  "power": "ON",
  "type": "DESIRED"
}
```

You can produce the above record using the following command:

```sh
$ docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic desired-state-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < desired-state-events.json"
```

Finally, query the API again and verify that the desired state was overridden from `desired.power` == `OFF` to `desired.power` == `ON`:

```sh
$ curl localhost:7000/devices/1

# output
{
  "desired": {
    "timestamp": "2020-11-23T09:12:00.000Z",
    "windSpeedMph": null,
    "power": "ON",
    "type": "DESIRED"
  },
  "reported": {
    "timestamp": "2020-11-23T09:02:01.000Z",
    "windSpeedMph": 68,
    "power": "ON",
    "type": "REPORTED"
  }
}
```

# The Road to Production
This code corresponds with Chapter 12 in the upcoming O'Reilly book: [Mastering Kafka Streams and ksqlDB][book] by Mitch Seymour. In this chapter, we discuss how to test Kafka Streams and ksqlDB applications, monitoring strategies, benchmarking Kafka Streams applications, and more.

[book]: https://www.kafka-streams-book.com/

## Kafka Streams Topology Test
The example Kafka Streams app that we will be testing lives in the [app/](app/) directory. You can view the tests [here](app/src/test/java/com/magicalpipelines). Once you are ready to run the tests, simply execute the following commands:

```sh
$ cd app/

$ ./gradlew test --info
```

## Kafka Streams Microbenchmarks
To run the microbenchmarks for our example Kafka Streams application, run the following commands:

```sh
$ cd app/

$ ./gradlew jmh
```

## ksqlDB Query Test
The example ksqlDB tests can be viewed in the [ksqldb-tests/](ksqldb-tests/) directory. Once you are ready to run the tests, execute the following commands:

```sh
$ cd ksqldb-tests/

$ docker run  \
    -v "$(pwd)":/ksqldb/ \
    -w /ksqldb \
    -ti confluentinc/ksqldb-server:0.14.0 \
    ksql-test-runner -s statements.sql -i input.json -o output.json
    
# look for "Test passed!" in the output

```

## Monitoring
The following commands will start a local Kafka cluster using Docker Compose, and then show you how to connect jconsole to the running ksqlDB server instance in order to explore the built-in JMX metrics.

```
$ docker-compose up -d

# run the command below to save your IP address to an env var.
# you can also manually set this env var to your IP if needed
$ MY_IP=$(ipconfig getifaddr en0);

$ docker run \
    --net=chapter-12_default \
    -p 1099:1099  \
    -v "$(pwd)/ksqldb":/ksqldb \
    -e KSQL_JMX_OPTS="\
        -Dcom.sun.management.jmxremote \
        -Djava.rmi.server.hostname=$MY_IP \
        -Dcom.sun.management.jmxremote.port=1099 \
        -Dcom.sun.management.jmxremote.rmi.port=1099 \
        -Dcom.sun.management.jmxremote.authenticate=false \
        -Dcom.sun.management.jmxremote.ssl=false" \
    -ti confluentinc/ksqldb-server:0.14.0  \
    ksql-server-start /ksqldb/config/server.properties
```

In another tab, start `jconsole`:

```sh
$ MY_IP=$(ipconfig getifaddr en0);

$ jconsole $MY_IP:1099
```

Click the button that says _Insecure connection_, and then once connected, select the tab that says _MBeans_ to start exploring the Kafka Streams and ksqlDB metrics.

Once you are finished, tear everything down by running:

```sh
docker-compose down
```

## Monitoring (advanced)
This will section will be added before the book goes to print. Thanks for your patience.


## Containerizing a Kafka Streams Application
You can easily containerize an application using [Jib][jib]. For example:

[jib]: https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin

```
$ cd app/

$ gradle jibDockerBuild
```

This will output the image name:

```
Built image to Docker daemon as magicalpipelines/myapp:0.1.0
```

Now, you can run your Kafka Streams image using the following command:

```
$ docker-compose up -d

$ docker run \
  -p 9010:9010 \
  --net=chapter-12_default \
  -ti magicalpipelines/myapp:0.1.0
```

Now, produce some messages:

```
$ docker-compose exec kafka \
  kafka-console-producer --bootstrap-server localhost:9092 --topic users
  
 # the above command will drop you into a prompt. enter some values followed by <ENTER>
 > izzy
 > mitch
 > elyse
```

Now, you can consume from the application's output topic, _greetings_, to verify that it works:

```sh
$ docker-compose exec kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings -from-beginning
 
# output
Hello izzy
Hello mitch
Hello elyse
```

Furthermore, this app is configured to expose the built-in Kafka Streams metrics, as well as some custom metrics, at the following endpoint:

- [localhost:9010/metrics](http://localhost:9010/metrics)

See the source code for implementation details.

Once you are finished, tear everything down by running:

```sh
docker-compose down
```

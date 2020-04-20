# Running Locally

There are two easy options for running the example code here.

## Option 1
First, if you want to see this running without setting up a service account for the translation and sentiment analysis service, you can run the following command.

```sh
docker run --name ch2-sandbox \
  -v "$(pwd)":/app \
  -w /app \
  -ti magicalpipelines/cp-sandbox:latest  bash -c "\
  confluent local start schema-registry; \
  ./scripts/create-topics.sh; \
  ./gradlew run --info"
```

Now, follow the instructions in [Producing Test Data](#-producing-test-data).

## Option 2
If you want the app to actually perform tweet translation and sentiment analysis, you will need to setup a service account with Google Cloud.

You can download `gcloud` by following the instructions [here](https://cloud.google.com/sdk/docs/downloads-interactive#mac). Then, run the following commands to enable the translation / NLP (natural language processing) APIs, and to download your service account key.

```bash
# login to your GCP account
$ gcloud auth login <email>

# if you need to create a project
$ gcloud projects create <project-name> # e.g. kafka-streams-demo. must be globally unique so adjust accordingly

# set the project to the appropriate value
# see `gcloud projects list` for a list of valid projects
$ gcloud config set project <project>

# create a service account for making NLP API requests
$ gcloud beta iam service-accounts create <sa-name> \ # e.g. <sa-name> could be "dev-streams"
    --display-name "Kafka Streams"

# enable the NLP API
$ gcloud services enable language.googleapis.com

# enable the translate API
$ gcloud services enable translate.googleapis.com

# create and download a key
$ gcloud iam service-accounts keys create ~/gcp-demo-key.json \
     --iam-account <sa-name>@<project>.iam.gserviceaccount.com
```

Then, set the following environment variable to the location where you saved your key.
```
export GCP_CREDS_PATH=~/gcp-demo-key.json
```

Finally, run the Kafka Streams application in the Docker sandbox container:
```sh
docker run --name ch2-sandbox \
  -v "$(pwd)":/app \
  -w /app \
  -v "${GCP_CREDS_PATH}":/secrets/credentials.json \
  -e "GOOGLE_APPLICATION_CREDENTIALS=/secrets/credentials.json" \
  -ti magicalpipelines/cp-sandbox:latest  bash -c "\
    confluent local start schema-registry; \
    ./scripts/create-topics.sh; \
    ./gradlew run --info"
```

Now, follow the instructions in [Producing Test Data](#-producing-test-data).

# Producing Test Data
We have a couple of test records saved to the `data/test.json` file. Feel free to modify the data in this file as you see fit. Then, run the following command to produce the test data to the source topic (`tweets`).

```sh
docker exec -ti ch2-sandbox bash -c "\
  kafka-console-producer \
  --broker-list localhost:9092 \
  --topic tweets < data/test.json"
```

In another tab, run the following command to consume data from the sink topic (`crypto-sentiment`).
```sh
docker exec -ti ch2-sandbox \
 kafka-avro-console-consumer \
 --bootstrap-server localhost:9092 \
 --topic crypto-sentiment \
 --from-beginning
 ```
 
 You should see records similar to the following appear in the sink topic.
 ```json
 {"created_at":1577933872630,"entity":"bitcoin","text":"Bitcoin has a lot of promise. I'm not too sure about #ethereum","sentiment_score":0.3444212495322003,"sentiment_magnitude":0.9464683988787772,"salience":0.9316858469669134}
{"created_at":1577933872630,"entity":"ethereum","text":"Bitcoin has a lot of promise. I'm not too sure about #ethereum","sentiment_score":0.1301464314096875,"sentiment_magnitude":0.8274198304784903,"salience":0.9112319163372604}
```

# create the tweets topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic tweets \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the crypto-sentiment topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic crypto-sentiment \
  --replication-factor 1 \
  --partitions 4 \
  --create

echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:9092 1 20

# create the reported-state-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic reported-state-events \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the desired-state-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic desired-state-events \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the digital-twins topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic digital-twins \
  --replication-factor 1 \
  --partitions 4 \
  --create

sleep infinity

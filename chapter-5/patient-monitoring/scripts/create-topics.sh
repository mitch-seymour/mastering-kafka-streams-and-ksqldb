# create the pulse-events topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic pulse-events \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the body-temp-events topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic body-temp-events \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the combined-vitals topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic alerts \
  --replication-factor 1 \
  --partitions 4 \
  --create

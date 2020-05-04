# create the hello-world topic
kafka-topics \
  --bootstrap-server localhost:9092 \
  --topic hello-world \
  --replication-factor 1 \
  --partitions 4 \
  --create


docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic reported-state-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < reported-state-events.json"

docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic desired-state-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < desired-state-events.json"

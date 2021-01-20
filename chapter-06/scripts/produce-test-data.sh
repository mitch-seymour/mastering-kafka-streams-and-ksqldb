docker-compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic patient-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < patient-events.json"

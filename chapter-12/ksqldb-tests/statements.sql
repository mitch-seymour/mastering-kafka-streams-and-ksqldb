CREATE STREAM users (
  ROWKEY INT KEY,
  USERNAME VARCHAR
) WITH (kafka_topic='users', value_format='JSON');

CREATE STREAM greetings
WITH (KAFKA_TOPIC = 'greetings') AS
SELECT ROWKEY, 'Hello, ' + USERNAME AS "greeting"
FROM users
EMIT CHANGES;


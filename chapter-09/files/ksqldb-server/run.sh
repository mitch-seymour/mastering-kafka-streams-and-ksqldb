#! /bin/bash

COMPONENT_DIR="/home/appuser"
CONNECT_PROPS="/etc/ksqldb-server/connect.properties"
CONFLUENT_HUB="/home/appuser/bin/confluent-hub"

# install the jdbc connector
$CONFLUENT_HUB install confluentinc/kafka-connect-jdbc:10.0.0 \
  --component-dir $COMPONENT_DIR \
  --worker-configs $CONNECT_PROPS \
  --no-prompt

# install the elasticsearch connector
$CONFLUENT_HUB install confluentinc/kafka-connect-elasticsearch:10.0.2 \
    --component-dir $COMPONENT_DIR \
    --worker-configs $CONNECT_PROPS \
    --no-prompt

# start the ksqldb server
ksql-server-start /etc/ksqldb-server/ksql-server.properties
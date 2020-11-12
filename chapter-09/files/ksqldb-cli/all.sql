CREATE SOURCE CONNECTOR `postgres-source` WITH(
    "connector.class"='io.confluent.connect.jdbc.JdbcSourceConnector',
    "connection.url"='jdbc:postgresql://postgres:5432/root?user=root&password=secret',
    "mode"='incrementing',
    "incrementing.column.name"='id',
    "topic.prefix"='',
    "table.whitelist"='titles',
    "key"='id');


CREATE SINK CONNECTOR `elasticsearch-sink` WITH(
    "connector.class"='io.confluent.connect.elasticsearch.ElasticsearchSinkConnector',
    "connection.url"='http://elasticsearch:9200',
    "connection.username"='',
    "connection.password"='',
    "batch.size"='1',
    "write.method"='insert',
    "topics"='titles',
    "type.name"='changes',
    "key"='title_id');

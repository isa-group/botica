package es.us.isa.botica.director.broker;

public final class RabbitMqConfigurationGeneratorTestData {
  static final String QUEUE_FORMAT =
      "    {\n"
          + "      \"name\": \"%s\",\n"
          + "      \"vhost\": \"/\",\n"
          + "      \"durable\": true,\n"
          + "      \"auto_delete\": false,\n"
          + "      \"arguments\": {}\n"
          + "    }";
  static final String STREAM_QUEUE_FORMAT =
      "    {\n"
          + "      \"name\": \"%s\",\n"
          + "      \"vhost\": \"/\",\n"
          + "      \"durable\": true,\n"
          + "      \"auto_delete\": false,\n"
          + "      \"arguments\": {\n"
          + "        \"x-queue-type\": \"stream\"\n"
          + "      }\n"
          + "    }";

  static final String EXCHANGE_FORMAT =
      "    {\n"
          + "      \"name\": \"%s\",\n"
          + "      \"vhost\": \"/\",\n"
          + "      \"type\": \"topic\",\n"
          + "      \"durable\": true,\n"
          + "      \"auto_delete\": false,\n"
          + "      \"internal\": false,\n"
          + "      \"arguments\": {}\n"
          + "    }";

  static final String BINDING_FORMAT =
      "    {\n"
          + "      \"source\": \"%s\",\n"
          + "      \"vhost\": \"/\",\n"
          + "      \"destination\": \"%s\",\n"
          + "      \"destination_type\": \"queue\",\n"
          + "      \"routing_key\": \"%s\",\n"
          + "      \"arguments\": {}\n"
          + "    }";
}

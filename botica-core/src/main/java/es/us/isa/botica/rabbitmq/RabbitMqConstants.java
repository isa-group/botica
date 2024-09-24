package es.us.isa.botica.rabbitmq;

public class RabbitMqConstants {
  public static final String CONTAINER_NAME = "rabbitmq";

  public static final String ORDER_EXCHANGE = "botica.order";
  public static final String PROTOCOL_EXCHANGE = "botica.protocol";

  // region names for both queues and keys
  public static final String BOT_TYPE_ORDERS_DISTRIBUTED_FORMAT = "bot_type.%s.orders.distributed";
  public static final String BOT_TYPE_ORDERS_BROADCAST_FORMAT = "bot_type.%s.orders.broadcast";

  public static final String DIRECTOR_PROTOCOL = "director.protocol";
  public static final String BOT_PROTOCOL_IN_FORMAT = "bot.%s.protocol";
  public static final String BOT_ORDERS_FORMAT = "bot.%s.orders";
  // endregion
}

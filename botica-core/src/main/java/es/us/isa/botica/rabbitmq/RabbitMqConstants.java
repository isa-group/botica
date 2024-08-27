package es.us.isa.botica.rabbitmq;

public class RabbitMqConstants {
  public static final String CONTAINER_NAME = "rabbitmq";

  public static final String ORDER_EXCHANGE = "botica.order";
  public static final String PROTOCOL_EXCHANGE = "botica.protocol";

  public static final String BOT_TYPE_ORDERS_DISTRIBUTED_FORMAT = "bot_type.%s.orders.distributed";
  public static final String BOT_TYPE_ORDERS_BROADCAST_FORMAT = "bot_type.%s.orders.broadcast";
  public static final String BOT_ORDERS_FORMAT = "bot.%s.orders";

  public static final String BOT_PROTOCOL_IN_FORMAT = "bot.%s.protocol.in";
  public static final String BOT_PROTOCOL_OUT_FORMAT = "bot.%s.protocol.out";
}

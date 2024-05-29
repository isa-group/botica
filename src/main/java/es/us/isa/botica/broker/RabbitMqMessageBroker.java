package es.us.isa.botica.broker;

public class RabbitMqMessageBroker implements MessageBroker {
  public static final String BOT_MESSAGES_EXCHANGE = "botica.bot_messages";
  public static final String INTERNAL_EXCHANGE = "botica.internal_messages";
}

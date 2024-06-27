package es.us.isa.botica.configuration.broker;

public enum BrokerType {
  RABBIT_MQ(RabbitMqConfiguration.class, "rabbitmq");

  private final Class<? extends BrokerConfiguration> implementationClass;
  private final String name;

  public Class<? extends BrokerConfiguration> getImplementationClass() {
    return implementationClass;
  }

  public String getName() {
    return name;
  }

  BrokerType(Class<? extends BrokerConfiguration> implementationClass, String name) {
    this.implementationClass = implementationClass;
    this.name = name;
  }
}

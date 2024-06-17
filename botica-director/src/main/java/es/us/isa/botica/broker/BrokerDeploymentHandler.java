package es.us.isa.botica.broker;

public interface BrokerDeploymentHandler {
  String BROKER_NETWORK_NAME = "broker_network";

  void deploy();

  boolean isRunning();

  void shutdown();
}

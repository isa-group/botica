package es.us.isa.botica.director.broker;

public interface BrokerDeploymentHandler {
  void deploy();

  boolean isRunning();

  void shutdown();
}

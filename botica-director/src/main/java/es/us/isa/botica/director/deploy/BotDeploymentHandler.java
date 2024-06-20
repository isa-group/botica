package es.us.isa.botica.director.deploy;

public interface BotDeploymentHandler {
  void removePreviousDeployment();

  void deploy();

  void shutdown();
}

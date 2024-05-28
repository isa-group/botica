package es.us.isa.botica.configuration.broker;

// provisional
public class RabbitMqConfigurationPaths {
  private String main;
  private String definitions;
  private String connection;

  public String getMain() {
    return main;
  }

  public void setMain(String main) {
    this.main = main;
  }

  public String getDefinitions() {
    return definitions;
  }

  public void setDefinitions(String definitions) {
    this.definitions = definitions;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  @Override
  public String toString() {
    return "RabbitMqConfigurationPaths{"
        + "main='"
        + main
        + '\''
        + ", definitions='"
        + definitions
        + '\''
        + ", connection='"
        + connection
        + '\''
        + '}';
  }
}

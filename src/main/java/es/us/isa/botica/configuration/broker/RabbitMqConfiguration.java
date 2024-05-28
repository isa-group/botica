package es.us.isa.botica.configuration.broker;

public class RabbitMqConfiguration implements BrokerConfiguration {
  private String host;
  private int port;
  private int uiPort;
  private String username;
  private String password;
  private RabbitMqConfigurationPaths configurationPaths;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getUiPort() {
    return uiPort;
  }

  public void setUiPort(int uiPort) {
    this.uiPort = uiPort;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public RabbitMqConfigurationPaths getConfigurationPaths() {
    return configurationPaths;
  }

  public void setConfigurationPaths(RabbitMqConfigurationPaths configurationPaths) {
    this.configurationPaths = configurationPaths;
  }

  @Override
  public String toString() {
    return "RabbitMqConfiguration{"
        + "host='"
        + host
        + '\''
        + ", port="
        + port
        + ", uiPort="
        + uiPort
        + ", username='"
        + username
        + '\''
        + ", password='"
        + password
        + '\''
        + ", configurationPaths="
        + configurationPaths
        + '}';
  }
}

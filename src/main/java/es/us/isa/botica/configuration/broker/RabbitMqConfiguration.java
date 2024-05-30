package es.us.isa.botica.configuration.broker;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class RabbitMqConfiguration implements BrokerConfiguration {
  private String username;
  private String password;
  private int port = 5672;

  @Override
  public void validate(ValidationReport report) {
    if (username == null || username.isBlank()) {
        report.addError("username", "missing or empty username");
    }
    if (password == null || password.isBlank()) {
        report.addError("password", "missing or empty password");
    }
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

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public String toString() {
    return "RabbitMqConfiguration{"
        + "username='"
        + username
        + '\''
        + ", password='"
        + password
        + '\''
        + ", port="
        + port
        + '}';
  }
}

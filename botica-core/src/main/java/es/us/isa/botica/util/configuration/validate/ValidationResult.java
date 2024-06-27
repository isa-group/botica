package es.us.isa.botica.util.configuration.validate;

public class ValidationResult {
  private final Type type;
  private final String property;
  private final String message;

  public ValidationResult(Type type, String property, String message, Object... args) {
    this.type = type;
    this.property = property;
    this.message = String.format(message, args);
  }

  public Type getType() {
    return type;
  }

  public String getProperty() {
    return property;
  }

  public String getMessage() {
    return message;
  }

  public enum Type {
    WARNING("warning"),
    ERROR("error");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}

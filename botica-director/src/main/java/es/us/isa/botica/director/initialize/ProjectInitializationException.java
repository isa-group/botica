package es.us.isa.botica.director.initialize;

public class ProjectInitializationException extends Exception {
  public ProjectInitializationException(String message) {
    super(message);
  }

  public ProjectInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}

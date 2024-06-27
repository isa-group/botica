package es.us.isa.botica.director.exception;

/**
 * A controlled exception in botica-director. Director exceptions will be logged as errors without
 * printing the stack trace.
 */
public class DirectorException extends RuntimeException {
  public DirectorException(String message) {
    super(message);
  }

  public DirectorException(Throwable cause) {
    super(cause.getMessage());
  }
}

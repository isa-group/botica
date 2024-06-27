package es.us.isa.botica.director.exception;

/** Signals that an error occurred while attempting to establish a connection. */
public class ConnectException extends DirectorException {
  public ConnectException(String message) {
    super(message);
  }

  public ConnectException(Throwable cause) {
    super(cause);
  }
}

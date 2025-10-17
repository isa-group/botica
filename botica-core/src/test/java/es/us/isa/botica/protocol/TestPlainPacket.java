package es.us.isa.botica.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestPlainPacket implements Packet {
  private final String message;

  @JsonCreator
  public TestPlainPacket(@JsonProperty("message") String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}

package es.us.isa.botica.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.protocol.query.AbstractResponsePacket;

public class TestResponsePacket extends AbstractResponsePacket {
  private final String responseData;

  @JsonCreator
  public TestResponsePacket(@JsonProperty("responseData") String responseData) {
    super();
    this.responseData = responseData;
  }

  public TestResponsePacket(String responseData, String requestId) {
    super(requestId);
    this.responseData = responseData;
  }

  public String getResponseData() {
    return responseData;
  }
}

package es.us.isa.botica.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.protocol.query.AbstractRequestPacket;

public class TestRequestPacket extends AbstractRequestPacket<TestResponsePacket> {
  private final String requestData;

  @JsonCreator
  public TestRequestPacket(@JsonProperty("requestData") String requestData) {
    super(TestResponsePacket.class);
    this.requestData = requestData;
  }

  public TestRequestPacket(String requestData, String requestId) {
    super(TestResponsePacket.class, requestId);
    this.requestData = requestData;
  }

  public String getRequestData() {
    return requestData;
  }
}

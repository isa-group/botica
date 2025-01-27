package es.us.isa.botica.protocol.query;

public abstract class AbstractResponsePacket implements ResponsePacket {
  protected String requestId;

  public AbstractResponsePacket() {}

  public AbstractResponsePacket(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }
}

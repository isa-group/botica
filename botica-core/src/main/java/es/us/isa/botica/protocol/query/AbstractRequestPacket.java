package es.us.isa.botica.protocol.query;

public abstract class AbstractRequestPacket<ResponsePacketT extends ResponsePacket>
    implements RequestPacket<ResponsePacketT> {
  private final Class<ResponsePacketT> responsePacketClass;
  protected String requestId;

  public AbstractRequestPacket(Class<ResponsePacketT> responsePacketClass) {
    this.responsePacketClass = responsePacketClass;
  }

  public AbstractRequestPacket(Class<ResponsePacketT> responsePacketClass, String requestId) {
    this.responsePacketClass = responsePacketClass;
    this.requestId = requestId;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  @Override
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public Class<ResponsePacketT> getResponsePacketClass() {
    return responsePacketClass;
  }
}

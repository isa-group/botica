package es.us.isa.botica.protocol.query;


import es.us.isa.botica.protocol.Packet;

public interface RequestPacket<ResponsePacketT extends ResponsePacket> extends Packet {
  String getRequestId();

  void setRequestId(String requestId);

  Class<ResponsePacketT> getResponsePacketClass();
}

package es.us.isa.botica.director.protocol;

import es.us.isa.botica.protocol.Packet;

@FunctionalInterface
public interface PacketListener<P extends Packet> {
  void onPacketReceived(String sourceBotId, P packet);
}

package es.us.isa.botica.protocol;

public interface PacketListener<P extends Packet> {
  void onPacketReceived(P packet);
}

package es.us.isa.botica.director.protocol;

import es.us.isa.botica.protocol.Packet;
import java.util.concurrent.TimeoutException;

public interface BoticaServer {
  void start() throws TimeoutException;

  boolean isConnected();

  <P extends Packet> void registerPacketListener(Class<P> packetClass, PacketListener<P> listener);

  void sendPacket(Packet packet, String botId);

  void close();
}

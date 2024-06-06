package es.us.isa.botica.client;

import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.protocol.Packet;
import es.us.isa.botica.protocol.PacketListener;
import java.util.concurrent.TimeoutException;

public interface BoticaClient {
  void connect(BrokerConfiguration brokerConfiguration) throws TimeoutException;

  void registerOrderListener(String order, OrderListener listener);

  void publishOrder(String key, String order, String message);

  <P extends Packet> void registerPacketListener(Class<P> packetClass, PacketListener<P> listener);

  void sendPacket(Packet packet);

  void close();
}

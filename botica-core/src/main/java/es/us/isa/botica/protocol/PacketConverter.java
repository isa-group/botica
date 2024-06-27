package es.us.isa.botica.protocol;

public interface PacketConverter {
  String serialize(Packet packet);

  Packet deserialize(String raw);
}

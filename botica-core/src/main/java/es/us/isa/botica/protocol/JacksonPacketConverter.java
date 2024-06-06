package es.us.isa.botica.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonPacketConverter implements PacketConverter {
  private final ObjectMapper objectMapper;

  public JacksonPacketConverter() {
    this(new ObjectMapper());
  }

  public JacksonPacketConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String serialize(Packet packet) {
    try {
      return objectMapper.writeValueAsString(packet);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          "Exception while serializing " + packet.getClass().getName() + ": " + packet, e);
    }
  }

  @Override
  public Packet deserialize(String raw) {
    try {
      return objectMapper.readValue(raw, Packet.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          "Exception while serializing packet " + raw, e);
    }
  }
}

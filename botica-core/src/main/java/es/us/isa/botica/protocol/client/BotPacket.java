package es.us.isa.botica.protocol.client;

import es.us.isa.botica.protocol.Packet;

/**
 * A meta-packet that contains the actual packet sent by a bot along with its id.
 *
 * <p>This class is useful in scenarios where the communication protocol does not directly support
 * identifying the source of a packet. For example, in a broker implementation that uses a single
 * director queue, this class helps differentiate packets from different bots by attaching the bot's
 * id.
 */
public class BotPacket implements ClientPacket {
  private String botId;
  private Packet packet;

  public BotPacket() {}

  public BotPacket(String botId, Packet packet) {
    this.botId = botId;
    this.packet = packet;
  }

  public String getBotId() {
    return botId;
  }

  public void setBotId(String botId) {
    this.botId = botId;
  }

  public Packet getPacket() {
    return packet;
  }

  public void setPacket(Packet packet) {
    this.packet = packet;
  }

  @Override
  public String toString() {
    return "BotPacket{" + "botId='" + botId + '\'' + ", packet=" + packet + '}';
  }
}

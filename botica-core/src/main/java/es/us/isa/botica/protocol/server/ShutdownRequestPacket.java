package es.us.isa.botica.protocol.server;

import es.us.isa.botica.protocol.client.ShutdownResponsePacket;

/**
 * Packet sent by botica-director to all bots when a shutdown is requested.
 *
 * <p>Bots should reply with {@link ShutdownResponsePacket}.
 */
public class ShutdownRequestPacket implements ServerPacket {
  private boolean forced;

  public ShutdownRequestPacket() {}

  public ShutdownRequestPacket(boolean forced) {
    this.forced = forced;
  }

  public boolean isForced() {
    return forced;
  }

  public void setForced(boolean forced) {
    this.forced = forced;
  }

  @Override
  public String toString() {
    return "ShutdownRequestPacket{" + "forced=" + forced + '}';
  }
}

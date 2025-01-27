package es.us.isa.botica.protocol.server;

import es.us.isa.botica.protocol.client.ShutdownResponsePacket;
import es.us.isa.botica.protocol.query.AbstractRequestPacket;

/**
 * Packet sent by botica-director to all bots when a shutdown is requested.
 *
 * <p>Bots should reply with {@link ShutdownResponsePacket}.
 */
public class ShutdownRequestPacket extends AbstractRequestPacket<ShutdownResponsePacket>
    implements ServerPacket {
  private boolean forced;

  public ShutdownRequestPacket() {
    super(ShutdownResponsePacket.class);
  }

  public ShutdownRequestPacket(boolean forced) {
    super(ShutdownResponsePacket.class);
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
    return "ShutdownRequestPacket{" + "forced=" + forced + ", requestId='" + requestId + '\'' + '}';
  }
}

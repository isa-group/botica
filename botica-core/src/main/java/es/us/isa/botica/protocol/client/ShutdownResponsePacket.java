package es.us.isa.botica.protocol.client;

import es.us.isa.botica.protocol.server.ShutdownRequestPacket;

/**
 * Packet sent by bots in response to a {@link ShutdownRequestPacket}, indicating if it's ready to
 * be shut down or needs more time to complete its job.
 */
public class ShutdownResponsePacket implements ClientPacket {
  private boolean ready;

  public ShutdownResponsePacket() {}

  public ShutdownResponsePacket(boolean ready) {
    this.ready = ready;
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  @Override
  public String toString() {
    return "ShutdownResponsePacket{" + "ready=" + ready + '}';
  }
}

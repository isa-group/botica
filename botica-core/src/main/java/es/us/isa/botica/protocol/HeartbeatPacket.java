package es.us.isa.botica.protocol;

/**
 * Sent by both the server and clients to verify their connection status. The server sends this
 * packet regularly, and clients should respond by sending the same packet back to the server.
 */
public class HeartbeatPacket implements Packet {}

package es.us.isa.botica.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import es.us.isa.botica.protocol.client.BotPacket;
import es.us.isa.botica.protocol.client.ReadyPacket;
import es.us.isa.botica.protocol.server.ShutdownRequestPacket;
import es.us.isa.botica.protocol.client.ShutdownResponsePacket;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = BotPacket.class, name = "bot"),
    @Type(value = ReadyPacket.class, name = "ready"),
    @Type(value = HeartbeatPacket.class, name = "heartbeat"),
    @Type(value = ShutdownRequestPacket.class, name = "shutdownRequest"),
    @Type(value = ShutdownResponsePacket.class, name = "shutdownResponse")
})
public interface Packet {}

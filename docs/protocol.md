# The botica protocol

The **Botica Protocol** is a communication protocol used by the Botica Director and its bots to
exchange messages. The protocol defines how bots communicate with the Director to ensure proper
coordination of tasks such as heartbeat checks, shutdown requests, and status updates. The current
implementation uses a message broker for communication, but the protocol itself is abstracted from
any specific communication layer, allowing for future implementations over different transport
mechanisms (e.g., sockets).

The protocol relies on **packets**, which are JSON-serialized data structures, with a common
field `"type"` that indicates the packet type. The protocol is extensible, and new packet types can
be added to support additional functionality.

## Core concepts

### Packets

Packets are the core units of communication in the Botica Protocol. Each packet has a `"type"` field
that indicates the type of message being sent. The protocol uses JSON serialization and
deserialization to convert these packets into structured data.

- **Common field**: `"type"` — this field indicates the type of the packet and is used for
  deserialization.

### BotPacket: a special wrapper for some protocol implementations

The **BotPacket** is a special packet used in scenarios where the protocol implementation cannot
handle identifying the source of a packet (such as when all bots communicate through a single
message queue). The `BotPacket` wraps another packet and includes the ID of the bot that sent it,
allowing the Director to identify the sender.

## Packet specification

Below is a description of the different packet types defined in the Botica Protocol, their purpose,
and their properties.

### Ready packet

- **Type**: `ready`
- **Purpose**: the **ReadyPacket** is sent by the bot to indicate that it is fully operational and
  ready to handle tasks.
- **Properties**: none
- **Direction**: from bots to Director

### Heartbeat packet

- **Type**: `heartbeat`
- **Purpose**: sent regularly by both the Director and the bots to verify the connection's status.
  Bots respond to the Director's heartbeat by sending a heartbeat packet back.
- **Properties**: none
- **Direction**: bidirectional (sent by both Director and bots)

### Shutdown request packet

- **Type**: `shutdownRequest`
- **Purpose**: sent by the Director when it requests a bot to shut down. This packet can indicate
  whether the shutdown is forced.
- **Properties**:
    - `forced`: `boolean` — indicates whether the shutdown is forced. A forced shutdown allows only
      a brief grace period for bots to save important data before the container is shut down.
- **Direction**: from Director to bots

### Shutdown response packet

- **Type**: `shutdownResponse`
- **Purpose**: sent by bots in response to a shutdown request from the Director. It indicates
  whether the bot is ready to shut down or needs more time to complete its tasks.
- **Properties**:
    - `ready`: `boolean` — Indicates whether the bot is ready for shutdown. If `false`, the bot is
      still working and will require more time.
- **Direction**: from bots to Director

### BotPacket

- **Type**: `bot`
- **Purpose**: the **BotPacket** is a meta-packet that wraps another packet and includes the ID of
  the bot that sent it. This packet is used in cases where the protocol implementation cannot track
  the origin of the packets, such as when all bots communicate through a single queue.
- **Properties**:
    - `botId`: `string` — The ID of the bot that sent the packet.
    - `packet`: `Packet` — The actual packet being sent, wrapped by the `BotPacket`.
- **Direction**: from bots to Director

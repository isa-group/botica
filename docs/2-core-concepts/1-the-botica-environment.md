# The Botica environment

A Botica environment is a fully integrated, self-contained ecosystem that brings together all the
necessary components for your multi-bot system to operate cohesively. When you run the Botica
Director, it creates this environment, which includes your bots, a message broker, a shared network,
a shared directory, and all the underlying connections that link them together.

![Botica overview diagram](../assets/architecture.svg)

## Components of a Botica environment

### The Botica Director

The **Director** is the central orchestrator of the environment. It's the command-line program that
reads your `environment.yml` file and brings your entire multi-bot system to life.

Its responsibilities include:

- Deploying the message broker and all your bot containers.
- Establishing the shared network and volumes.
- Passing essential configuration and identity details (such as bot type, instance ID, and internal
  connection passwords) to each bot.
- Maintaining communication with each bot for health checks (heartbeats) and other management tasks
  like graceful shutdown requests.

After starting the environment, the Director provides an interactive command-line interface (CLI).
For instance, you can use the `stop` command to initiate a shutdown:

```bash
11:46:29.663 INFO  Botica is running! Use the 'stop' command to shut down the environment.
> stop
11:46:39.374 INFO  Requesting bots to shut down...
11:46:39.424 INFO  my-bot-1 is ready to be shut down. Stopping...
11:46:39.427 INFO  my-bot-2 is ready to be shut down. Stopping...
11:46:39.431 INFO  my-other-bot-1 is ready to be shut down. Stopping...
11:46:40.302 INFO  Shutting down the container infrastructure...
```

If any of your bots are programmed to delay or cancel a shutdown request, you can override this
behavior using `stop --force`.

### The message broker

The **message broker** is the central communication hub that allows your bots to exchange messages,
which are called **orders** in Botica.

Crucially, this is a component you don't have to manage yourself, nor do you directly interact with
it in your bot's code. It is automatically deployed in a container by the Director before your bots
start, and your bots, built with the official Botica libraries, automatically connect to it. You use
convenient methods provided by the Botica libraries (e.g., `publishOrder`, `on`) to send and receive
orders, with the message broker acting as the underlying transport.

Each time you start a Botica environment, the broker starts from a clean slate with a new, randomly
generated username and password for security. While the broker's default port on your host machine
is typically fine, you can configure it in your `environment.yml` if the port is already in use.

```yml
# This entire section is optional.
broker:
  type: rabbitmq # This is the default broker type
  port: 5673 # Change the default host port from 5672 to 5673
```

To learn more about how bots send and receive messages, see
**[Messaging between bots](./3-messaging-between-bots.md)**.

### Bots

Bots are the main actors in your environment. Each bot is a program, built using a **Botica
library**, that runs inside its own Docker container. Because they operate within the Botica
environment, they are automatically connected to the Director for lifecycle management and to the
message broker for communication. This eliminates the need for any boilerplate code related to
network connections or broker setup.

To learn more about bots, their capabilities, or how to define them, see
**[The concept of a bot](./2-the-concept-of-a-bot.md)**.

### Shared resources

All components within the Botica environment have access to shared resources that facilitate
different forms of communication and data exchange.

#### Shared network

All bot containers and the message broker are connected to the same internal Docker network. This
allows them to communicate directly if needed, for instance, by making direct HTTP requests. Bots
can discover each other on this network using their unique instance ID as a hostname.

Instance IDs follow a predictable pattern for replicas: `%bot_type%-%replica_number%` (e.g.,
`my_bot_type-1`, `my_bot_type-2`). The Botica libraries provide a utility function to get the
correct hostname for a given bot ID.

- **Java:**
  ```java
  String workerTwoHostname = getBotHostname("my_bot_type-2");
  // Now you can connect to http://my_bot_type-2:<port>
  ```

- **Node.js:**
  ```ts
  const workerTwoHostname = bot.getBotHostname("my_bot_type-2");
  // Now you can connect to http://my_bot_type-2:<port>
  ```

For more details, see the documentation
for [Java](https://github.com/isa-group/botica-lib-java/blob/main/docs/6-accessing-environment-and-utilities.md)
and [Node.js](https://github.com/isa-group/botica-lib-node/blob/main/docs/6-accessing-environment-and-utilities.md).

#### Shared directory

Botica automatically creates and mounts a shared Docker volume at `/shared` inside every bot
container. This provides a common filesystem for all bots, making it easy to share large files,
datasets, or any data that isn't suitable for sending through the message broker.

- **Java:**
  ```java
  File sharedDir = getSharedDirectory();
  Path outputFile = sharedDir.toPath().resolve("report.pdf");
  ```

- **Node.js:**
  ```ts
  import { SHARED_DIRECTORY } from "botica-lib-node";
  import path from "path";
  
  const outputFile = path.join(SHARED_DIRECTORY, "report.pdf");
  ```

---

## Next steps

Now that you understand the overall Botica environment, delve deeper into the core concept of a bot:

- **[The concept of a bot](./2-the-concept-of-a-bot.md)**

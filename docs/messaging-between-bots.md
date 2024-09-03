# Messaging between bots in Botica

In Botica, communication between bots is facilitated through a messaging system that operates via a
message broker. Messages, also known as orders, are the core units of communication, and they
involve three key concepts: **keys**, **orders**, and **message content**. This page will explain
how these concepts work together to allow bots to communicate and how you can configure and use
them in your Botica bots.

## Messaging concepts

### Key

The **key** is a broker-level concept. When a bot publishes a message, it must specify a key. This
key determines which bots will receive the message, based on the keys they are subscribed to. Bots
can subscribe to multiple keys as defined in their configuration.

> [!NOTE]
> Key subscriptions are specified at the configuration level. Bots subscribed to a particular
> key will always receive any messages published with that key.

### Order

The **order** is a bot-level concept. When a bot receives a message via a subscribed key, the order
within that message is what the Botica library (e.g., for Java, Node.js) uses to trigger the
appropriate order listener. This means that at the development level, when you are writing the bot,
you are primarily concerned with adding listeners for specific orders rather than subscribing to
keys directly.

> [!NOTE]
> Order listeners are the mechanisms through which bots respond to incoming messages. Developers
> add listeners in their bot code that react to specific orders received on subscribed keys.

### Message Content

The **message content** is the actual data being sent between bots. It can be in any format, though
JSON is commonly used. The content is simply a string that is passed along with the key and order
when a message is published.

## Configuration and development workflow

### Configuration level

At the configuration level, you define which keys a bot is subscribed to. This is done in the
infrastructure configuration file. The Botica Director reads this configuration and
sets up the necessary subscriptions when starting the bots:

Additionally, you can specify the strategy for how messages are delivered to the bot instances
subscribed to a key. There are two strategies available:

- **Distributed strategy**: with this strategy, each new order is delivered to one instance of the
  bot type that is subscribed to the key and available to process a new task. Bots request new
  orders from the queue when they are free, ensuring that tasks are assigned to bots that have
  completed their previous work. This approach is ideal for load balancing, as it ensures that no
  single instance becomes a bottleneck and tasks are efficiently distributed across available bots.

- **Broadcast strategy**: in contrast, the broadcast strategy delivers each new order to every
  instance of the bot type subscribed to the key. This is useful when you need all instances of a
  bot to act on the same information, such as updating a shared state or performing a synchronized
  action.

```yaml
bots:
  example-bot:
    subscribe:
      - key: "tasks"
        strategy: distributed
      - key: "data_updates"
        strategy: broadcast
    { ... }
```

In this example, example-bot instances will receive all messages sent with the `data_update` key
and request messages from the queue of the `tasks` key.

### Bot development level

When developing a bot, you focus on defining how it responds to specific orders. Since key
subscriptions are handled by the configuration, your main task is to register order listeners and
define how to handle the incoming messages.

#### Listening to orders

```js
import botica from "botica-lib-node";

const bot = await botica();

// Registering an order listener in a Node.js bot for the "process_data" order
bot.onOrderReceived((order, message) => {
  // Process the data contained in the message
  console.log("Processing data:", message);
}, "process_data");

// Now, for the "update_data" order
bot.onOrderReceived((order, message) => {
  console.log("Updating data:", message);
}, "update_data");

await bot.start();
```

In this example, the bot listens for the `process_data` and `update_data` orders and responds
accordingly. The `onOrderReceived` method is a simple way to tie specific orders to their
corresponding handlers.

#### Publishing messages

To publish a message from a bot, you need to specify the key, order, and message content.

```js
// Publishing a message in a Node.js bot
await bot.publish({id: 123, payload: "data"}, "tasks", "process_data");
```

Here, the bot publishes a message with the `tasks` key and the `process_data` order, sending
along some data in JSON format.

### Simplifying development with defaults

To streamline the development process, Botica allows you to configure default keys and orders in the
bot's configuration. These defaults can be used to simplify the code by eliminating the need to
repeatedly specify the key and order.

#### Registering an order listener with defaults

You can configure the bot to use a default order for its listeners, allowing you to write cleaner,
simpler code.

```yaml
bots:
  example-bot:
    subscribe:
      - key: "tasks"
        strategy: distributed
    lifecycle:
      type: reactive
      order: "process_data" # default order for listeners
    { ... }
```

In the bot code:

```js
// Registering a listener for the "process_data" default order
bot.registerOrderListener((order, message) => {
  console.log("Processing data:", message);
});
```

#### Publishing messages with defaults

Similarly, you can configure default key and order values for publishing messages. This allows you
to publish messages without specifying these details every time.

```yaml
bots:
  example-bot:
    publish:
      key: "tasks"
      order: "process_data"
  { ... }
```

In the bot code:

```js
// Using defaults for publishing in a Node.js bot
await bot.publish({id: 123, payload: "data"});
```

In this example, the bot will automatically use the `task_updates` key and the `process_data` order
when publishing messages.

[<- The concept of a bot](the-concept-of-a-bot.md) | [Sharing files between bots ->](sharing-files-between-bots.md)

# Messaging between bots in Botica

In Botica, communication between bots is facilitated through a messaging system that operates via a
message broker. Messages, also known as orders, are the core units of communication, and they
involve three key concepts: **key**, **action**, and **payload**. This page will explain
how these concepts work together to allow bots to communicate and how you can configure and use
them in your Botica bots.

## Messaging concepts

### Key

The **key** is a broker-level concept. When a bot publishes an order, it must specify a key. This
key determines which bots will receive the order, based on the keys they are subscribed to. Bots
can subscribe to multiple keys as defined in their configuration.

> [!NOTE]
> Key subscriptions are specified at the configuration level. Bots subscribed to a particular
> key will always receive any orders published with that key.

### Action

The **action** is a bot-level concept. When a bot receives an order via a subscribed key, the
action within that order is what the Botica library (e.g., for Java, Node.js) uses to trigger the
appropriate order listener/method. This means that at the development level, when you are writing
the bot, you are primarily concerned with adding listeners for specific actions rather than
subscribing to keys directly.

> [!NOTE]
> Order listeners are the mechanisms through which bots respond to incoming orders. Developers
> add listeners in their bot code that react to specific actions received on subscribed keys.

### Payload

The **payload** is the actual data being sent between bots. It can be in any format, though
JSON is commonly used. The content is simply a string that is passed along with the key and action
when an order is published.

## Configuration and development workflow

### Configuration level

At the configuration level, you define which keys a bot is subscribed to. This is done in the
infrastructure configuration file. The Botica Director reads this configuration and
sets up the necessary subscriptions when starting the bots:

Additionally, you can specify the strategy for how orders are delivered to the bot instances
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

In this example, example-bot instances will receive all orders sent with the `data_update` key
and request orders from the queue of the `tasks` key.

### Bot development level

When developing a bot, you focus on defining how it responds to specific actions. Since key
subscriptions are handled by the configuration, your main task is to register order listeners and
define how to handle the incoming orders.

#### Listening to orders

```js
import botica from "botica-lib-node";

const bot = await botica();

// Registering an order listener in a Node.js bot for the "process_data" action
bot.onOrderReceived((message) => {
  // Process the data contained in the message
  console.log("Processing data:", message);
}, "process_data");

// Now, for the "update_data" action
bot.onOrderReceived((message) => {
  console.log("Updating data:", message);
}, "update_data");

await bot.start();
```

In this example, the bot listens for the `process_data` and `update_data` actions and responds
accordingly. The `onOrderReceived` method is a simple way to tie specific actions to their
corresponding handlers.

#### Publishing orders

To publish an order from a bot, you need to specify the key, action, and payload.

```js
// Publishing a message in a Node.js bot
await bot.publish({id: 123, payload: "data"}, "tasks", "process_data");
```

Here, the bot publishes an order with the `tasks` key and the `process_data` action, sending
along some data in JSON format.

## Advanced: Simplifying development with defaults

To streamline the development process, Botica allows you to configure default keys and actions in
the bot's configuration. These defaults can be used to simplify the code by eliminating the need to
repeatedly specify the key and action.

### Registering an order listener with defaults

You can configure the bot to use a default action for its listeners, allowing you to write cleaner,
simpler code.

```yaml
bots:
  example-bot:
    subscribe:
      - key: "tasks"
        strategy: distributed
    lifecycle:
      type: reactive
      defaultAction: "process_data" # default action for listeners
    { ... }
```

In the bot code:

```js
// Registering a listener for the "process_data" default order
bot.onOrderReceived((message) => {
  console.log("Processing data:", message);
});
```

### Publishing orders with defaults

Similarly, you can configure default key and action values for publishing orders. This allows you
to publish orders without specifying these details every time.

```yaml
bots:
  example-bot:
    publish:
      defaultKey: "tasks"
      defaultAction: "process_data"
  { ... }
```

In the bot code:

```js
// Using defaults for publishing in a Node.js bot
await bot.publish({id: 123, payload: "data"});
```

In this example, the bot will automatically use the `task_updates` key and the `process_data` action
when publishing orders.

[<- Creating process chains](2-process-chains.md) | [Sharing files between bots ->](4-sharing-files-between-bots.md)

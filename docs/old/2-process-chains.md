# Creating process chains with Botica

One of the most powerful features of Botica is its ability to create sophisticated process chains.
These chains can involve multiple bots, each performing a specific step in the process. For example,
a workflow might begin with a proactive bot that initiates a task at regular intervals, which then
triggers a series of reactive bots, each responsible for the next step in the process. This approach
allows for the automation of complex, multi-step processes, with each bot handling a specific aspect
of the task.

Bots in Botica can subscribe to different keys, with various strategies that dictate how the
orders are delivered:

- **Distributed strategy**: with this strategy, each new order is delivered to one instance of the
  bot type that is subscribed to the key and available to process a new task. Bots request new
  orders from the queue when they are free, ensuring that tasks are assigned to bots that have
  completed their previous work. This approach is ideal for load balancing, as it ensures that no
  single instance becomes a bottleneck and tasks are efficiently distributed across available bots.

- **Broadcast strategy**: in contrast, the broadcast strategy delivers each new order to every
  instance of the bot type subscribed to the key. This is useful when you need all instances of a
  bot to act on the same information, such as updating a shared state or performing a synchronized
  action. Broadcast strategies are also beneficial when you want to enforce a synchronization point
  between multiple bots, ensuring that all bots reach a certain state or complete a specific task
  simultaneously.

These different strategies ensure that Botica can handle a wide variety of automation needs, from
simple task execution to the orchestration of complex, distributed workflows.

[<- The concept of a bot](1-the-concept-of-a-bot.md) | [Messaging between bots ->](3-messaging-between-bots.md)

# Botica overview

Botica is an advanced framework and platform designed to simplify the development, deployment, and
management of automated processes using containerized bots. By leveraging modern containerization
technologies and message brokering systems, Botica empowers developers to create scalable,
parallelized workflows that can automate complex tasks across various environments with minimal
overhead, with the following core goals:

- **Ease of development**: Botica abstracts away the complexities of setting up and managing the
  underlying infrastructure required for bot communication, scheduling, and lifecycle management.
  Developers can focus on writing the core logic of their bots without worrying about the
  intricacies of message brokers, schedulers, or network configurations.

- **Flexible automation**: Botica facilitates the creation of entire process chains, enabling
  developers to design workflows where tasks are performed in sequence or in parallel. Bots can be
  configured to trigger subsequent bots based on predefined conditions, creating a highly flexible
  and responsive automation pipeline.

- **Containerized execution**: each bot in the Botica ecosystem runs inside a container, ensuring a
  consistent and isolated execution environment. Botica currently supports Docker for container
  management, providing a seamless integration with container orchestration tools.

## Architecture

Botica operates with two primary components:

- **Botica Director**: this is the central management program that runs on the host machine. The
  Director handles all interactions with the Docker environment, setting up the necessary
  infrastructure, including the message broker (currently RabbitMQ), networks, and volumes. It
  manages the lifecycle of the bot containers, ensuring that they are correctly instantiated,
  monitored, and terminated as per the defined configuration.

- **Bots**: these are the worker units within Botica, designed and implemented by developers. Each
  bot runs inside a container and communicates with the Botica Director and other bots using the
  message broker. Bots can be configured to perform tasks either proactively (on a schedule) or
  reactively (in response to messages). Moreover, bots can publish and subscribe to multiple orders,
  allowing for complex inter-bot communication and orchestration.

## The concept of a bot

Bots are the core functional units within the Botica platform, responsible for executing tasks,
processing data, and driving automated workflows. Designed to be both powerful and easy to
implement, Botica bots enable developers to create robust, scalable automation solutions with
minimal effort.

In Botica, a bot is a containerized program that performs specific tasks within an automated
process. Bots can be developed using different programming languages and can interact with other
bots through a message broker. Each bot runs in its own container, ensuring a consistent and
isolated environment, which makes it easier to manage dependencies and scale the system.

### Capabilities of bots

Bots in Botica can perform a wide variety of tasks, ranging from simple operations like logging data
or sending notifications to complex processes involving data transformation, machine learning
inference, or multi-step workflows. These bots are able to:

- **Publish messages**: bots can publish messages to the message broker, which can trigger actions
  in other bots.

- **Subscribe to multiple orders**: bots can subscribe to multiple keys and orders, allowing them to
  listen for and respond to different types of messages. This capability is crucial for building
  complex workflows where a bot may need to react to various events or data inputs.

- **Mounting file systems**: bots can mount any part of the host's file system, giving them access
  to specific directories or files as needed.

- **Shared directory**: in addition to individual mounts, Botica provides a shared directory that
  allows bots to share files among each other. This is particularly useful for scenarios where
  multiple bots need to collaborate on the same data or state.

### Behavior and lifecycle

Bots in Botica can exhibit different behaviors depending on their lifecycle configuration:

- **Proactive lifecycle**: proactive bots operate on a schedule, executing tasks at regular
  intervals. These bots are ideal for scenarios where tasks need to be performed periodically, such
  as data generation or regular maintenance jobs.
  While primarily time-driven, proactive bots can also listen to and respond to incoming orders.

- **Reactive lifecycle**: reactive bots are triggered by specific events or messages. They listen
  for orders sent through the message broker and respond accordingly, making them perfect for tasks
  that need to be executed based on certain conditions or data availability.

- **Unmanaged lifecycle**: unmanaged bots are those that do not follow the typical lifecycle managed
  by Botica. This lifecycle type allows the inclusion of other types of images, such as external
  services (e.g., databases) that do not implement the Botica protocol but need to be part of the
  container network. The Botica Director creates and removes these unmanaged containers when Botica
  is started and stopped, ensuring they are available during the bot operation but not left running
  when they are no longer needed.

### Developing bots in Botica

One of Botica’s primary objectives is to simplify the development process for bots. Developers can
create bots using familiar programming languages and tools, while Botica handles the complexities of
infrastructure management. By providing libraries and a clear configuration structure, Botica
reduces the overhead associated with setting up message brokers, managing container lifecycles, and
orchestrating bot communication.

Creating a bot in Botica involves defining its behavior, lifecycle, and interaction with other bots
through a configuration file. Developers specify the bot's container image, environment variables,
and how it should publish or subscribe to messages. Botica’s libraries provide the necessary
abstractions, allowing developers to focus on the logic of their bots rather than the underlying
infrastructure.

Overall, Botica bots are designed to be versatile, easy to implement, and powerful enough to handle
complex automation tasks. Whether you need a bot that runs periodically, reacts to specific events,
or manages its own operations, Botica provides the tools and framework to support your automation
needs.

## Creating process chains with Botica

One of the most powerful features of Botica is its ability to create sophisticated process chains.
These chains can involve multiple bots, each performing a specific step in the process. For example,
a workflow might begin with a proactive bot that initiates a task at regular intervals, which then
triggers a series of reactive bots, each responsible for the next step in the process. This approach
allows for the automation of complex, multi-step processes, with each bot handling a specific aspect
of the task.

Bots in Botica can subscribe to different keys, with various strategies that dictate how the
messages are delivered:

- **Distributed strategy**: with this strategy, each new order is delivered to only one random
  instance of the bot type that has subscribed to the key. This is ideal for load balancing tasks
  across multiple instances of the same bot type, ensuring that no single instance becomes a
  bottleneck. For example, when tasks are distributed, a bot that picks up a task will remove it
  from the queue, allowing the next available bot to take on a different task, effectively balancing
  the load across the bots.

- **Broadcast strategy**: in contrast, the broadcast strategy delivers each new order to every
  instance of the bot type subscribed to the key. This is useful when you need all instances of a
  bot to act on the same information, such as updating a shared state or performing a synchronized
  action. Broadcast strategies are also beneficial when you want to enforce a synchronization point
  between multiple bots, ensuring that all bots reach a certain state or complete a specific task
  simultaneously.

These different strategies ensure that Botica can handle a wide variety of automation needs, from
simple task execution to the orchestration of complex, distributed workflows.

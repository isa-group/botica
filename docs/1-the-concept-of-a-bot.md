# The concept of a bot

Bots are the core functional units within the Botica platform, responsible for executing tasks,
processing data, and driving automated workflows. Designed to be both powerful and easy to
implement, Botica bots enable developers to create robust, scalable automation solutions with
minimal effort.

In Botica, a bot is a containerized program that performs specific tasks within an automated
process. Bots can be developed using different programming languages and can interact with other
bots through a message broker. Each bot runs in its own container, ensuring a consistent and
isolated environment, which makes it easier to manage dependencies and scale the system.

## Capabilities of bots

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

You can read more on messaging between bots [here](3-messaging-between-bots).

## Behavior and lifecycle

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

## Developing bots in Botica

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

[<- Getting started](0-getting-started.md) | [Creating process chains ->](2-process-chains.md)

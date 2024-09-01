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

## [Check out our documentation!](docs/getting-started.md)

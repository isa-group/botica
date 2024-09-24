# The infrastructure configuration file

## Contents

1. [Overview](#overview)
2. [Docker top-level element](#docker-top-level-element)
    1. [host](#host)
3. [Broker top-level element](#broker-top-level-element)
    1. [type](#type)
    2. [RabbitMQ configuration properties](#rabbitmq-configuration-properties)
4. [Shutdown top-level element](#shutdown-top-level-element)
5. [Bots top-level element](#bots-top-level-element)
6. [Bot type object](#bot-type-object)
    1. [image](#image)
    2. [mount](#mount)
    3. [publish](#publish)
    4. [subscribe](#subscribe)
    5. [lifecycle](#lifecycle)
    6. [instances](#instances)
        1. [lifecycle](#lifecycle-1)
        2. [environment](#environment)

## Overview

> [!NOTE]
> Botica currently supports both YAML and JSON configuration file formats. This page shows the
> Botica configuration file specification in YAML, which is recommended, although a JSON
> configuration file would follow this same specification.

You can see the full example configuration files here:

* [YAML](../botica-director/src/main/resources/config.yml)
* [JSON](../botica-director/src/main/resources/config.json)

## Docker top-level element

The top-level `docker` property allows configuring the docker host URI.

### host

`host` specifies the docker URI to connect to for deploying the infrastructure. If missing, the
default URI for the running OS is chosen.

```yaml
docker:
  host: "unix:///var/run/docker.sock"
```

Defaults:

- Unix: `unix:///var/run/docker.sock`
- Windows: `npipe:////./pipe/docker_engine`

## Broker top-level element

The top-level `broker` property specifies the broker type and configuration to use.

### type

`type` specifies the broker technology to use. The only broker supported currently is `rabbitmq`.

```yaml
broker:
  type: rabbitmq
```

The remaining `broker` properties vary depending on the broker technology selected.
> [!NOTE]
> The broker instance is provided and deployed by Botica: your system does not need to have a
> running broker instance, and the configuration (authentication, port...) doesn't have to (and, in
> case of `port`, should not) match them.

### RabbitMQ configuration properties

#### username

The username used for the broker authentication.

```yaml
username: "username"
```

#### password

The password used for the broker authentication.

```yaml
password: "password"
```

#### port

The port to expose the provided broker instance to your host system. Defaults to `5672`.

```yaml
port: 5672
```

## Shutdown top-level element

### timeout

The amount of time, in milliseconds, the director waits before considering that a bot has timed out
in responding to a shutdown request. Defaults to `10000` milliseconds (10 seconds).

```yaml
shutdown:
  timeout: 10000
```

## Bots top-level element

The top-level `bots` property contains all the bot type objects providing their configurations to
launch Botica.

```yaml
bots:
  bot_1: { ... }
  bot_2: { ... }
  bot_3: { ... }
```

## Bot type object

### image

The container image of the bot type.

```yaml
bots:
  bot_name:
    image: "container_image"
```

### mount

The list of the directories or files from the host system to mount on the file systems of the
containers of the bot type. Every mount element specifies the `source` (on the host file system) and
the `target` (on the container file system) of the directory or file. If `source` does not exist on
the host file system and `createHostPath` is `true` (defaults to `false`), an empty directory will
be created.

```yaml
bots:
  bot_name:
    mount:
      - source: "path/to/host/file.extension"
        target: "path/to/container/file.extension"
      - source: "path/to/host/directory"
        target: "path/to/container/directory"
        createHostPath: true
```

### publish

The publish configuration for the bot type. If your bot type does not publish any messages, you can
skip this section.

```yaml
bots:
  bot_name:
    publish:
      key: "publish_key"
      order: "publish_order"
```

#### key

The key of the message that the bot will send after completing its job.

#### order

The order that the bot will execute after completing its job.

### subscribe

The subscribe configuration defines the keys that a bot type subscribes to. If your bot type does
not need to subscribe to any key, you can skip this section.

```yaml
bots:
  bot_name:
    subscribe:
      - key: "a_distributed_key"
        strategy: distributed
      - key: "another_distributed_key"
        strategy: distributed
      - key: "broadcast_key"
        strategy: broadcast
```

#### key

The key to subscribe to.

#### strategy

The strategy defines how orders are delivered to the bots. Defaults to `distributed`.
`distributed` subscriptions will deliver every new order to only one random instance of the bot
type, while `broadcast` subscriptions will deliver every new order to each instance.

### lifecycle

`lifecycle` specifies how the instaces of this bot type will behave: when they will run and how.

#### type

The type of the lifecycle. Supported values:

* `proactive`: the bot will run every `period` seconds after `initialDelay` seconds:

```yaml
bots:
  bot_name:
    lifecycle:
      type: proactive
      initialDelay: 10 # defaults to 0
      period: 60 # defaults to 1
```

If `period` is set to `-1`, the action will execute once. The bot will then automatically shut down
if there are no active user threads remaining.

* `reactive`: the bot will run when it receives a message with the given `order` to one of the
  subscribed `keys`.

```yaml
bots:
  bot_name:
    lifecycle:
      type: reactive
      order: "subscribe_order"
```

* `unmanaged`: the image is not a Botica bot and manages its own lifecycle. The director will not
  try to communicate with this container, but it will be connected to the same network as the other
  bots.

```yaml
bots:
  bot_name:
    lifecycle:
      type: unmanaged
```

### instances

The instances of this bot type that Botica will deploy.

```yaml
bots:
  bot_name:
    instances:
      bot_1:
        environment:
          - KEY=VALUE
      bot_2:
        lifecycle:
          type: proactive
          initialDelay: 30
          period: 30
        environment:
          - KEY=VALUE
      bot_3: { }
      bot_4: { }
```

#### lifecycle

Option to override the [lifecycle configuration of the bot's type](#lifecycle).

#### environment

The list of the environment variables to pass to the bot's container.

[<- Sharing files between bots](sharing-files-between-bots.md) | [Example projects ->](example-projects.md)

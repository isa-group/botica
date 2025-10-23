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
    2. [replicas (optional)](#replicas-optional)
    3. [mount (optional)](#mount-optional)
    4. [publish (optional)](#publish-optional)
    5. [subscribe (optional)](#subscribe-optional)
    6. [lifecycle (optional)](#lifecycle-optional-defaults-to-reactive)
    7. [ports (optional)](#ports-optional)
    8. [environment (optional)](#environment-optional)
    9. [instances (optional)](#instances-optional)
        1. [lifecycle (optional)](#lifecycle-optional)
        2. [ports (optional)](#ports-optional-1)
        3. [environment (optional)](#environment-optional-1)

## Overview

> [!NOTE]
> Botica currently supports both YAML and JSON configuration file formats. This page shows the
> Botica configuration file specification in YAML, which is recommended, although a JSON
> configuration file would follow this same specification.

You can see the full example configuration files here:

* [YAML](../botica-director/src/main/resources/example-environment-file.yml) - with comments for
  every section
* [JSON](../botica-director/src/main/resources/example-environment-file.json)

---

## Docker top-level element

The top-level `docker` property allows configuring the docker host URI. If missing, the default
values will be used.

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

---

## Broker top-level element

The top-level `broker` property specifies the broker type and configuration to use.

> [!NOTE]
> The broker instance is provided and deployed by Botica: your system does not need to have a
> running broker instance, and the configuration (authentication, port...) doesn't have to (and, in
> case of `port`, should not) match them.

If the `broker` property is missing in the configuration file, `rabbitmq` will be used by default
with random values for username and password.

### type

`type` specifies the broker technology to use. The only broker supported currently is `rabbitmq`.

```yaml
broker:
  type: rabbitmq
```

The remaining `broker` properties vary depending on the broker technology selected.

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

---

## Shutdown top-level element

### timeout

The amount of time, in milliseconds, that the director waits before considering that a bot has timed
out in responding to a shutdown request.

A bot may take some time to respond to a shutdown request if it needs to save data to a file or
database before shutting down.

Defaults to `5000` milliseconds (5 seconds).

```yaml
shutdown:
  timeout: 5000
```

---

## Bots top-level element

The top-level `bots` property contains all the bot type objects providing their configurations to
launch Botica.

```yaml
bots:
  bot_type_1: { ... }
  bot_type_2: { ... }
  bot_type_3: { ... }
```

---

## Bot type object

### image

The container image of the bot type.

```yaml
bots:
  my_bot_type:
    image: "container_image"
```

### replicas (optional)

The number of bots of this type to deploy. The bot instances will be named
`%bot_type%-%replica_number%` (e.g.: `my_bot_type-1`, `my_bot_type-2`, `my_bot_type-3`). Defaults to
`1` if no [instances](#instances-optional) are defined.

```yaml
bots:
  my_bot_type:
    replicas: 3
```

### mount (optional)

The list of the directories or files from the host system to mount on the file systems of the
containers of the bot type. Every mount element specifies the `source` (on the host file system) and
the `target` (on the container file system) of the directory or file. If `source` does not exist on
the host file system and `createHostPath` is `true` (defaults to `false`), an empty directory will
be created.

```yaml
bots:
  my_bot_type:
    mount:
      - source: "path/to/host/file.extension"
        target: "path/to/container/file.extension"
      - source: "path/to/host/directory"
        target: "path/to/container/directory"
        createHostPath: true
```

### subscribe (optional)

The subscribe configuration defines the keys that a bot type subscribes to. If your bot type does
not need to subscribe to any key, you can skip this section.

```yaml
bots:
  my_bot_type:
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
`distributed` subscriptions will deliver every new order to only one random available instance of
the bot type, while `broadcast` subscriptions will deliver every new order to each instance. Learn
more in [this page](3-messaging-between-bots.md).

### lifecycle (optional, defaults to reactive)

`lifecycle` specifies how the instances of this bot type will behave: when they will run and how.

#### type

The type of the lifecycle. Supported values:

* `proactive`: the main function of the bot will run every `period` seconds after `initialDelay`
  seconds:
    ```yaml
    bots:
      my_bot_type:
        lifecycle:
          type: proactive
          initialDelay: 10 # defaults to 0
          period: 60 # defaults to 1
    ```
  If `period` is set to `-1`, the action will execute once. The bot will then automatically shut
  down if there are no active user threads remaining.


* `reactive`: the bot will run when it receives an order with the given `action` to one of the
  subscribed `keys`. This is the default value if the whole lifecycle section is missing.
    ```yaml
    bots:
      my_bot_type:
        lifecycle:
          type: reactive
          defaultAction: "subscribe_order"  # (optional) default value for order subscriptions if not specified in code
    ```

* `unmanaged`: the image is not a Botica bot and manages its own lifecycle. The director will not
  try to communicate with this container, but it will be connected to the same network as the other
  bots.

    ```yaml
    bots:
      my_bot_type:
        lifecycle:
          type: unmanaged
    ```

### publish (optional)

The optional default publish configuration for the bot type. If your bot publishes an order without
specifying key or action, they will be taken from this section.

```yaml
bots:
  my_bot_type:
    publish:
      defaultKey: "default_publish_key"
      defaultAction: "default_publish_order"
```

### ports (optional)

Exposes container ports to the host machine. This is a list of strings, where each string specifies
a port mapping.

> [!WARNING]
> The underlying `docker-java` library supports a subset of the formats accepted by the Docker CLI.
> **Port ranges (e.g., `3000-3005`) are not supported.**

The supported formats are:

- `<containerPort>` (e.g., `"3000"`)
- `<hostPort>:<containerPort>` (e.g., `"8080:80"`)
- `<ip>::<containerPort>` (e.g., `"127.0.0.1::80"`)
- `<ip>:<hostPort>:<containerPort>` (e.g., `"127.0.0.1:8080:80"`)

You can also specify the protocol (defaults to `tcp`):

- `<port>:<port>/udp` (e.g., `"6060:6060/udp"`)

```yaml
bots:
  my_bot_type:
    ports:
      - "8080:80"
      - "6060:6060/udp"
```

### environment (optional)

The list of the environment variables to pass to the bot containers.

```yaml
bots:
  my_bot_type:
    environment:
      - KEY1=VALUE1
      - KEY2=VALUE2
```

### instances (optional)

The `instances` property allows you to define distinct bot configurations beyond those managed by
the [replicas property](#replicas-optional). It is used to create custom, individually configured
instances of any bot type, either to override default settings or to add unique configurations.

These can be combined with the `replicas` property. Keep in mind that having two instances with the
same name, even instances from different bot types, will throw an error.

```yaml
bots:
  my_bot_type:
    replicas: 3
    environment:
      - KEY1=VALUE1
      - KEY2=VALUE2
    instances:
      my_bot_type-4: # my_bot_type-1, my_bot_type-2 and my_bot_type-3 will be created (see replicas above)
        environment:
          - KEY3=VALUE3 # added "KEY3" variable
      my_bot_type-5:
        environment:
          - KEY2=OVERRIDEN_VALUE_FROM_TYPE # overridden "KEY2" variable
```

#### lifecycle (optional)

Overrides the [lifecycle configuration of the bot's type](#lifecycle-optional-defaults-to-reactive)
for this specific instance.

```yaml
bots:
  my_bot_type:
    lifecycle:
      type: proactive
      initialDelay: 10
      period: 60
    instances:
      bot_1:
        lifecycle:
          type: proactive
          initialDelay: 20 # overriding initialDelay from parent
          period: 120 # overriding period from parent
```

#### ports (optional)

Overrides the [ports configuration](#ports-optional) for this specific instance. The list of ports
defined here will be appended to the list defined at the bot type level.

```yaml
bots:
  my_bot_type:
    # All instances of this type will expose port 8080 on the host, mapped to container port 80
    ports:
      - "8080:80"
    instances:
      bot_1:
        # This instance will expose both ports 8080 and 9090 on the host, mapping to container ports 80 and 90.
        ports:
          - "9090:90"
```

#### environment (optional)

Adds to or overrides the list of [environment variables](#environment-optional) from the bot type.

```yaml
bots:
  my_bot_type:
    environment:
      - KEY1=VALUE1
      - KEY2=VALUE2
    instances:
      bot_1:
        environment:
          - KEY3=VALUE3 # added "KEY3" variable
      bot_2:
        environment:
          - KEY2=OVERRIDEN_VALUE_FROM_TYPE # overridden "KEY2" variable
```

[<- Sharing files between bots](4-sharing-files-between-bots.md) | [Example projects ->](example-projects.md)

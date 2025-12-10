# Botica: the multi-bot collaboration framework

Build, deploy, and scale your collaborative software bots with ease. Botica provides the framework
for development and the infrastructure for orchestration.

![Botica Overview Diagram](docs/assets/overview.svg)

Botica provides a collaboration framework designed to streamline the **development** and
**orchestration** of multi-bot systems. It allows you to define your entire bot infrastructure,
including bot types, instances, and communication patterns, within a **single declarative
configuration file**.

Upon interpreting this configuration, Botica automatically **provisions and manages** the underlying
**message broker**, the necessary **network infrastructure**, the **deployment of each bot** as an
isolated container and the **connections to the message broker and their complete lifecycle**:

![Botica Architecture Diagram](docs/assets/architecture.svg)

## Features

- **Easy development:** Official libraries for **Java** and **Node.js** (with **Python** and more
  planned) provide simplified APIs for building robust and collaborative bots.
- **Declarative deployment:** Define your multi-bot environment in a single YAML file, eliminating
  manual Docker commands and complex scripting.
- **Simplified messaging:** Bots interact via "orders"; Botica abstracts away the underlying message
  broker logic and message routing complexities.
- **Integrated scalability:** Scale bot instances by modifying a single `replicas` value, supporting
  both distributed (work-stealing) and broadcast messaging strategies.
- **Shared filesystem:** A common `/shared` volume is automatically mounted across all bot
  containers for seamless data exchange.
- **Automated lifecycle management:** Comprehensive handling of bot startup, readiness checks,
  heartbeats, and graceful shutdowns ensures system resilience.

## How it works

This example demonstrates how Botica orchestrates a simple workflow: a **Node.js generator bot**
periodically publishes random data, which is then processed by a scalable team of
**Java worker bots**. The setup involves defining the bots in an `environment.yml` file and then
implementing each bot's specific logic using the respective Botica library.

### 1. Environment configuration (`environment.yml`)

```yml
bots:
  generator-bot:
    image: "my-org/generator-bot-node" # Your Docker image for the Node.js bot
    replicas: 1 # Botica deploys 1 instance of this bot type
    lifecycle:
      type: proactive
      period: 5 # Executes its proactive task every 5 seconds

  worker-bot:
    image: "my-org/worker-bot-java" # Your Docker image for the Java bot
    replicas: 3 # Botica deploys 3 instances of this bot type
    lifecycle:
      type: reactive
    subscribe:
      - key: "worker_jobs"
        strategy: distributed # Each order with "worker_jobs" key is sent to only one of the 3 workers
```

This configuration defines two bot types: `generator-bot` (Node.js) and `worker-bot` (Java).

- The `generator-bot` is configured as `proactive` to run a task every 5 seconds.
- The `worker-bot` is configured as `reactive` and `subscribes` to the `worker_jobs` key with a
  `distributed` strategy, ensuring that each incoming order is processed by only one of its three
  `replicas`.

Botica translates this configuration into a running, coordinated multi-bot system.

### 2. Node.js data generator bot

```ts
import botica from "botica-lib-node";
import {randomUUID} from "crypto";

const bot = await botica();

bot.proactive(async () => {
  const data = {
    id: randomUUID(),
    timestamp: new Date().toISOString(),
    value: Math.random() * 100
  };

  console.log(`Generated and publishing data: ${JSON.stringify(data)}`);
  await bot.publishOrder("worker_jobs", "process_data", data);
});

await bot.start();
```

This bot's logic uses [botica-lib-node](https://github.com/isa-group/botica-lib-node) to define a
proactive task. It generates a unique data payload every 5 seconds (as configured in
`environment.yml`) and publishes it as an order with the action `process_data` to the `worker_jobs`
key.

### 3. Java data worker bot

```java
import es.us.isa.botica.bot.BaseBot;
import es.us.isa.botica.bot.OrderHandler;
import org.json.JSONObject;

public class WorkerBot extends BaseBot {

  @OrderHandler("process_data")
  public void onProcessData(JSONObject payload) { // Order handler methods can accept String, JSONObject or any POJO types
    String processedResult = 
        "Processed-" + payload.getString("id") + "-" + payload.getDouble("value") * 2;

    System.out.println("Worker " + getBotId() + " finished processing: " + processedResult);
  }
}
```

This bot's logic uses [botica-lib-java](https://github.com/isa-group/botica-lib-java) to define an
`@OrderHandler` method. When an order with the `process_data` action arrives on a subscribed key (in
this case, `worker_jobs`), this method is automatically invoked to simulate data processing and log
the result.

### 4. Build and run the system

Once your bot code is implemented and your `environment.yml` is defined:

1. **Build your bot images**: For each bot project (e.g., `generator-bot`, `worker-bot`), use the
   `build.sh` (Linux/macOS) or `build.bat` (Windows) script provided in the official library
   templates to build your Docker images. These scripts automatically handle compilation and Docker
   image tagging.
2. **Run the Botica Director**: Navigate to the directory containing your `environment.yml` file and
   execute the Botica Director program. The Director will read your configuration, deploy the
   message broker and all bot containers, and manage their interactions.

To gracefully shut down the entire system, simply stop the Botica Director program with the `stop`
command. It will ensure all bots and associated container infrastructure are terminated cleanly.

## Getting started

To begin developing with Botica:

- **[Explore core concepts](docs/2-core-concepts/1-the-botica-environment.md)** to understand the
  foundational principles.
- **[Follow the getting started guide](docs/1-getting-started.md)** for installation and your
  first deployment.
- **[Review example projects](docs/5-example-projects.md)** for practical implementations.

### [Read full documentation, detailed guides and example projects](docs/0-index.md)

## Libraries

- [**botica-lib-java**](https://github.com/isa-group/botica-lib-java) - The official Java library
  for building Botica bots.
- [**botica-lib-node**](https://github.com/isa-group/botica-lib-node) - The official Node.js library
  for building Botica bots.

## License

This project is distributed under the LGPL v2.1 License.

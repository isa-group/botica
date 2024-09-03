# Example projects

This page provides links to example projects that showcase how Botica can be used to create
automated, containerized workflows.

## The Botica fishbowl

This project serves as a simple demonstration of Botica's capabilities. In this project, a
9x9 fishbowl is simulated with multiple fish bots that move around and a manager bot that
tracks their positions. The fish bots, implemented in both Java and Node.js, are proactive; they
periodically send their positions within the fishbowl. The manager bot, written in Java, is
reactive. It listens for updates from the fish bots, logs the current state of the fishbowl, and
periodically saves this state to files.

This project illustrates how Botica can manage and orchestrate bots written in different programming
languages within the same infrastructure. It also demonstrates proactive and reactive bot behaviors
and how bots can interact with the file system, a crucial aspect of many automation tasks.

[You can explore the Fishbowl project here](https://github.com/isa-group/botica-infrastructure-fishbowl)

# Real-world projects

Botica is also used in real-world applications to automate complex workflows and processes. Below is
an example of how Botica can be applied in a professional setting.

## REST API testing with RESTest

The `botica-infrastructure-restest` project is a real-world example that automates the process of
testing REST APIs using the RESTest framework. In this infrastructure, a complete process chain is
established, involving several bots that work together to generate, execute, and report on REST API
tests.

The process begins with generator bots, each responsible for a specific API. These bots use the
provided OpenAPI specification for each API to generate test cases, expected results, and test
classes that are ready for execution. Once the test cases are prepared, the generator bots publish
an order to trigger the next phase of the process. Executor bots then listen for these orders. When
an order is received, they take the generated test classes, run them against the API, and save the
results. Finally, reporter bots compare the actual results with the expected outcomes, generate
detailed analytics, and produce an Allure dashboard for visualization.

A key feature of this infrastructure is that the executor and reporter bots listen to message keys
using the distributed strategy. This means that each bot instance takes a message from the queue as
soon as it becomes available, ensuring that tasks are evenly distributed among the available bot
instances. This approach allows the infrastructure to handle a large volume of tasks efficiently by
parallelizing the work, as each bot keeps getting orders from the queue and processes them
independently.

This project showcases how Botica can handle distributed, parallelized work, significantly speeding
up complex processes like API testing. It also illustrates the use of Botica in managing and
automating real-world tasks in a professional environment.

[You can explore the Botica RESTest infrastructure here](https://github.com/isa-group/botica-infrastructure-restest)

[<- The infrastructure configuration file](the-infrastructure-configuration-file.md)

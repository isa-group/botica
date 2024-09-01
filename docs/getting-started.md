# Getting started with Botica

Welcome to Botica! This guide will help you get started with setting up your Botica environment,
creating bots, and configuring your infrastructure. By the end of this guide, you'll have a fully
functional Botica environment running your custom bots.

## Overview

Botica simplifies the process of setting up automated, containerized workflows by providing a
structured approach to managing bots and their environments. There are two main components in a
Botica environment:

- **Infrastructure**: this is the backbone of your Botica environment. It includes the
  infrastructure
  configuration, which defines what bots will be deployed and how.
  The infrastructure also contains any scripts or resources necessary for deploying and managing the
  environment.
- **Bots**: these are the individual units of work in Botica. Bots are containerized programs that
  perform specific tasks. They are developed using the Botica libraries for various programming
  languages, and each bot is associated with its own repository.

## Documentation
1. [The concept of a bot](the-concept-of-a-bot.md)
2. [Messaging between bots](messaging-between-bots.md)
3. [The infrastructure configuration file](the-infrastructure-configuration-file.md)

## Setting up your Botica environment

### Step 1: create your bots

The first step in setting up your Botica environment is to create the bots that will perform tasks
within your infrastructure. Botica provides official seeds (templates) for various programming
languages that simplify the process of creating bots.

Currently, Botica offers libraries for the following languages:

- **Java**: through the [botica-lib-java](https://github.com/isa-group/botica-lib-java/) library.
  [Seed available here](https://github.com/isa-group/botica-seed-java/).
- **Node.js**: through [botica-lib-node](https://github.com/isa-group/botica-lib-node/) library.
  Contains definitions for Typescript projects.
  [Seed available here](https://github.com/isa-group/botica-seed-node/).

To get started with your bot development:

1. Choose the appropriate seed for your preferred programming language.
2. Follow the instructions in the seed's documentation to create a new bot repository.
3. Customize the bot code and configurations according to your needs.
4. Build the Docker image for your bot using the provided scripts.

### Step 2: create your infrastructure

With your bots ready, the next step is to create the infrastructure that will host and manage these
bots. Botica provides an
[official infrastructure template](https://github.com/isa-group/botica-infrastructure/) to
streamline the setup process.

This template contains:

- **The infrastructure configuration file**: The `config.yml` file that defines the bot types,
  instances, and other configurations specific to your environment.
- **Assets**: directories and files to be mounted into the bots as defined in the configuration.
- **Deployment script**: a script that downloads the latest version of the Botica Director and runs
  the environment based on the infrastructure configuration.

### Step 3: running your Botica infrastructure

After setting up your bots and infrastructure:

1. Ensure that all your bot images are built and available (either locally or in a Docker registry).
2. Navigate to your infrastructure repository.
3. Run the deployment script provided in the infrastructure seed. This script will download the
   Botica Director and start your environment based on the configurations in config.yml.

Once your Botica infrastructure is running, you can monitor the status of your bots through the
Docker environment or any logging mechanisms you have implemented. Bots will execute their tasks
according to the configurations and interactions defined in your setup.

As Botica continues to evolve, you can expect more seeds and libraries for additional programming
languages, further expanding the platform's versatility.

For detailed documentation on specific language seeds and how to use them, please refer to the
respective documentation links provided in the seed repositories.

[The concept of a bot ->](the-concept-of-a-bot.md)

# Getting started

This guide will walk you through setting up the Botica environment on your local machine, from
installing the prerequisites to running the Botica Director for the first time.

## 1. Prerequisites

### Install Docker

Botica requires a running Docker engine to deploy and manage the containerized infrastructure for
your bots and the message broker. The recommended way to set this up is by **[installing Docker
Desktop](https://www.docker.com/products/docker-desktop/)**.

> [!NOTE]
> **Platform-specific Docker configuration**
>
> Botica is designed to connect to the Docker engine out-of-the-box, provided Docker Desktop is
> running. It automatically detects the active Docker daemon connection for your operating system.
>
> In short, if you can run `docker ps` successfully in your terminal, the Botica Director should be
> able to connect to Docker without any additional configuration.

### Install Java (JDK 11 or newer)

The Botica Director is a Java application and requires a Java Development Kit (JDK) version 11 or
newer to run.

- **Windows / macOS**: We recommend installing a JDK from a reputable provider like
  **[Eclipse Adoptium](https://adoptium.net/temurin/releases/)** (Temurin). Choose the latest LTS
  (Long-Term Support) version available (e.g., JDK 17, JDK 21).
- **Linux**: Follow your distribution's official package manager instructions to install the latest
  stable OpenJDK. For example:
    - **Debian or Ubuntu**:
      ```bash
      sudo apt update
      sudo apt install openjdk-21-jdk
      ```
    - **Fedora, Rocky Linux or Red Hat Enterprise Linux**:
      ```bash
      sudo dnf install java-latest-openjdk
      ```

## 2. Installing the Botica Director

The **Botica Director** is the command-line tool that orchestrates your entire multi-bot environment
based on your `environment.yml` configuration file.

### Recommended method: using the executable

The easiest and recommended way to use the Director is via the platform-specific executable
wrappers (`botica-director` for Linux/macOS, `botica-director.cmd` for Windows). These lightweight
scripts are preferred because they automatically manage Director updates, ensuring you are always
running the latest version without manual intervention, and also check for a compatible Java
runtime.

1. **Download the appropriate executable** for your operating system from the [**latest Botica
   release on GitHub**](https://github.com/isa-group/botica/releases/latest).
    - For Linux or macOS: `botica-director`
    - For Windows: `botica-director.cmd`
2. **Place the downloaded file** in the root directory of your project. This is where your
   `environment.yml` file will live.
3. **(For Linux/macOS only) Make the file executable:** Open a terminal in your project directory
   and run the following command:
   ```bash
   chmod +x botica-director
   ```

### Alternative method: using the JAR file

If you prefer to manage the JAR file directly, you can download the `botica-director-X.Y.Z.jar` file
from the releases page and run it using a compatible Java runtime (
`java -jar botica-director-X.Y.Z.jar`). While the JAR itself contains update logic, manual execution
means you would need to re-run the command after an update has been applied.

## 3. Your first run

With the Director executable in your project's root directory, you are ready for your first
interaction.

### Creating the default environment file

The very first time you run the Director in a directory without an `environment.yml` file, it will
create one and then shut down. This allows you to configure your environment before attempting to
deploy anything.

```bash
# On Linux or macOS
./botica-director

# On Windows
botica-director.cmd
```

You should see output similar to this:

```text
HH:MM:SS.ms INFO  No environment file found. A default environment file has been created at /path/to/your/project/environment.yml
```

After this, the Director will exit.

### Starting the Botica environment

Once `environment.yml` has been created (and you've optionally modified it), you can run the
Director again to start your Botica environment.

```bash
# On Linux or macOS
./botica-director

# On Windows
botica-director.cmd
```

This time, the Director will read your `environment.yml` and attempt to set up the infrastructure.
If you run it with the default `environment.yml` (which references generic, non-existent bot
images), the Director will try to deploy the message broker and then the bots. It will report an
error for missing bot images and then gracefully shut down the entire container infrastructure it
just created.

You will see output similar to the following:

```text
HH:MM:SS.ms ERROR Docker image 'my-bot-container-image:latest' (used by 'my-bot' bots) was not
found.

Please verify that the image name or tag is correct in the bot's configuration.
Ensure the image has either been built locally (if it's a custom bot) or pushed to a public or
accessible private registry (e.g., Docker Hub).
```

This behavior confirms that Botica is connected to Docker and is attempting to follow your
configuration, even if the referenced bot images don't yet exist.

## 4. Integrating with Git

For effective version control of your Botica project, we recommend the following setup:

1. **Commit the `botica-director` executables** directly into your Git repository. They are very
   lightweight and ensure that all team members use the same version and update mechanism.
2. **Add the `.botica/` directory to your `.gitignore` file.** This directory is managed by the
   Director to store downloaded JARs and other runtime artifacts, which should not be part of your
   repository.

Your `.gitignore` file should contain:

```text
# Botica
.botica/
```

A typical Botica project repository will contain:

```text
.
├── .gitignore
├── botica-director
├── botica-director.cmd
├── environment.yml
└── (other project files, like configuration files to mount in bot containers, etc.)
```

## Next steps

Congratulations, you have successfully set up your Botica environment!

- To understand the fundamental concepts behind Botica, head over to
  **[Core Concepts](./2-core-concepts/0-the-botica-environment.md)**.
- Now, you can start defining your bots in the `environment.yml` file. To learn about all the
  available configuration options, refer to the
  **[Environment File Reference](./4-reference/0-the-environment-file.md)**.
- To quickly get started with developing your first bot:
    - **[Your First Java Bot](./3-guides/1-your-first-bot-java.md)**
    - **[Your First Node.js Bot](./3-guides/2-your-first-bot-node.md)**

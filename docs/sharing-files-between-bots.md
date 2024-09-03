# Sharing files between bots

Botica makes it easy to share files and data between bots by providing a default `/shared`
directory, accessible to all bot containers. This shared directory is especially useful for handling
large files, binaries, or any data that’s not practical to send through messaging orders.

## The shared directory

In every Botica infrastructure, a `/shared` directory is automatically set up as a Docker volume and
mounted in all bot containers. This common space allows bots to exchange files seamlessly, whether
it's for large datasets or complex data formats.

## Accessing the shared directory

While the physical path is always `/shared`, Botica libraries offer convenient ways to access this
directory without hardcoding the path.

- In `botica-lib-java`, you can retrieve the shared directory with the `getSharedDirectory()` method
  from `AbstractBotApplication`, which returns a `java.io.File` object pointing to the shared
  directory.
- In `botica-lib-node`, the path is provided as a `SHARED_DIRECTORY` constant, which you can import
  directly from the library.

## Keep in mind

- **Ensure data consistency**: manage potential conflicts if multiple bots are accessing the same
  files.
- **Use provided tools**: always use the methods or constants provided by the libraries to access
  the shared directory, to keep the bot's code clean and adaptable.
- **Clean up**: if your bots create temporary files, clean them up to avoid clutter in the shared
  directory.

[<- Messaging between bots](messaging-between-bots.md) | [The infrastructure configuration file ->](the-infrastructure-configuration-file.md)

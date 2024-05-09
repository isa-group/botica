# BOTICA

## Installing BOTICA as a local dependency

1. **Clone or download the project from the repository.**

2. **Compiling the project.**

    Open a terminal at the project's root and run the following Maven command to compile and package the project.
    ```
    mvn clean install
    ```

3. **Adding dependency to your project.**

    1. Open the `pom.xml` file of your project.

    2. Inside the <dependencies> section, add the following entry:

        ```xml
        <dependency>
          <groupId>io.github.isa-group</groupId>
          <artifactId>botica</artifactId>
          <version>1.0-SNAPSHOT</version>
        </dependency>
        ```

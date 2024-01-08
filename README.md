# BOTICA

## Installing BOTICA as a local dependency

1. **Clone or download the project from the repository.**

2. **Compiling the project.**

    Open a terminal at the project's root and run the following Maven command to compile and package the project.
    ```
    mvn clean install
    ```

3. **Installing the project in your Local Maven Repository.**

    Run the following maven command in the `target` folder of your project:
    ```
    mvn install:install-file -Dfile=botica.jar -DgroupId=com.botica -DartifactId=botica -Dversion=0.1.0 -Dpackaging=jar
    ```

4. **Adding dependency to your project.**

    1. Open the `pom.xml` file of your project.

    2. Inside the <dependencies> section, add the following entry:

        ```xml
        <dependency>
          <groupId>com.botica</groupId>
          <artifactId>botica</artifactId>
          <version>0.1.0</version>
        </dependency>
        ```
## Uninstalling BOTICA as a local dependency

Run the following maven command:
```
mvn dependency:purge-local-repository -DmanualInclude=com.botica:botica:0.1.0
```

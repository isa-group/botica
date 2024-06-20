package es.us.isa.botica.director;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import es.us.isa.botica.director.broker.DockerRabbitMqDeploymentHandler;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.deploy.DockerBotDeploymentHandler;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import java.io.File;

public class DirectorBootstrap {
  private static final File DEFAULT_MAIN_CONFIGURATION_FILE = new File("config.yml");

  public static void main(String[] args) {
    ConfigurationFileLoader loader = new JacksonConfigurationFileLoader();
    File configFile = new File("config.yml");
    MainConfiguration mainConfiguration = loader.load(configFile, MainConfiguration.class);

    DockerClient dockerClient = buildDockerClient();
    DockerRabbitMqDeploymentHandler brokerDeploymentHandler =
        new DockerRabbitMqDeploymentHandler(dockerClient, mainConfiguration);
    BotDeploymentHandler botDeploymentHandler =
        new DockerBotDeploymentHandler(configFile, mainConfiguration, dockerClient);

    new Director(brokerDeploymentHandler, botDeploymentHandler, mainConfiguration).init();
  }

  private static DockerClient buildDockerClient() {
    DockerClientConfig dockerConfig =
        DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    // TODO make configurable!
    return DockerClientBuilder.getInstance(dockerConfig)
        .withDockerHttpClient(
            new ApacheDockerHttpClient.Builder().dockerHost(dockerConfig.getDockerHost()).build())
        .build();
  }
}

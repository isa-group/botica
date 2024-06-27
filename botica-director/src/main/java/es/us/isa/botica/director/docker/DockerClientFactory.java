package es.us.isa.botica.director.docker;

import static com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder;
import es.us.isa.botica.configuration.docker.DockerConfiguration;
import es.us.isa.botica.director.exception.ConnectException;

public final class DockerClientFactory {
  private DockerClientFactory() {}

  /**
   * Creates a {@link DockerClient} with the docker host specified in the given configuration. If
   * the host is not specified, the docker-java default URI will be used.
   *
   * @param dockerConfiguration the docker configuration to use
   * @return the docker client
   */
  public static DockerClient createDockerClient(DockerConfiguration dockerConfiguration) {
    DefaultDockerClientConfig.Builder clientConfigBuilder = createDefaultConfigBuilder();
    if (dockerConfiguration.getHost() != null) {
      clientConfigBuilder.withDockerHost(dockerConfiguration.getHost());
    }

    DockerClientConfig clientConfig = clientConfigBuilder.build();
    DockerClient client =
        DockerClientBuilder.getInstance(clientConfig)
            .withDockerHttpClient(new Builder().dockerHost(clientConfig.getDockerHost()).build())
            .build();
    checkConnection(client, clientConfig);
    return client;
  }

  private static void checkConnection(DockerClient client, DockerClientConfig clientConfig) {
    try {
      client.infoCmd().exec();
    } catch (RuntimeException e) { // there is no specific exception...
      throw new ConnectException(
          "Unable to access the Docker socket at "
              + clientConfig.getDockerHost()
              + ". Is the Docker daemon running?");
    }
  }
}

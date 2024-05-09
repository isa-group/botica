package es.us.lsi.botica.utils.collector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.us.lsi.botica.rabbitmq.CollectorMessageProcessor;
import es.us.lsi.botica.rabbitmq.RabbitMQManager;
import es.us.lsi.botica.runners.CollectorLoader;
import es.us.lsi.botica.utils.directory.DirectoryOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

public class CollectorUtils {
    
    private static final Logger logger = LogManager.getLogger(CollectorUtils.class);
    private static final String BASE_CONTAINER_PATH = "/app/volume";

    private static String containerId;
    private static boolean isWindows;

    private CollectorUtils(){
    }

    public static DockerClient launchContainerToCollect(String imageName, String containerName, String defaultWindowsHost){

        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        DockerClient dockerClient;
        if (isWindows){
            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(defaultWindowsHost)
                    .withDockerTlsVerify(false)
                    .build();

            DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .build();

            dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient);
        }else{
            dockerClient = DockerClientBuilder.getInstance().build();
        }

        String volumeName = DirectoryOperations.getProjectName() + "_botica-volume";

        try{
            dockerClient.inspectVolumeCmd(volumeName).exec();
        } catch (Exception e){
            throw new RuntimeException("The volume " + volumeName + " does not exist. Please, create it before launch the container.");
        }

        if (!dockerClient.listContainersCmd().withShowAll(true).withNameFilter(List.of(containerName)).exec().isEmpty()){
            dockerClient.killContainerCmd(containerName).exec();
            dockerClient.removeContainerCmd(containerName).exec();
        }

        CreateContainerCmd container = dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .withBinds(new Bind(volumeName, new Volume(BASE_CONTAINER_PATH)))
                .withCmd("tail", "-f", "/dev/null");

        CreateContainerResponse containerResponse = container.exec();
        containerId = containerResponse.getId();

        dockerClient.startContainerCmd(containerId).exec();

        return dockerClient;
    }

    public static void executeCollectorAction(Integer initialDelay, Integer period, List<String> pathsToObserve,
                                                String localPathToCopy){

        for (String path : pathsToObserve) {
            Path directoryPath = Path.of(localPathToCopy + path);
            DirectoryOperations.createDir(directoryPath);
        }

        //RabbitMQ connection
        CollectorMessageProcessor messageProcessor = new CollectorMessageProcessor(pathsToObserve, BASE_CONTAINER_PATH, localPathToCopy);
        RabbitMQManager messageSender = new RabbitMQManager(messageProcessor, null, null, null, "localhost", 0);

        List<String> bindingKeys = new ArrayList<>();
        bindingKeys.add("requestToCollector");
        List<Boolean> queueOptions = Arrays.asList(true, false, false);
        try{
            String queueName = messageSender.connect("collector", bindingKeys, queueOptions);
            messageSender.receiveMessage(queueName);
        }catch(IOException | TimeoutException e){
            e.printStackTrace();
        }
        //

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> collectFromRabbit(pathsToObserve, BASE_CONTAINER_PATH, localPathToCopy);
        scheduler.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
    }

    public static void collectFromRabbit(List<String> pathsToObserve, String baseContainerPath, String localPathToCopy){
        logger.info("Collecting data ...");
        for (String path : pathsToObserve) {
            CollectorUtils.executeDockerCp(containerId, path, baseContainerPath, localPathToCopy + path);
        }
    }

    private static void executeDockerCp(String containerId, String sourcePath, String baseContainerPath, String destinationPath) {
        
        String command = "docker cp " + containerId + ":" + baseContainerPath + sourcePath + " " + destinationPath.substring(0, destinationPath.lastIndexOf("/"));

        Process process = null;
        
        try {
            if (isWindows) {
                process = Runtime.getRuntime().exec("cmd /c " + command);
            } else {
                process = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void stopAndRemoveContainer(DockerClient dockerClient, String containerName) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dockerClient.killContainerCmd(containerName).exec();
            dockerClient.removeContainerCmd(containerName).exec();
        }));

        try {
            logger.info("Press Ctrl+C to exit.");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void collectData(CollectorLoader collectorLoader){

        String imageName = collectorLoader.getImageName();
        String containerName = collectorLoader.getContainerName();
        List<String> pathsToObserve = collectorLoader.getPathsToObserve();
        String localPathToCopy = collectorLoader.getLocalPathToCopy();
        String defaultWindowsHost = collectorLoader.getWindowsDockerHost();

        DockerClient dockerClient = launchContainerToCollect(imageName, containerName, defaultWindowsHost);

        logger.info("Collecting data ...");
        for (String path : pathsToObserve) {
            Path directoryPath = Path.of(localPathToCopy + path);
            DirectoryOperations.createDir(directoryPath);
            executeDockerCp(containerId, path, BASE_CONTAINER_PATH, localPathToCopy + path);
        }
        dockerClient.killContainerCmd(containerName).exec();
        dockerClient.removeContainerCmd(containerName).exec();
    }
}

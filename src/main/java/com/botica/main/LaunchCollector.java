package com.botica.main;

import java.util.List;

import com.botica.runners.CollectorLoader;
import com.botica.utils.collector.CollectorUtils;

import com.github.dockerjava.api.DockerClient;

public class LaunchCollector {

    private static final String CONFIGURATION_PROPERTIES_FILE_PATH = "src/main/resources/BOTICAConfig/collector.properties";

    public static void main(String[] args) {

        CollectorLoader collectorLoader = new CollectorLoader(CONFIGURATION_PROPERTIES_FILE_PATH, true);

        List<String> pathsToObserve = collectorLoader.getPathsToObserve();
        
        String localPathToCopy = collectorLoader.getLocalPathToCopy();
        String containerName = collectorLoader.getContainerName();
        String imageName = collectorLoader.getImageName();
        String windowsDockerHost = collectorLoader.getWindowsDockerHost();
        
        Integer initialDelayToCollect = collectorLoader.getInitialDelayToCollect();
        Integer periodToCollect = collectorLoader.getPeriodToCollect();
        
        DockerClient dockerClient = CollectorUtils.launchContainerToCollect(imageName, containerName, windowsDockerHost);
        CollectorUtils.executeCollectorAction(initialDelayToCollect, periodToCollect, pathsToObserve, localPathToCopy);
        CollectorUtils.stopAndRemoveContainer(dockerClient, containerName);
    }
}

package com.template.main;

import java.util.ArrayList;
import java.util.List;

import com.botica.utils.collector.CollectorUtils;
import com.github.dockerjava.api.DockerClient;

public class LaunchCollector {

    private static final List<String> PATHS_TO_OBSERVE = new ArrayList<>(
        List.of(
                // Add here the paths to observe.
                ));

    private static final String LOCAL_PATH_TO_COPY = "tmp/collector";
    private static final String CONTAINER_NAME = "collector";
    private static final String IMAGE_NAME = "dummy";
    private static final String DEFAULT_WINDOWS_HOST = "tcp://127.0.0.1:2375";

    private static final Integer INITIAL_DELAY_TO_COLLECT = 10;
    private static final Integer PERIOD_TO_COLLECT = 60;

    public static void main(String[] args) {
        DockerClient dockerClient = CollectorUtils.launchContainerToCollect(IMAGE_NAME, CONTAINER_NAME, DEFAULT_WINDOWS_HOST);
        CollectorUtils.executeCollectorAction(INITIAL_DELAY_TO_COLLECT, PERIOD_TO_COLLECT, PATHS_TO_OBSERVE, LOCAL_PATH_TO_COPY);
        CollectorUtils.stopAndRemoveContainer(dockerClient, CONTAINER_NAME);
    }
}

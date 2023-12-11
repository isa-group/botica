package com.botica.main;

import java.util.ArrayList;
import java.util.List;

import com.botica.utils.collector.InitCollector;
import com.github.dockerjava.api.DockerClient;

public class LaunchCollector {

    private static final List<String> PATHS_TO_OBSERVE = new ArrayList<>(
        List.of(
                "/target/allure-results",
                "/target/coverage-data",
                "/target/test-data",
                "/src/main/resources/Examples/Ex4_CBTGeneration/allure_report",
                "/src/main/resources/Examples/Ex5_CBTGeneration/allure_report"
                ));

    private static final String LOCAL_PATH_TO_COPY = "tmp/collector";
    private static final String CONTAINER_NAME = "collector";
    private static final String IMAGE_NAME = "dummy";
    private static final String DEFAULT_WINDOWS_HOST = "tcp://127.0.0.1:2375";

    private static final Integer INITIAL_DELAY_TO_COLLECT = 10;
    private static final Integer PERIOD_TO_COLLECT = 5;

    public static void main(String[] args) {

        DockerClient dockerClient = InitCollector.launchContainerToCollect(IMAGE_NAME, CONTAINER_NAME, DEFAULT_WINDOWS_HOST);
        InitCollector.executeCollectorAction(INITIAL_DELAY_TO_COLLECT, PERIOD_TO_COLLECT, PATHS_TO_OBSERVE, LOCAL_PATH_TO_COPY);
        InitCollector.stopAndRemoveContainer(dockerClient, CONTAINER_NAME);

    }
}

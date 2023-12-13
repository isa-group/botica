package com.botica.rabbitmq;

import java.util.List;

import com.botica.utils.collector.CollectorUtils;

public class CollectorMessageProcessor implements MessageProcessor {

    private List<String> pathsToObserve;
    private String baseContainerPath;
    private String localPathToCopy;

    public CollectorMessageProcessor(List<String> pathsToObserve, String baseContainerPath, String localPathToCopy){
        this.pathsToObserve = pathsToObserve;
        this.baseContainerPath = baseContainerPath;
        this.localPathToCopy = localPathToCopy;
    }

    @Override
    public void processMessage(String message) {
        CollectorUtils.collectFromRabbit(pathsToObserve, baseContainerPath, localPathToCopy);
    }
}

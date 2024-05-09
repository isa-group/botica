package es.us.lsi.botica.rabbitmq;

import java.util.List;

import org.json.JSONObject;

import es.us.lsi.botica.utils.collector.CollectorUtils;

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
        JSONObject messageData = new JSONObject(message);
        if(messageData.has("order")){
            String order = messageData.getString("order");
            if(order.equals("collect")){
                CollectorUtils.collectFromRabbit(pathsToObserve, baseContainerPath, localPathToCopy);
            }
        }
    }
}

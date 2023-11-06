package com.botica.bots;

import com.botica.utils.RabbitCommunicator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public abstract class AbstractBot {

    protected String keyToPublish;
    protected String orderToPublish;
    protected RabbitCommunicator rabbitCommunicator;

    protected static final Logger logger = LogManager.getLogger(AbstractBot.class);

    protected AbstractBot(String keyToPublish, String orderToPublish) {
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
        this.rabbitCommunicator = new RabbitCommunicator(this.keyToPublish, logger);
    }

    protected abstract void botAction();
    protected abstract JSONObject createMessage();
    
    public void executeBotActionAndSendMessage() {
        botAction();
        rabbitCommunicator.sendMessage(createMessage().toString());
    }
}

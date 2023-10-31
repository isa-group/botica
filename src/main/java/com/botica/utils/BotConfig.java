package com.botica.utils;

public class BotConfig {
    private String botId;
    private String order;
    private String keyToPublish;
    private String orderToPublish;
    private String botType;

    public BotConfig(String botId, String order, String keyToPublish, String orderToPublish, String botType) {
        this.botId = botId;
        this.order = order;
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
        this.botType = botType;
    }

    // Getters
    public String getBotId() {
        return botId;
    }

    public String getOrder() {
        return order;
    }

    public String getKeyToPublish() {
        return keyToPublish;
    }

    public String getOrderToPublish() {
        return orderToPublish;
    }

    public String getBotType() {
        return botType;
    }
}


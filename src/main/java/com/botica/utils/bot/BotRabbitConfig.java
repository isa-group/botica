package com.botica.utils.bot;

public class BotRabbitConfig {

    private String order;
    private String keyToPublish;
    private String orderToPublish;
    private String botType;

    /**
     * Constructor for the BotRabbitConfig class.
     *
     * @param order          The order associated with the bot.
     * @param keyToPublish   The binding key for publishing messages.
     * @param orderToPublish The order to be sent in the message.
     * @param botType        The type of the bot.
     */
    public BotRabbitConfig(String botType, String order, String keyToPublish, String orderToPublish) {
        this.botType = botType;
        this.order = order;
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
    }

    // Getters

    public String getBotType() {
        return botType;
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
}


package com.botica.rabbitmq;

public interface MessageProcessor {
    void processMessage(String message);
}

package es.us.lsi.botica.rabbitmq;

public interface MessageProcessor {
    void processMessage(String message);
}

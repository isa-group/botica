package es.us.isa.botica.rabbitmq;

public interface MessageProcessor {
    void processMessage(String message);
}

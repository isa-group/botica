package es.us.isa.botica.client;

@FunctionalInterface
public interface OrderListener {
  void onMessageReceived(String order, String message);
}

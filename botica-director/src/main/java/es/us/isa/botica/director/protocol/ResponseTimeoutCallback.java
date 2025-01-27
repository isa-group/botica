package es.us.isa.botica.director.protocol;


@FunctionalInterface
public interface ResponseTimeoutCallback {
  void onResponseTimeout(String timeoutBotId);
}

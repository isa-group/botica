package es.us.isa.botica.protocol.query;

import es.us.isa.botica.util.StringUtils;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QueryHandler {
  @VisibleForTesting static final int REQUEST_ID_LENGTH = 8;

  private final ScheduledExecutorService executorService;

  private final Map<String, Consumer<ResponsePacket>> callbacks = new ConcurrentHashMap<>();
  private final Map<String, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();

  public QueryHandler(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  @SuppressWarnings("unchecked")
  public <ResponsePacketT extends ResponsePacket> void registerQuery(
      RequestPacket<ResponsePacketT> packet,
      Consumer<ResponsePacketT> callback,
      Runnable timeoutCallback,
      long timeout,
      TimeUnit timeoutUnit) {
    String requestId = StringUtils.random(REQUEST_ID_LENGTH);
    packet.setRequestId(requestId);

    ScheduledFuture<?> future =
        this.executorService.schedule(
            () -> {
              this.callbacks.remove(requestId);
              this.timeoutFutures.remove(requestId);
              timeoutCallback.run();
            },
            timeout,
            timeoutUnit);
    this.callbacks.put(requestId, (Consumer<ResponsePacket>) callback);
    this.timeoutFutures.put(requestId, future);
  }

  public void acceptResponse(ResponsePacket packet) {
    if (packet.getRequestId() == null) {
      throw new IllegalArgumentException(
          String.format("received %s packet with null request ID", packet.getClass().getName()));
    }

    Consumer<ResponsePacket> callback = this.callbacks.remove(packet.getRequestId());
    if (callback == null) return;

    ScheduledFuture<?> timeoutFuture = this.timeoutFutures.remove(packet.getRequestId());
    if (timeoutFuture != null) timeoutFuture.cancel(false);

    callback.accept(packet);
  }

  @VisibleForTesting
  Map<String, Consumer<ResponsePacket>> getCallbacks() {
    return callbacks;
  }

  @VisibleForTesting
  Map<String, ScheduledFuture<?>> getTimeoutFutures() {
    return timeoutFutures;
  }
}

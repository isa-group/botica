package es.us.isa.botica.rabbitmq;

import com.rabbitmq.client.impl.DefaultExceptionHandler;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SilentExceptionHandler extends DefaultExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(SilentExceptionHandler.class);

  @Override
  protected void log(String message, Throwable e) {
    if (e instanceof IOException && "Connection reset".equals(e.getMessage())) {
      log.debug("{} ({})", message, e.getMessage());
      return;
    }
    super.log(message, e);
  }
}

package es.us.isa.botica.director.cli;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.jline.reader.LineReader;

public class JLineAppender extends AppenderBase<ILoggingEvent> {
  private final PatternLayout layout = new PatternLayout();
  private final LineReader lineReader;

  public JLineAppender(LineReader lineReader) {
    this.lineReader = lineReader;
  }

  @Override
  public void start() {
    super.start();
    this.layout.setContext(this.getContext());
    this.layout.setPattern("%d{HH:mm:ss.SSS} %-5level %msg%n");
    this.layout.start();
  }

  @Override
  public void stop() {
    this.layout.stop();
    super.stop();
  }

  @Override
  protected void append(ILoggingEvent event) {
    this.lineReader.printAbove(this.layout.doLayout(event));
  }
}

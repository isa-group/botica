package es.us.isa.botica.configuration.bot;

public class BotMountConfiguration {
  private String source;
  private String target;

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "BotMountConfiguration{"
        + "source='"
        + source
        + '\''
        + ", target='"
        + target
        + '\''
        + '}';
  }
}

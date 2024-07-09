package es.us.isa.botica.configuration.bot.lifecycle;

public enum BotLifecycleType {
  REACTIVE("reactive"),
  PROACTIVE("proactive"),
  UNMANAGED("unmanaged");

  private final String name;

  BotLifecycleType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

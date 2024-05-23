package es.us.isa.botica.util.configuration;

public class DummyConfigurationFile implements ConfigurationFile {
  public String string;
  public InnerObject object;

  public static class InnerObject {
    public Integer integer;
  }
}

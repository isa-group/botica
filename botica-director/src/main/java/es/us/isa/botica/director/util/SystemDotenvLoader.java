package es.us.isa.botica.director.util;

import io.github.cdimascio.dotenv.Dotenv;

public class SystemDotenvLoader implements DotenvLoader {
  @Override
  public void loadIntoSystemProperties() {
    Dotenv.configure().ignoreIfMissing().systemProperties().load();
  }
}

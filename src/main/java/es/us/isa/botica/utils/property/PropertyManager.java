package es.us.isa.botica.utils.property;

/**
 * @author Sergio Segura
 */
public class PropertyManager { //

  // legacy, provisional code!
  public static String readProperty(String name) {
    return System.getenv(name);
  }
}

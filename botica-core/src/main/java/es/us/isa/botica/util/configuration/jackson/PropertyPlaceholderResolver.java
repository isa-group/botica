package es.us.isa.botica.util.configuration.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyPlaceholderResolver {
  private static final Pattern PLACEHOLDER_PATTERN =
      Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
  private static final Logger log = LoggerFactory.getLogger(PropertyPlaceholderResolver.class);

  public static <T> T resolve(ObjectMapper mapper, String content, Class<T> targetClass)
      throws JsonProcessingException {
    JsonNode rootNode = processNode(mapper.readTree(content));
    return mapper.treeToValue(rootNode, targetClass);
  }

  private static JsonNode processNode(JsonNode node) {
    if (node.isTextual()) {
      String text = node.asText();
      String resolved = resolvePlaceholders(text);

      if (!resolved.equals(text)) {
        return createTypedNode(resolved);
      }
      return new TextNode(resolved);
    } else if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      for (int i = 0; i < node.size(); i++) {
        arrayNode.set(i, processNode(node.get(i)));
      }
      return arrayNode;
    } else if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode
          .fields()
          .forEachRemaining(entry -> objectNode.set(entry.getKey(), processNode(entry.getValue())));
      return objectNode;
    }
    return node;
  }

  private static String resolvePlaceholders(String text) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();

    while (matcher.find()) {
      String propertyName = matcher.group(1);
      String defaultValue = matcher.group(2);
      String propertyValue = System.getProperty(propertyName);

      if (propertyValue == null && defaultValue == null) {
        log.warn(
            "Configuration reference to environment variable '{}' is missing and has no default "
                + "value. Ensure it is set in the system environment or provide a default in the "
                + "configuration file.",
            propertyName);
      }
      String value = propertyValue != null ? propertyValue : defaultValue;
      matcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private static JsonNode createTypedNode(String value) {
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
      return JsonNodeFactory.instance.booleanNode(Boolean.parseBoolean(value));
    }

    try {
      if (value.contains(".")) {
        return JsonNodeFactory.instance.numberNode(Double.parseDouble(value));
      } else {
        return JsonNodeFactory.instance.numberNode(Long.parseLong(value));
      }
    } catch (NumberFormatException e) {
      return JsonNodeFactory.instance.textNode(value);
    }
  }
}

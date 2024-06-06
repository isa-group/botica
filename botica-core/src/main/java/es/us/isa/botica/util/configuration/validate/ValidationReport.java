package es.us.isa.botica.util.configuration.validate;

import es.us.isa.botica.util.configuration.validate.ValidationResult.Type;
import java.util.*;

public class ValidationReport {
  private final Map<String, List<ValidationResult>> results = new LinkedHashMap<>();
  private final Map<String, ValidationReport> children = new LinkedHashMap<>();

  public ValidationReport() {}

  public void addWarning(String property, String message, Object... args) {
    this.addResult(Type.WARNING, property, message, args);
  }

  public boolean hasWarnings() {
    return this.hasResultsMatchingType(Type.WARNING);
  }

  public long countWarnings() {
    return this.countResultsMatchingType(Type.WARNING);
  }

  public void addError(String property, String message, Object... args) {
    this.addResult(Type.ERROR, property, message, args);
  }

  public boolean hasErrors() {
    return this.hasResultsMatchingType(Type.ERROR);
  }

  public long countErrors() {
    return this.countResultsMatchingType(Type.ERROR);
  }

  public long countResultsMatchingType(Type... types) {
    return results.values().stream()
            .flatMap(Collection::stream)
            .filter(result -> Arrays.asList(types).contains(result.getType()))
            .count()
        + children.values().stream()
            .mapToLong(report -> report.countResultsMatchingType(types))
            .sum();
  }

  public boolean hasResultsMatchingType(Type... types) {
    return results.values().stream()
            .flatMap(Collection::stream)
            .anyMatch(result -> Arrays.asList(types).contains(result.getType()))
        || children.values().stream().anyMatch(report -> report.hasResultsMatchingType(types));
  }

  public void addResult(Type type, String property, String message, Object... args) {
    this.addResult(new ValidationResult(type, property, message, args));
  }

  public void addResult(ValidationResult validationResult) {
    results
        .computeIfAbsent(validationResult.getProperty(), p -> new ArrayList<>())
        .add(validationResult);
  }

  public void registerChild(String property, Validatable validatable) {
    if (validatable == null) {
      addError(property, "missing property");
      return;
    }

    ValidationReport child = this.createChild();
    validatable.validate(child);
    this.addChild(property, child);
  }

  public void registerChild(String property, List<? extends Validatable> validatables) {
    for (int i = 0; i < validatables.size(); i++) {
      this.registerChild(property + "[" + i + "]", validatables.get(i));
    }
  }

  public ValidationReport createChild() {
    return new ValidationReport();
  }

  public void addChild(String property, ValidationReport report) {
    children.put(property, report);
  }

  @Override
  public String toString() {
    return this.render();
  }

  public String render() {
    return this.render(Type.values());
  }

  public String render(Type... types) {
    return this.render(0, types);
  }

  private String render(int level, Type... types) {
    String indent = " ".repeat(level * 2);
    StringBuilder builder = new StringBuilder();

    this.results.forEach(
        (property, results) ->
            results.stream()
                .filter(result -> Arrays.asList(types).contains(result.getType()))
                .forEach(result -> builder.append(renderResult(property, result, indent))));

    this.children.entrySet().stream()
        .filter(entry -> entry.getValue().hasResultsMatchingType(types))
        .forEach(
            entry ->
                builder.append(
                    renderReport(entry.getKey(), entry.getValue(), indent, level, types)));

    if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n') {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  private String renderResult(String property, ValidationResult result, String indent) {
    return String.format("%s- %s: %s\n", indent, property, result.getMessage());
  }

  private String renderReport(
      String property, ValidationReport report, String indent, int level, Type... types) {
    return String.format("%s- %s:\n%s\n", indent, property, report.render(level + 1, types));
  }

  public List<ValidationResult> getResults(String property) {
    return results.get(property);
  }

  public Map<String, List<ValidationResult>> getResultsByProperty() {
    return Collections.unmodifiableMap(results);
  }

  public ValidationReport getChild(String property) {
    return children.get(property);
  }

  public Map<String, ValidationReport> getChildren() {
    return Collections.unmodifiableMap(children);
  }
}

package io.github.zoroaster1x.vlcskin.format;

/**
 * One problem found while parsing or validating a skin.
 */
public record ParseIssue(Severity severity, String message, String location) {

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    public static ParseIssue error(String message, String location) {
        return new ParseIssue(Severity.ERROR, message, location);
    }

    public static ParseIssue warning(String message, String location) {
        return new ParseIssue(Severity.WARNING, message, location);
    }

    public static ParseIssue info(String message, String location) {
        return new ParseIssue(Severity.INFO, message, location);
    }

    @Override
    public String toString() {
        String where = location == null || location.isBlank() ? "" : " [" + location + "]";
        return severity + ": " + message + where;
    }
}

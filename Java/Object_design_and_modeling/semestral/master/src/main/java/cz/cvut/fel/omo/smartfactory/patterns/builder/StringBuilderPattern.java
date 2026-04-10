package cz.cvut.fel.omo.smartfactory.patterns.builder;

/**
 * Builder for constructing detailed string representations.
 */
public class StringBuilderPattern {
    private final StringBuilder stringBuilder;

    public StringBuilderPattern() {
        this.stringBuilder = new StringBuilder();
    }

    public StringBuilderPattern append(String key, Object value) {
        stringBuilder.append(key).append(": ").append(value).append("\n");
        return this;
    }

    public StringBuilderPattern addSeparator() {
        stringBuilder.append("------------\n");
        return this;
    }

    public StringBuilderPattern appendHeader(String header) {
        stringBuilder.append(header).append("\n").append("============\n");
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }
}

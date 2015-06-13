package me.williamhester.knapsack.internal;

/**
 * Created by william on 6/12/15.
 */
class SerializableFieldBundling implements FieldBundling {

    private final String type;
    private final String name;

    SerializableFieldBundling(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getBundleMethodPhrase() {
        return "Serializable";
    }

    @Override
    public boolean requiresCast() {
        return true;
    }
}

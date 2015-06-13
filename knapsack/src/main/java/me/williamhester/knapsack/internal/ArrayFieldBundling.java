package me.williamhester.knapsack.internal;

/**
 * Created by william on 6/12/15.
 */
class ArrayFieldBundling implements FieldBundling {

    private final String type;
    private final String name;

    ArrayFieldBundling(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getBundleMethodPhrase() {
        return type + "Array";
    }

    @Override
    public boolean requiresCast() {
        return false;
    }
}

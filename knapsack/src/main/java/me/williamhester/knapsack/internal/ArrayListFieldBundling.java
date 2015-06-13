package me.williamhester.knapsack.internal;

/**
 * Created by william on 6/12/15.
 */
class ArrayListFieldBundling implements FieldBundling {

    private final String name;
    private final String type;

    ArrayListFieldBundling(String type, String name) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "ArrayList<" + type + ">";
    }

    @Override
    public String getBundleMethodPhrase() {
        return type + "ArrayList";
    }

    @Override
    public boolean requiresCast() {
        return false;
    }
}

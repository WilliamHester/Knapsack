package me.williamhester.knapsack.internal;

/**
 * Created by william on 6/12/15.
 */
class SimpleFieldBundling implements FieldBundling {

    private final String bundleMethodPart;
    private final String name;

    SimpleFieldBundling(String bundleMethodPart, String name) {
        this.bundleMethodPart = bundleMethodPart;
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
        return bundleMethodPart;
    }

    @Override
    public boolean requiresCast() {
        return false;
    }
}

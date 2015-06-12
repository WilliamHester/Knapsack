package me.williamhester.knapsack;

import java.util.LinkedHashMap;
import java.util.Map;

import me.williamhester.knapsack.internal.Bundler;

/**
 * @author William Hester
 */
@SuppressWarnings("unused")
public class Knapsack {

    private static final Map<Class<?>, Bundler> BUNDLERS = new LinkedHashMap<>();

    public static void save(Object target, Bundle state) {
        Bundler bundler = getBundlerForClass(target.getClass());
        bundler.save(state);
    }

    public static boolean restore(Object target, Bundle state) {
        if (state == null) {
            return false;
        }
        Bundler bundler = getBundlerForClass(target.getClass());
        bundler.restore(state);
        return true;
    }

    private static Bundler getBundlerForClass(Class<?> clazz) {
        Bundler bundler;
        if (!BUNDLERS.containsKey(clazz)) {
            bundler = createBundler();
        } else {
            bundler = BUNDLERS.get(clazz);
        }
        return bundler;
    }

    private static Bundler createBundler() {
        return null;
    }

}

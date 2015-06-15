package me.williamhester.knapsack;

import android.os.Bundle;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import me.williamhester.knapsack.internal.KnapsackProcessor;

/**
 * @author William Hester
 */
@SuppressWarnings("unused")
public class Knapsack {

    private static final String TAG = "Knapsack";

    private static final String JAVA_PREFIX = "java.";
    private static final String ANDROID_PREFIX = "android.";

    private static final boolean debug = true;

    private static final Map<Class<?>, Bundler> BUNDLERS = new LinkedHashMap<>();

    public static void save(Object target, Bundle state) {
        try {
            Bundler bundler = getBundlerForClass(target.getClass());
            bundler.save(state, target);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static boolean restore(Object target, Bundle state) {
        if (state == null) {
            return false;
        }
        try {
            Bundler bundler = getBundlerForClass(target.getClass());
            bundler.restore(state, target);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return true;
    }


    private static Bundler getBundlerForClass(Class<?> cls)
            throws IllegalAccessException, InstantiationException {
        Bundler viewBinder = BUNDLERS.get(cls);
        if (viewBinder != null) {
            if (debug) Log.d(TAG, "HIT: Cached in view binder map.");
            return viewBinder;
        }
        String clsName = cls.getName();
        if (clsName.startsWith(ANDROID_PREFIX) || clsName.startsWith(JAVA_PREFIX)) {
            if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
            return NOP_BUNDLER;
        }
        try {
            Class<?> viewBindingClass = Class.forName(clsName + KnapsackProcessor.SUFFIX);
            //noinspection unchecked
            viewBinder = (Bundler) viewBindingClass.newInstance();
            if (debug) Log.d(TAG, "HIT: Loaded view binder class.");
        } catch (ClassNotFoundException e) {
            if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
            viewBinder = getBundlerForClass(cls.getSuperclass());
        }
        BUNDLERS.put(cls, viewBinder);
        return viewBinder;
    }

    private static Bundler createBundler() {
        return null;
    }

    private static final Bundler NOP_BUNDLER = new Bundler() {
        @Override
        public void save(Bundle state, Object target) { }

        @Override
        public void restore(Bundle state, Object target) { }
    };

}

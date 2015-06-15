package me.williamhester.knapsack;

import android.os.Bundle;

/**
 * Created by william on 6/11/15.
 */
public interface Bundler {

    void save(Bundle state, Object target);

    void restore(Bundle state, Object target);

}

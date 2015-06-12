package me.williamhester.knapsack.internal;

import android.os.Bundle;

/**
 * Created by william on 6/11/15.
 */
public interface Bundler {

    void save(Bundle state);

    void restore(Bundle state);

}

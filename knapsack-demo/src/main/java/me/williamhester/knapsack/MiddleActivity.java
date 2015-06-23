package me.williamhester.knapsack;

import android.os.Bundle;

/**
 * Created by william on 6/19/15.
 */
public class MiddleActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Knapsack.restore(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }
}

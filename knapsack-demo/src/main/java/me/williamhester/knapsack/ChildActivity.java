package me.williamhester.knapsack;

import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by william on 6/18/15.
 */
public class ChildActivity extends MiddleActivity {

    @Save int test;
    @Save ArrayList<Things> things = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            test = 2;
        }
    }

    @Override
    protected String getText() {
        return super.getText() + " " + test;
    }

    static class Things implements Serializable {

    }
}

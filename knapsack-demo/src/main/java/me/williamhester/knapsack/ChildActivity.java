package me.williamhester.knapsack;

import android.os.Bundle;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by william on 6/18/15.
 */
public class ChildActivity extends MainActivity {

    @Save int test;
    @Save ArrayList<Things> things = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            test = 2;
        }

        TextView t = (TextView) findViewById(R.id.value);
        t.setText("" + 2);
    }

    static class Things implements Serializable {

    }
}

package me.williamhester.knapsack;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Save int mValue = -1;
    @Save ArrayList<String> strings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Knapsack.restore(this, savedInstanceState)) {
            mValue = 10;
            for (int i = 0; i < 10; i++) {
                strings.add(((char) i + 'a') + "");
            }
        }
        TextView tv = (TextView) findViewById(R.id.value);
        tv.setText("" + mValue + " " + strings.toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }
}

package me.williamhester.knapsack;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Save int mValue = -1;
    @Save ArrayList<String> strings = new ArrayList<>();
    @Save ArrayList<Thing> things = new ArrayList<>();
    @Save boolean value;
    @Save SparseArray<Thing> sparseArray = new SparseArray<>();
    @Save String string = "stuff";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Knapsack.restore(this, savedInstanceState)) {
            mValue = 10;
            for (int i = 0; i < 10; i++) {
                strings.add((char) (i + 'a') + "");
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

    static class Thing implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public Thing() {
        }

        protected Thing(Parcel in) {
        }

        public static final Parcelable.Creator<Thing> CREATOR = new Parcelable.Creator<Thing>() {
            public Thing createFromParcel(Parcel source) {
                return new Thing(source);
            }

            public Thing[] newArray(int size) {
                return new Thing[size];
            }
        };
    }
}

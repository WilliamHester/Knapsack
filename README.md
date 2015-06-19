# Knapsack
An instance saver for Android Fragments and Activities.

You'll never have to write a million public static final Strings
again! Knapsack is an annotation processor that lets you save your
activities and fragments by simply annotating variables that could
normally be saved into a bundle during onSaveInstanceState() and
restored in onCreate().

Knapsack uses annotation processing rather than reflection for
performance. This means that variables and inner classes that are
to be saved using Knapsack cannot be private and instead must have
at a minimum no access modifier.

### Sample usage
```java
public class MyActivity extends Activity {
    @Save int mMyInt;
    @Save String mMyString;
    @Save ArrayList<MyParcelable> mMyParcelables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Knapsack.restore(this, savedInstanceState)) {
            // set up your variables
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }
}
```

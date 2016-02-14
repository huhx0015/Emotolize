package osmo.devweekhack2016.com.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import osmo.devweekhack2016.com.R;
import osmo.devweekhack2016.com.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {

            MainFragment mainFragment = new MainFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, mainFragment)
                    .commit();
        }
    }
}

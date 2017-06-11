package com.wheelselector.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.naz013.wheelselector.WheelSelector;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WheelSelector wheelSelector = (WheelSelector) findViewById(R.id.wheel);
        wheelSelector.setListener(new WheelSelector.OnWheelScrollListener() {
            @Override
            public void onValueSelected(int position, String value) {
                Log.d(TAG, "onValueSelected: " + position + ", value: " + value);
            }

            @Override
            public void onWheelScrolled(int position, String value) {
                Log.d(TAG, "onWheelScrolled: " + position + ", value: " + value);
            }
        });
    }
}

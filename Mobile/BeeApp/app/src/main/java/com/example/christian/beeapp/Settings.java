package com.example.christian.beeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    private String phoneNumber;
    private static final String PREFIX = "+4";

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        sharedPreferences = getSharedPreferences(MainActivity.SCALE_PREFERENCES, Context.MODE_PRIVATE);

        Button save = (Button) findViewById(R.id.buttonSave);
        final EditText phoneEditText = (EditText) findViewById(R.id.phoneNumber);

        String savedNumber = sharedPreferences.getString(MainActivity.SCALE_PHONE_NUMBER_KEY, "");
        if (savedNumber.length() != 0) {
            phoneEditText.setText(savedNumber);
            Toast.makeText(getApplicationContext(),
                    "Field already completed", Toast.LENGTH_LONG).show();
        }

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneEditText.getText().toString();
                if (phoneNumber.length() < 10) {
                    Snackbar.make(v, "Invalid phone number", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (phoneNumber.length() == 10) {
                    MainActivity.beeScalePhoneNumber = PREFIX + phoneNumber;


                    editor.putString(MainActivity.SCALE_PHONE_NUMBER_KEY, MainActivity.beeScalePhoneNumber);
                    editor.commit();
                    Snackbar.make(v, "Saved number", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    phoneEditText.setText(PREFIX + phoneNumber);
                } else {
                    MainActivity.beeScalePhoneNumber = phoneNumber;
                    editor.putString(MainActivity.SCALE_PHONE_NUMBER_KEY, MainActivity.beeScalePhoneNumber);
                    editor.commit();
                    Snackbar.make(v, "Saved number", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}

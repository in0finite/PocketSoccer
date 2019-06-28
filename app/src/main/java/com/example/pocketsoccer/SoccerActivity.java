package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class SoccerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer);


        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sample_soccer_field_view, null);



    }
}

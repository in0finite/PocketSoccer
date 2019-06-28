package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class SoccerActivity extends AppCompatActivity {

    public static int ballImageId;
    public static int fieldImageId;
    public static Vec2 ballPos = new Vec2(50, 50);
    public static Vec2 ballSize = new Vec2(40, 40);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer);


        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sample_soccer_field_view, null);



    }
}

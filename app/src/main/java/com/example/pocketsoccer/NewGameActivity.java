package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class NewGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        View viewById = this.findViewById(R.id.scrollView1);
        View viewById2 = this.findViewById(R.id.scrollView2);

        // populate scroll views with flags
        
    }
}

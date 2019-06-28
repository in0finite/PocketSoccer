package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

public class NewGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        ScrollView viewById1 = this.findViewById(R.id.scrollView1);
        ScrollView viewById2 = this.findViewById(R.id.scrollView2);

        // populate scroll views with flags

        int[] imageIds = new int[]{R.drawable.en, R.drawable.fr, R.drawable.arg, R.drawable.br, R.drawable.es, R.drawable.ger, R.drawable.rs};

        for (int imageId : imageIds) {
            ((ViewGroup) viewById1.getChildAt(0)).addView(createFlag(imageId));
            ((ViewGroup) viewById2.getChildAt(0)).addView(createFlag(imageId));
        }

    }

    ImageView createFlag(int imageId) {
        ImageView imageView = new ImageView(getApplicationContext());
        //imageView.setLayoutParams(new ViewGroup.LayoutParams());
        imageView.setMaxHeight(150);
        imageView.setImageResource(imageId);
        return imageView;
    }

}

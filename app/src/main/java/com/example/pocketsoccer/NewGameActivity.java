package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

public class NewGameActivity extends AppCompatActivity {

    private int mPlayer1ImageId = 0, mPlayer2ImageId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        ScrollView viewById1 = this.findViewById(R.id.scrollView1);
        ScrollView viewById2 = this.findViewById(R.id.scrollView2);

        // populate scroll views with flags

        int[] imageIds = new int[]{R.drawable.en, R.drawable.fr, R.drawable.arg, R.drawable.br, R.drawable.es, R.drawable.ger, R.drawable.rs};

        for (final int imageId : imageIds) {

            ImageView imageView = createFlag(imageId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayer1ImageId = imageId;
                }
            });
            ((ViewGroup) viewById1.getChildAt(0)).addView(imageView);

            imageView = createFlag(imageId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayer2ImageId = imageId;
                }
            });
            ((ViewGroup) viewById2.getChildAt(0)).addView(imageView);
        }

        // set button action
        findViewById(R.id.buttonStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

    }

    ImageView createFlag(int imageId) {
        ImageView imageView = new ImageView(getApplicationContext());
        //imageView.setLayoutParams(new ViewGroup.LayoutParams());
        imageView.setMaxHeight(50);
        imageView.setImageResource(imageId);
        return imageView;
    }

    void startGame() {
        Intent intent = new Intent(this, SoccerActivity.class);
        intent.putExtra("flagId1", mPlayer1ImageId);
        intent.putExtra("flagId2", mPlayer2ImageId);
        startActivity(intent);
    }

}

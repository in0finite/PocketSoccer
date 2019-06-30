package com.example.pocketsoccer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

public class NewGameActivity extends AppCompatActivity {

    private int mPlayer1ImageId = 0, mPlayer2ImageId = 0;

    private ViewGroup mFlagsContainer1, mFlagsContainer2;
    private EditText mEditTextPlayerName1, mEditTextPlayerName2;
    private CheckBox mCheckBoxIsAI1, mCheckBoxIsAI2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        mFlagsContainer1 = this.findViewById(R.id.flagsContainer1);
        mFlagsContainer2 = this.findViewById(R.id.flagsContainer2);

        mEditTextPlayerName1 = this.findViewById(R.id.editTextPlayer1Name);
        mEditTextPlayerName2 = this.findViewById(R.id.editTextPlayer2Name);

        mCheckBoxIsAI1 = this.findViewById(R.id.checkBoxIsAI1);
        mCheckBoxIsAI2 = this.findViewById(R.id.checkBoxIsAI2);

        // set default flags
        mPlayer1ImageId = R.drawable.br;
        mPlayer2ImageId = R.drawable.ger;

        // populate scroll views with flags

        mFlagsContainer1.removeAllViews();
        mFlagsContainer2.removeAllViews();

        int[] imageIds = new int[]{R.drawable.en, R.drawable.fr, R.drawable.arg, R.drawable.br, R.drawable.es, R.drawable.ger, R.drawable.rs};

        for (final int imageId : imageIds) {

            ImageView imageView = createFlag(imageId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayer1ImageId = imageId;
                }
            });
            mFlagsContainer1.addView(imageView);

            imageView = createFlag(imageId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayer2ImageId = imageId;
                }
            });
            mFlagsContainer2.addView(imageView);

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
        intent.putExtra("name1", mEditTextPlayerName1.getText().toString());
        intent.putExtra("name2", mEditTextPlayerName2.getText().toString());
        intent.putExtra("isAI1", mCheckBoxIsAI1.isChecked());
        intent.putExtra("isAI2", mCheckBoxIsAI2.isChecked());
        startActivity(intent);
    }

}

package com.example.numberguess;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class Score extends AppCompatActivity {

    private TextView tvFirstPlace;
    private TextView tvSecondPlace;
    private TextView tvThirdPlace;
    private Button btnBack;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        tvFirstPlace = findViewById(R.id.tv_first_place);
        tvSecondPlace = findViewById(R.id.tv_second_place);
        tvThirdPlace = findViewById(R.id.tv_third_place);
        btnBack = findViewById(R.id.btn_back);

        db = new DBHelper(this);

        loadTopScores();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTopScores() {
        ArrayList<ModelUser> topScores = db.getTop3Scores();

        // 1st Place
        if (topScores.size() > 0) {
            ModelUser first = topScores.get(0);
            tvFirstPlace.setText("🥇 1st: " + first.getName() + " - " + first.getScore() + " pts");
        } else {
            tvFirstPlace.setText("🥇 1st: No records");
        }

        // 2nd Place
        if (topScores.size() > 1) {
            ModelUser second = topScores.get(1);
            tvSecondPlace.setText("🥈 2nd: " + second.getName() + " - " + second.getScore() + " pts");
        } else {
            tvSecondPlace.setText("🥈 2nd: No records");
        }

        // 3rd Place
        if (topScores.size() > 2) {
            ModelUser third = topScores.get(2);
            tvThirdPlace.setText("🥉 3rd: " + third.getName() + " - " + third.getScore() + " pts");
        } else {
            tvThirdPlace.setText("🥉 3rd: No records");
        }
    }
}
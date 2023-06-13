package com.example.logicgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class EndQuizActivity extends AppCompatActivity {

    private int myPosition;
    private Double totalPoints;
    private String username;

    private TextView positionLabel;
    private TextView pointsLabel;
    private TextView usernameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_end_quiz);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRefs();
        setUIData();
    }

    private void getRefs(){
        Intent intent  = getIntent();
        myPosition = intent.getIntExtra("myPosition", 0);
        totalPoints = intent.getDoubleExtra("totalPoints", 0.0);
        username = intent.getStringExtra("username");
        positionLabel = findViewById(R.id.positionEndQuizLabel);
        pointsLabel = findViewById(R.id.pointsEndQuizLabel);
        usernameLabel = findViewById(R.id.usernameEndQuizLabel);

    }
    private void setUIData(){
        positionLabel.setText("Ti sei classificato " + myPosition +"° ");
        DecimalFormat df = new DecimalFormat("####0.0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        pointsLabel.setText("Hai "+ df.format(totalPoints) + " punti! ");
        usernameLabel.setText(username);
    }
}

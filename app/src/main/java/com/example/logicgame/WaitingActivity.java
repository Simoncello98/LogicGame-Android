package com.example.logicgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingActivity extends AppCompatActivity {
    private String gameCode;
    private TextView usernameLabel;
    private TextView connectedUserLabel;
    private String username;
    private String myRandomUID;
    private TextView gpsLocationLabel;

    //

    private ValueEventListener isStarted;
    private ValueEventListener numPlayers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_waiting);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRefs();
        setUsernameLabel();
       // setActivityIndicatorProgress();
        setupStartedObserver();
        setupConnectedUserObserver();
        getGPSLocation();

    }

    private void getRefs(){
        gameCode = getIntent().getStringExtra("gameCode");
        username = getIntent().getStringExtra("username");
        myRandomUID = getIntent().getStringExtra("myRandomUID");
        usernameLabel = findViewById(R.id.usernameEndQuizLabel);
        connectedUserLabel = findViewById(R.id.connectedUserLabel);
        gpsLocationLabel = findViewById(R.id.gpsLocationLabel);
        //activityIndicator = findViewById(R.id.activityIndicatorProgress);
    }

    private void setUsernameLabel(){
        usernameLabel.setText(username);
    }

    /*private void setActivityIndicatorProgress(){
        activityIndicator.startAnimation();
    }*/

    private void setupStartedObserver(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference lobby = database.getReference("lobby").child(gameCode);
        isStarted = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if(snap.exists()){
                    Boolean value = (Boolean) snap.getValue();
                    if(value == true){
                        if (isStarted != null) lobby.child("started").removeEventListener(isStarted);
                        if (numPlayers != null) lobby.child("players").removeEventListener(numPlayers);
                        //TODO: start the quiz
                        presentQuestionActivity();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error reading", "Error database");
            }
        };


        lobby.child("started").addValueEventListener(isStarted);
    }

    private void setupConnectedUserObserver(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference lobby = database.getReference("lobby").child(gameCode);
        numPlayers = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if(snap.exists()){
                    int num = (int) snap.getChildrenCount();
                    connectedUserLabel.setText("Utenti connessi: "+ num);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error reading", "Error database");
            }
        };

        lobby.child("players").addValueEventListener(numPlayers);

    }

    private void getGPSLocation(){
        GPSManager gps = new GPSManager(this);
        gpsLocationLabel.setText(gps.getAddress());
    }


    private void presentQuestionActivity(){
        Intent intent = new Intent(getBaseContext(), QuestionActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("gameCode", gameCode);
        intent.putExtra("myRandomUID", myRandomUID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


}

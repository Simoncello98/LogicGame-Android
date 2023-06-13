package com.example.logicgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class QuestionResultActivity extends AppCompatActivity {

    private String username;
    private String gameCode;
    private String myRandomUID;
    private Boolean timedOut;

    private Boolean correctOutput;
    private Double totalPoints;
    private Double bonusPoints;
    private int currentQuestion;



    private ValueEventListener timeoutListener;
    private ValueEventListener endQuestionListener;
    private DatabaseReference lobby;


    private String prevUserUID = "";
    private String prevUserPoints;
    private String prevUsername = "";
    private int myPosition;


    //UI - ELEMENTS
    private TextView usernameLabel;
    private TextView correctOutputLabel;
    private TextView bonusPointsLabel;
    private TextView positionLabel;
    private TextView pointsLabel;
    private TextView prevUserLabel;
    private TextView waitingAnswerLabel;

    private ProgressBar activityIndicator;

    private ImageView correctOutputImage;

    private LinearLayout resultsCard;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_question_result);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRefs();
        setupUI();


    }

    private void getRefs(){
        Intent intent  = getIntent();
        username = intent.getStringExtra("username");
        gameCode = intent.getStringExtra("gameCode");
        myRandomUID = intent.getStringExtra("myRandomUID");
        timedOut = intent.getBooleanExtra("timedOut",false);
        bonusPoints = intent.getDoubleExtra("bonusPoints", 0.0);
        correctOutput = intent.getBooleanExtra("correctOutput",false);

        totalPoints = intent.getDoubleExtra("totalPoints",1);
        currentQuestion = intent.getIntExtra("currentQuestion", 0);
        lobby = FirebaseDatabase.getInstance().getReference("lobby").child(gameCode);

        //UI-elements
        usernameLabel = findViewById(R.id.usernameEndQuizLabel);
        correctOutputLabel = findViewById(R.id.correctOutputLabel);
        bonusPointsLabel = findViewById(R.id.bonusPointsLabel);
        positionLabel = findViewById(R.id.positionEndQuizLabel);
        pointsLabel = findViewById(R.id.pointsEndQuizLabel);
        prevUserLabel = findViewById(R.id.seeYouNextTimeLabel);
        activityIndicator = findViewById(R.id.activityQuestionResultsProgress);
        resultsCard = findViewById(R.id.resultsCard);
        correctOutputImage = findViewById(R.id.correctOutputImage);
        waitingAnswerLabel = findViewById(R.id.greetingsLabel);

        //TODO: imageView ref

    }

    private void setupUI(){
        usernameLabel.setText(username);
        setUIForWaiting();
        if (timedOut){
            //get data and show results
            startClassificationListener();
            setupEndQuestionListener();
        }
        else{
            //show waiting and start listener on timeOut node
            setupTimeoutListener();
        }
    }



    private void showQuestionResults(){
        if(timedOut){
            correctOutputLabel.setText("Tempo scaduto.. ");
        }
        if (correctOutput){
            //immagine ok
            correctOutputImage.setImageResource(R.drawable.ok);
            correctOutputLabel.setText("Risposta corretta!");
            bonusPointsLabel.setText(bonusPoints + " punti bonus per tempo risposta!");
            Log.d("a","immagine e testo ok");
        }
        else{
            //immagine x
            correctOutputImage.setImageResource(R.drawable.error);
            if(!timedOut) correctOutputLabel.setText("Risposta errata.. ");
            bonusPointsLabel.setText("");
        }
        DecimalFormat df = new DecimalFormat("####0.0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        positionLabel.setText("Sei in posizione " + myPosition);
        pointsLabel.setText("Hai "+ df.format(totalPoints) + " punti! ");
        waitingAnswerLabel.setText("Attendo il docente..");

        if (!prevUsername.equalsIgnoreCase("")) prevUserLabel.setText("Sei sotto " + prevUsername  + ", punti: "+ prevUserPoints);
        else prevUserLabel.setText("");
        setUIForResults();
    }

    private void setUIForResults(){ //shows elements
        //usernameLabel.setVisibility(View.VISIBLE);
        correctOutputLabel.setVisibility(View.VISIBLE);
        bonusPointsLabel.setVisibility(View.VISIBLE);
        /*positionLabel = findViewById(R.id.positionLabel);
        pointsLabel = findViewById(R.id.pointsLabel);
        prevUserLabel = findViewById(R.id.prevUserLabel);*/
        activityIndicator.setVisibility(View.INVISIBLE);
        resultsCard.setVisibility(View.VISIBLE);
        correctOutputImage.setVisibility(View.VISIBLE);

    }

    private void setUIForWaiting(){ //hide elements
       // usernameLabel.setVisibility(View.INVISIBLE);
        correctOutputLabel.setVisibility(View.INVISIBLE);
        bonusPointsLabel.setVisibility(View.INVISIBLE);
        /*positionLabel = findViewById(R.id.positionLabel);
        pointsLabel = findViewById(R.id.pointsLabel);
        prevUserLabel = findViewById(R.id.prevUserLabel);*/
        activityIndicator.setVisibility(View.VISIBLE);
        resultsCard.setVisibility(View.INVISIBLE);
        correctOutputImage.setVisibility(View.INVISIBLE);

    }

    private void setupTimeoutListener(){

        timeoutListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Boolean timeout = (Boolean) dataSnapshot.getValue();
                    if (timeout){
                        if(timeoutListener != null){
                            DatabaseReference timeOutRef = lobby.child("Question "+currentQuestion).child("timeOut");
                            timeOutRef.removeEventListener(timeoutListener);
                        }
                        startClassificationListener();
                        setupEndQuestionListener();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference timeOutRef = lobby.child("Question "+currentQuestion).child("timeOut");
        timeOutRef.addValueEventListener(timeoutListener);
    }

    private void startClassificationListener(){ //SinleEventValue

        //TODO: set a listener for previous user in classification.

        ValueEventListener classificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    int myPos = (int) dataSnapshot.getChildrenCount();
                    myPosition = myPos;
                    if (myPos == 1) {
                        Log.d("PRIMO", "no prec");
                        //sono primo

                        showQuestionResults();
                    }
                    else{
                        Log.d("scarico", "dati precedente");
                        startPrevUserInClassificationListener();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //TODO: set correct ref for classification.
        DecimalFormat df = new DecimalFormat("####0.0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);

        Query classificationRef = lobby.child("classification").orderByValue().startAt(df.format(totalPoints),myRandomUID);
        classificationRef.addListenerForSingleValueEvent(classificationListener);
    }

    //TODO: is not working when an user is previous
    private void startPrevUserInClassificationListener(){ //singleValue listener
        ValueEventListener classificationPrevUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                   for(DataSnapshot user : dataSnapshot.getChildren()){
                       if(!user.getKey().equals(myRandomUID)){
                           //è il precedente
                           //prendo il nome utente
                           prevUserUID = user.getKey();
                           prevUserPoints = (String) user.getValue();
                           setupListenerForPrevUsername();
                       }
                   }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DecimalFormat df = new DecimalFormat("####0.0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        Query classificationRef = lobby.child("classification").orderByValue().startAt(df.format(totalPoints)).limitToFirst(2);
        classificationRef.addListenerForSingleValueEvent(classificationPrevUserListener);

    }

    private void setupListenerForPrevUsername(){
        ValueEventListener usernamePrevUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    prevUsername = (String) dataSnapshot.getValue();
                    //ho tutti i dati di classifica
                    showQuestionResults();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DatabaseReference prevUserRef = lobby.child("players").child(prevUserUID).child("username");
        prevUserRef.addListenerForSingleValueEvent(usernamePrevUserListener);
    }

    private  void setupEndQuestionListener(){
        endQuestionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Boolean end = (Boolean) dataSnapshot.getValue();
                    if(end){
                        if(endQuestionListener != null){
                            DatabaseReference timeOutRef = lobby.child("Question "+currentQuestion).child("end");
                            timeOutRef.removeEventListener(endQuestionListener);
                        }
                        if( currentQuestion == 10){

                            Intent intent = new Intent(getApplicationContext(), StartPushNotificationProcessService.class);
                            intent.putExtra("gameCode", gameCode);
                            intent.putExtra("myPosition", myPosition);
                            startService(intent);
                            //TODO: go to endQuiz Activity.
                            presentEndQuizActivity();
                        }
                        else{
                            //TODO: go back to QuestionActivity
                            currentQuestion += 1; //needs to be passed to QeustionActivity.
                            dismissQuestionResults();
                        }
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference endQuestionRef = lobby.child("Question "+currentQuestion).child("end");
        endQuestionRef.addValueEventListener(endQuestionListener);
    }




    private void dismissQuestionResults(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("currentQuestion",currentQuestion);
        setResult(this.RESULT_OK,returnIntent);
        NavUtils.navigateUpFromSameTask(this);
    }

    private void presentEndQuizActivity(){
        Intent intent = new Intent(getApplicationContext(), EndQuizActivity.class);
        intent.putExtra("myPosition",myPosition);
        intent.putExtra("totalPoints",totalPoints);
        intent.putExtra("username", username);
        startActivity(intent);

    }






}

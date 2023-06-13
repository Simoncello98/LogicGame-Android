package com.example.logicgame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class QuestionActivity extends AppCompatActivity implements ShakeDetector.OnShakeListener {

    private String gameCode;
    private int currentQuestion;
    private String teacherUID;
    private DatabaseReference lobby;
    private String myRandomUID;
    private String username;
    private Double totalPoints;
    private Double currentQuestionPoints;
    private Double currentQuestionBonusPoints;
    private Boolean timedOut;
    private Timer currentQuestionTimer;

    private Integer timeToAnswer;

    private Boolean correctOutput;

    private WebView linesWebView;
    private TextView questionNumberLabel;
    private Button sendButton;
    private ImageView questionImage;

    private EditText firstBitEditText;
    private EditText secondBitEditText;
    private EditText thirdBitEditText;
    private EditText fourthBitEditText;


    ValueEventListener timeoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("function", "onCreate");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_question);
        totalPoints = 0.0;
        getRefs();
        hideQuestionImage();
        getTeacherUIDAndFirstQuestionData();
    }



    private void getRefs(){
        myRandomUID = getIntent().getStringExtra("myRandomUID");
        gameCode = getIntent().getStringExtra("gameCode");
        username = getIntent().getStringExtra("username");
        lobby = FirebaseDatabase.getInstance().getReference("lobby").child(gameCode);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setClickable(true);
        sendButton.setEnabled(true);
        //sendButton.
        linesWebView = findViewById(R.id.linesWebView);
        linesWebView.getSettings().setJavaScriptEnabled(true);
        linesWebView.setBackgroundColor(Color.TRANSPARENT);
        linesWebView.setInitialScale(383);
        questionNumberLabel = findViewById(R.id.greetingsLabel);
        questionImage = findViewById(R.id.questionImage);

        firstBitEditText = findViewById(R.id.firstBit);
        secondBitEditText = findViewById(R.id.secondBit);
        thirdBitEditText = findViewById(R.id.thirdBit);
        fourthBitEditText = findViewById(R.id.fourthBit);



    }

    private void setupShakeListener(){
        ShakeDetector sd = new ShakeDetector(this);
        sd.setOnShakeListener(this);
    }

    @Override
    public void onShake(int count) {
        if (count > 4) count = 4;
        //count bit random
        for(int i = 0; i < count; i++){
            TextView t = getFirstEmptyTextView();
            if(t!= null){
                final int min = 0;
                final int max = 1;
                int random = new Random().nextInt((max - min) + 1) + min;
                t.setText(String.valueOf(random));
            }
        }
    }

    private TextView getFirstEmptyTextView(){
        if (firstBitEditText.getText().length() == 0) return firstBitEditText;
        if (secondBitEditText.getText().length() == 0) return secondBitEditText;
        if (thirdBitEditText.getText().length() == 0) return thirdBitEditText;
        if (fourthBitEditText.getText().length() == 0) return fourthBitEditText;
        return null;
    }

    private void getTeacherUIDAndFirstQuestionData(){
        currentQuestion = 1;
        ValueEventListener teacherUIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if(snap.exists()){
                    String uid = (String) snap.getValue();
                    teacherUID = uid;
                    startQuestionProcess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error reading", "Error database");
            }
        };
        lobby.child("teacher").addListenerForSingleValueEvent(teacherUIDListener);
    }


    private void startQuestionProcess(){
        timedOut = false;
        correctOutput = false;
        timeToAnswer = 60;
        currentQuestionBonusPoints = 0.0;
        currentQuestionPoints = 0.0;
        firstBitEditText.setText("");
        secondBitEditText.setText("");
        thirdBitEditText.setText("");
        fourthBitEditText.setText("");
        setupTimeoutListener();
        getCurrentQuestionData();
        setListenerForSendButton();
    }
    private void hideQuestionImage(){
        linesWebView.setVisibility(View.INVISIBLE);
        questionImage.setVisibility(View.INVISIBLE);
        firstBitEditText.setVisibility(View.INVISIBLE);
        secondBitEditText.setVisibility(View.INVISIBLE);
        thirdBitEditText.setVisibility(View.INVISIBLE);
        fourthBitEditText.setVisibility(View.INVISIBLE);
    }
    private void showQuestionImage(){
        linesWebView.setVisibility(View.VISIBLE);
        questionImage.setVisibility(View.VISIBLE);
        firstBitEditText.setVisibility(View.VISIBLE);
        secondBitEditText.setVisibility(View.VISIBLE);
        thirdBitEditText.setVisibility(View.VISIBLE);
        fourthBitEditText.setVisibility(View.VISIBLE);
    }


    private void getCurrentQuestionData(){
        ValueEventListener questionData = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                 String linesHTML = (String) dataSnapshot.child("lines").getValue();
                 String imageBase64 = (String) dataSnapshot.child("imageBase64").getValue();
                 updateUIWithData(linesHTML,imageBase64);
                 /*String html = (String) dataSnapshot.child("html").getValue();
                 updateUIWithDataHTML(html);*/
              }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference("users").child(teacherUID);
        teacherRef.child("quiz").child("Question " + currentQuestion).addListenerForSingleValueEvent(questionData);
    }

    private void updateUIWithData(String linesHTML, String imageBase64){
        Log.d("Download","linesHTML  e immagine ottenuti");
        linesWebView.loadDataWithBaseURL("", linesHTML, "text/html", "UTF-8", "");
        String base64String = imageBase64;
        String base64Image = base64String.split(",")[1];
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        questionImage.setImageBitmap(decodedByte);
        questionNumberLabel.setText("Question "+ currentQuestion);
        linesWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                showQuestionImage();
                startTimer();
                setupShakeListener();

            }
        });

    }


    /*private void updateUIWithDataHTML(String HTML){
        Log.d("Download","linesHTML  e immagine ottenuti");
        linesWebView.loadDataWithBaseURL("", HTML, "text/html", "UTF-8", "");

    }*/

    private void startTimer(){
        currentQuestionTimer = new Timer();
        currentQuestionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeToAnswer -= 1;
                if (timeToAnswer == 0) {
                    invalidateTimer();
                    timeToAnswer = 60;
                }
            }
        }, 0, 1000);

    }

    private void invalidateTimer(){
        currentQuestionTimer.cancel();
    }


    private void setupTimeoutListener(){

        timeoutListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                   Boolean timeoutValue = (Boolean) dataSnapshot.getValue();
                   if (timeoutValue){
                       Log.d("timedOut", "true");
                       DecimalFormat df = new DecimalFormat("####0.0");
                       DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                       symbols.setDecimalSeparator('.');
                       df.setDecimalFormatSymbols(symbols);
                       lobby.child("classification").child(myRandomUID).setValue(df.format(totalPoints));
                       DatabaseReference questionRef = lobby.child("players").child(myRandomUID).child("Question "+currentQuestion);
                       questionRef.child("correctOutput").setValue(false);
                       timedOut = true;
                       if(timeoutListener != null){
                           DatabaseReference timeOutRef = lobby.child("Question "+currentQuestion).child("timeOut");
                           timeOutRef.removeEventListener(timeoutListener);
                       }
                       presentQuestionResultActivity();
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



    //in the QuestionResult we will have an EndListener that indicates to go to the next question.
    private void sendButtonClicked(){
        Boolean validation = checkOutput();
        if (validation){
            //invalidate all observers.
            DatabaseReference timeOutRef = lobby.child("Question "+currentQuestion).child("timeOut");
            timeOutRef.removeEventListener(timeoutListener);
            setResult();

        }

        //get output from firebase
        //check output
        //save correctOutput -> true/false
        //gameRef.child("classification").child(self.myRandomID).setValue(self.myPoints)
        //TODO: set correctOutput

        //checkOutputAndSetResult
        //send to the QuestionResult - QuestionResult has to check the timer And Download ClassificationData.

    }

    private void setResult(){
        ValueEventListener outputListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String output = (String) dataSnapshot.getValue();
                    //calculate myoutputString
                    DatabaseReference myCorrectOutputRef = lobby.child("players").child(myRandomUID).child("Question "+currentQuestion).child("correctOutput");
                    String myOutput = firstBitEditText.getText() + "-" + secondBitEditText.getText() + "-" + thirdBitEditText.getText() + "-" + fourthBitEditText.getText();
                    Log.d("MYoutput", myOutput);
                    Log.d("output", output);

                    DecimalFormat df = new DecimalFormat("####0.0");
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setDecimalSeparator('.');
                    df.setDecimalFormatSymbols(symbols);

                    if(output.equalsIgnoreCase(myOutput)){
                        correctOutput = true;
                        myCorrectOutputRef.setValue(true);
                        Double bonusPoints = (timeToAnswer.doubleValue() / 10.0);
                        currentQuestionBonusPoints = bonusPoints;
                        currentQuestionPoints = 10.0 + bonusPoints;
                        totalPoints += currentQuestionPoints;
                        lobby.child("classification").child(myRandomUID).setValue(df.format(totalPoints));
                    }
                    else{
                        correctOutput = false;
                        myCorrectOutputRef.setValue(false);
                        currentQuestionPoints = 0.0;

                        lobby.child("classification").child(myRandomUID).setValue(df.format(totalPoints));
                    }
                    presentQuestionResultActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        //get output from firebase
        DatabaseReference thisQuestionRef = FirebaseDatabase.getInstance().getReference("users").child(teacherUID).child("quiz").child("Question "+currentQuestion).child("output");
        thisQuestionRef.addListenerForSingleValueEvent(outputListener);

    }

    private Boolean checkOutput(){
        Boolean result = true;
        if(!firstBitEditText.getText().toString().equalsIgnoreCase("0") && !firstBitEditText.getText().toString().equalsIgnoreCase("1")){
            firstBitEditText.setBackgroundColor(Color.RED);
            result = false;
        }
        else   firstBitEditText.setBackgroundColor(Color.TRANSPARENT);
        if(!secondBitEditText.getText().toString().equalsIgnoreCase("0") && !secondBitEditText.getText().toString().equalsIgnoreCase("1")){
            secondBitEditText.setBackgroundColor(Color.RED);
            result = false;
        }
        else secondBitEditText.setBackgroundColor(Color.TRANSPARENT);
        if(!thirdBitEditText.getText().toString().equalsIgnoreCase("0") && !thirdBitEditText.getText().toString().equalsIgnoreCase("1")){
            thirdBitEditText.setBackgroundColor(Color.RED);
            result = false;
        }
        else thirdBitEditText.setBackgroundColor(Color.TRANSPARENT);
        if(!fourthBitEditText.getText().toString().equalsIgnoreCase("0") && !fourthBitEditText.getText().toString().equalsIgnoreCase("1")){
            fourthBitEditText.setBackgroundColor(Color.RED);
            result = false;
        }
        else fourthBitEditText.setBackgroundColor(Color.TRANSPARENT);
        return result;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("intent", ""+ intent.getFlags());
    }

    private void presentQuestionResultActivity(){
        hideQuestionImage();
        Intent intent = new Intent(getApplicationContext(), QuestionResultActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("gameCode", gameCode);
        intent.putExtra("myRandomUID", myRandomUID);
        intent.putExtra("timedOut", timedOut);
        intent.putExtra("correctOutput",correctOutput);
        intent.putExtra("totalPoints", totalPoints);
        intent.putExtra("currentQuestion",currentQuestion);
        intent.putExtra("bonusPoints", currentQuestionBonusPoints);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(getApplicationContext(),"Result received",Toast.LENGTH_SHORT).show();
        currentQuestion = data.getIntExtra("currentQuestion",0);
        Log.d("currentQuestion", ""+currentQuestion);
        //reset all and start process again
        startQuestionProcess();
    }



    private void setListenerForSendButton(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendButtonClicked();
            }
        });
    }



}

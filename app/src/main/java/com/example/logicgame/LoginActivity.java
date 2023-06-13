package com.example.logicgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class LoginActivity extends AppCompatActivity {
    private String gameCode;
    private String username;
    private String myRandomUID;

    private Button joinButton;
    private EditText usernameEditText;
    private EditText gameCodeEditText;
    private TextView errorTextView;

    private String FCMToken;

    private ProgressBar activityIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login_activity);
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.default_notification_channel_id);
            String description = getString(R.string.default_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRefs();
        errorTextView.setVisibility(View.INVISIBLE);
        activityIndicator.setVisibility(View.INVISIBLE);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("error", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        FCMToken = task.getResult().getToken();
                    }
                });
        setListeners();
    }


    private void getRefs(){
        joinButton = findViewById(R.id.joinButton);
        usernameEditText = findViewById(R.id.usernameTextEdit);
        gameCodeEditText = findViewById(R.id.codeTextEdit);
        errorTextView =  findViewById(R.id.errorTextView);
        activityIndicator = findViewById(R.id.activityQuestionResultsProgress);
    }

    private void setListeners(){
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                errorTextView.setVisibility(View.INVISIBLE);
                Boolean fieldsAreOk = checkAndGetFields();
                if (fieldsAreOk){
                    join();
                }
            }
        });

    }


    private Boolean checkAndGetFields(){
        gameCode = gameCodeEditText.getText().toString();
        username = usernameEditText.getText().toString();
        if(username.length() == 0 || gameCode.length() == 0) {
            errorTextView.setText("Inserisci tutti i dati per favore!");
            errorTextView.setVisibility(View.VISIBLE);
            activityIndicator.setVisibility(View.INVISIBLE);
            return false;
        }

        if(username.length() > 10){
            errorTextView.setText("Il nome può essere max 10 caratteri!");
            errorTextView.setVisibility(View.VISIBLE);
            activityIndicator.setVisibility(View.INVISIBLE);
            return false;
        }
        activityIndicator.setVisibility(View.VISIBLE);
        return true;
    }


    private void join(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference lobby = database.getReference("lobby").child(gameCode);

        final ValueEventListener myUsernameExists = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if(!snap.exists()){
                    //Entro in partita
                    DatabaseReference myPlayerRef = lobby.child("players").push();
                    myRandomUID = myPlayerRef.getKey();
                    myPlayerRef.child("username").setValue(username);
                    myPlayerRef.child("FCMToken").setValue(FCMToken);
                    //lobby.child("FCMTokens").child(myRandomUID).setValue(FCMToken);

                    //TODO: presentWaitingActivity
                    presentWaitingActivity();
                }
                else{
                    errorTextView.setText("Questo nome utente è già stato utilizzato.");
                    errorTextView.setVisibility(View.VISIBLE);
                    activityIndicator.setVisibility(View.INVISIBLE);
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error reading", "Error database");
            }
        };

        final ValueEventListener startedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                if (snap.exists()){
                    if((Boolean) snap.getValue() == false){
                        lobby.child("players").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(myUsernameExists);
                    }
                    else{
                        errorTextView.setText("Partita già iniziata, troppo tardi!");
                        errorTextView.setVisibility(View.VISIBLE);
                        activityIndicator.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    errorTextView.setText("Non esiste alcuna lobby con questo code!");
                    errorTextView.setVisibility(View.VISIBLE);
                    activityIndicator.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error reading", "Error database");
            }
        };
        lobby.child("started").addListenerForSingleValueEvent(startedListener);

    }

    private void presentWaitingActivity(){
        Intent intent = new Intent(getBaseContext(), WaitingActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("gameCode", gameCode);
        intent.putExtra("myRandomUID", myRandomUID);
        startActivity(intent);
    }

    public void hideKeyboard() {
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

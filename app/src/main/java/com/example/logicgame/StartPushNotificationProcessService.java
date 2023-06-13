package com.example.logicgame;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class StartPushNotificationProcessService extends Service {

    DatabaseReference lobby;
    int myPosition;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get datas
        String gameCode = intent.getStringExtra("gameCode");
        myPosition = intent.getIntExtra("myPosition", 0);
        lobby = FirebaseDatabase.getInstance().getReference("lobby").child(gameCode);

        Log.d("SERVICE OK", "start");

        //background Thread

        Thread thread = new Thread(null, startPushNotificationCheck, "Background");
        thread.start();
        Log.d("thread", "start");

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Runnable startPushNotificationCheck = new Runnable() {
        @Override
        public void run() {
            startTimer();
        }
    };

    private void startTimer(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                checkFlagForNotificationSend();
                this.cancel();
            }
        }, myPosition * 1000);
    }

    private void checkFlagForNotificationSend(){
        Log.d("CHECK", "send flag");
        ValueEventListener sendPushListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    lobby.child("sendPush").setValue(true);
                }
                stopSelf(); //service completed
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        lobby.child("sendPush").addListenerForSingleValueEvent(sendPushListener);
    }

}

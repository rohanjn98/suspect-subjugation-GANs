package com.example.cb_f;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;


public class CoverActivity extends AppCompatActivity {

    ImageView coverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);
        Timer timer = new Timer();
        coverImage = findViewById(R.id.coverImage);

        timer.schedule(new TimerTask() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent i;
                if(user != null){
                    i = new Intent(CoverActivity.this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
                else{
                    handler.sendEmptyMessage(0);
                }

            }

        }, 2000);



    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(message.what==0){
                Intent i = new Intent(CoverActivity.this, LoginActivity.class);
               /* Pair pairs = new Pair<View, String>(coverImage, "imageTransition");
                ActivityOptions options = makeSceneTransitionAnimation(CoverActivity.this, pairs);*/
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i /*options.toBundle()*/);
            }
            return false;
        }
    });
}


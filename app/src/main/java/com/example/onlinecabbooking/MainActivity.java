package com.example.onlinecabbooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinecabbooking.driverui.DriverLoginRegisterActivity;
import com.example.onlinecabbooking.news.NewsActivity;
import com.example.onlinecabbooking.passengerui.CustomerLoginRegisterActivity;


public class MainActivity extends AppCompatActivity {

    float x1, y1, x2, y2;
    TextView hidetext;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //CountDownTimer timer = new MyCountDown(5000, 1000);
        hidetext = findViewById(R.id.hidetext);
       imageView = findViewById(R.id.imageView);

        // fade out view nicely after 5 seconds
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        alphaAnim.setStartOffset(4000);                        // start in 4 seconds
        alphaAnim.setDuration(400);
        alphaAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation)
            {
                // make invisible when animation completes, you could also remove the view from the layout
                hidetext.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        hidetext.setAnimation(alphaAnim);
        imageView.setAnimation(alphaAnim);


    }


    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 > x2) {
                    Intent i = new Intent(MainActivity.this, NewsActivity.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }

    public void open_driver(View V) {

        startActivity(new Intent(MainActivity.this, DriverLoginRegisterActivity.class));
    }

    public void open_passenger(View V) {
        startActivity(new Intent(MainActivity.this, CustomerLoginRegisterActivity.class));
    }



}
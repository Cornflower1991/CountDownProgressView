package com.cornflower1991.countdownprogressview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cornflower1991.library.CountDownProgressView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

   private CountDownProgressView mCountDownProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        mCountDownProgressView = (CountDownProgressView) findViewById(R.id.progressPieView);
        mCountDownProgressView.setOnProgressListener(new CountDownProgressView.OnProgressListener() {

            @Override
            public void onProgressChanged(long progress) {
                if (!mCountDownProgressView.isTextShowing()) {
                    mCountDownProgressView.setShowText(true);
                    mCountDownProgressView.setShowImage(false);
                }
            }

            @Override
            public void onProgressCompleted() {
                if (!mCountDownProgressView.isImageShowing()) {
                    mCountDownProgressView.setShowImage(true);
                }
                mCountDownProgressView.setShowText(false);
                mCountDownProgressView.setImageResource(R.mipmap.ic_action_accept);
            }
        });
    }

    @Override
    public void onClick(View view) {
        mCountDownProgressView.startCountDownTime(10*1000);
    }


}

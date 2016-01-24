package com.nego.flite;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class MyAudioRecord extends AppCompatActivity
{
    private static File mFileName = null;

    private MediaRecorder mRecorder = null;
    private Handler mHandlerVoice = new Handler();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_audio_record);

        findViewById(R.id.back_to_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        findViewById(R.id.record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
        mFileName = new File(this.getFilesDir() + File.separator + Costants.DIRECTORY_VOICE_NOTE + File.separator + Calendar.getInstance().getTimeInMillis() + "_audio.3gp");

        startRecording();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName.getPath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        mRecorder.start();
        showEffect();

    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Intent result = new Intent();
        result.putExtra(Costants.EXTRA_ACTION_TYPE, mFileName.getAbsolutePath());
        setResult(RESULT_OK, result);
        finish();
    }

    public void showEffect() {
        new Thread(new Runnable() {
            public void run() {
                mHandlerVoice.postDelayed(new Runnable() {
                    public void run() {
                        findViewById(R.id.record_button).animate().setInterpolator(new AccelerateDecelerateInterpolator())
                                .alpha(0)
                                .setDuration(1000)
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        findViewById(R.id.record_button).animate().setInterpolator(new AccelerateDecelerateInterpolator())
                                                .alpha(1)
                                                .setDuration(1000)
                                                .setListener(new Animator.AnimatorListener() {
                                                    @Override
                                                    public void onAnimationStart(Animator animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        showEffect();
                                                    }

                                                    @Override
                                                    public void onAnimationCancel(Animator animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animator animation) {

                                                    }
                                                })
                                                .start();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                })
                                .start();
                    }
                }, 200);
            }
        }).start();
    }
}

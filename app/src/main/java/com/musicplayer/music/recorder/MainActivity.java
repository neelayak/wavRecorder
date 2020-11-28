
package com.musicplayer.music.recorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.visualizer.amplitude.AudioRecordView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    ImageButton record_button, play_button;
    public static final int RequestPermissionCode = 1;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;


    private MediaRecorder recorder = null;

    private MediaPlayer player = null;

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    AudioRecordView audioRecordView;

    private static Wavrecordernew wavrecordernew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        record_button = findViewById(R.id.record_button);
        play_button = findViewById(R.id.play_button);
        audioRecordView = findViewById(R.id.audioRecordView);


        requestPermission();
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(mStartPlaying);

                if (mStartPlaying) {
                    play_button.setImageResource(R.drawable.ic_pause_circle_filled_24px);
                } else {
                    play_button.setImageResource(R.drawable.ic_play_circle_filled_24px);
                }
                mStartPlaying = !mStartPlaying;
            }
        });
        record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    onRecord(mStartRecording);
                    if (mStartRecording) {
                        record_button.setImageResource(R.drawable.ic_stop_24px);
                        audioRecordView.recreate();
                    } else {
                        record_button.setImageResource(R.drawable.ic_mic_24px);
                    }
                    mStartRecording = !mStartRecording;
                } else {
                    requestPermission();
                }

            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
        }
//        switch (requestCode) {
//            case REQUEST_RECORD_AUDIO_PERMISSION:
//                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//        }
//        if (!permissionToRecordAccepted) finish();

    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    private void onRecord(boolean start) {
        try {
            if (start) {
                startRecording();
            } else {
                stopRecording();
            }
        } catch (IllegalStateException e) {

        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            Log.e("File_loc", wavrecordernew.getFilename());
            player.setDataSource(wavrecordernew.getFilename());
            player.prepare();
            player.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            //   /sdcard/Android/data/com.musicplayer.music.recorder/cache/Automator/Audio.wav
            //  /storage/emulated/0/Android/data/com.musicplayer.music.recorder/cacheAudio.wav
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }

    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String wavfilepath = getExternalCacheDir().getAbsolutePath();
        String filename = "Audio.wav";
        wavrecordernew.startRecording(wavfilepath, filename);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();

        startAV();
    }

    private void stopRecording() {
        wavrecordernew.stopRecording();
        recorder.stop();
        recorder.release();

        recorder = null;

    }


    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            stopAV();
            recorder.release();
            recorder = null;

        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void stopAV() {
        audioRecordView.recreate();
    }

    public void startAV() {
        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    int alt = recorder.getMaxAmplitude();
                    audioRecordView.update(alt);
                } catch (NullPointerException e) {

                }

            }
        };
        timer.scheduleAtFixedRate(tt, 50, 500);
    }


}
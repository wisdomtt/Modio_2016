package com.example.curtismason.mft6;
import java.io.File;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;





public class MainActivity extends AppCompatActivity {
    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;
    MediaRecorder recorder;
    File audiofile = null;
    private static final String TAG = "SoundRecordingActivity";
    private View startButton;
    private View stopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });


    }


    public void startRecording(View view) throws IOException {

        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        File sampleDir = Environment.getExternalStorageDirectory();
        try {
            audiofile = File.createTempFile("sample", ".3gp", sampleDir);
        } catch (IOException e) {
            Log.e(TAG, "sdcard access error");
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording(View view) {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        recorder.stop();
        recorder.release();
        addRecordingToMediaLibrary();
    }

    protected void addRecordingToMediaLibrary() {
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
        ContentResolver contentResolver = getContentResolver();

        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }
    public void onTouch(View v) {

            // Getting the user sound settings
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            float actualVolume = (float) audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume / maxVolume;
            // Is the sound loaded already?
            if (loaded) {
                soundPool.play(soundID, volume, volume, 1, 0, 1f);
                Log.e("Test", "Played sound");

        }
        soundID = soundPool.load(audiofile.getAbsolutePath(), 1);

    }
}
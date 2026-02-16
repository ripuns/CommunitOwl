package com.example.communityowl;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

public class AlertService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Monitoring Community Security...", Toast.LENGTH_LONG).show();

        // Play pre-recorded audio message
        playMonitoringAudio();

        // simulate a security alert being triggered
        sendSecurityBroadcast();

        return START_STICKY;
    }

    private void playMonitoringAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.monitoring_started);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
        }
    }

    private void sendSecurityBroadcast() {
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("com.community.SECURITY_ALERT");
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
        Toast.makeText(this, "Security Monitoring Stopped", Toast.LENGTH_SHORT).show();
    }
}

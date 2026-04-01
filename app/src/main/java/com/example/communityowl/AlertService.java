package com.example.communityowl;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

public class AlertService extends Service {

    private MediaPlayer mediaPlayer;

    // this runs when the service starts and kicks off the security monitoring tasks
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Monitoring Community Security...", Toast.LENGTH_LONG).show();
        playMonitoringAudio();
        sendSecurityBroadcast();
        // this keeps the service running even if the app is closed until we stop it manually
        return START_STICKY;
    }

    // this sets up and plays a sound file to let the user know monitoring has begun
    private void playMonitoringAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.monitoring_started);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            // this cleans up the music player once the sound finishes playing
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
        }
    }

    // this sends out a message that other parts of the app can pick up to show an alert
    private void sendSecurityBroadcast() {
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("com.community.SECURITY_ALERT");
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // this cleans up resources like the music player when the service is stopped
    @Override
    public void onDestroy() {
        // making sure we stop and release the music player to save battery and memory
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
        Toast.makeText(this, "Security Monitoring Stopped", Toast.LENGTH_SHORT).show();
    }
}

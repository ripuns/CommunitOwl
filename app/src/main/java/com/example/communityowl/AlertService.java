package com.example.communityowl; // Check your package name

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class AlertService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // This code runs when the service is started
        Toast.makeText(this, "Monitoring Community Security...", Toast.LENGTH_LONG).show();

        // Simulate a security alert being triggered
        // In a real app, this would listen to a web server
        sendSecurityBroadcast();

        return START_STICKY; // Keeps the service running in the background
    }

    private void sendSecurityBroadcast() {
        // This sends a signal that our Broadcast Receiver will catch
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("com.community.SECURITY_ALERT");
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't need binding for this simple app
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Security Monitoring Stopped", Toast.LENGTH_SHORT).show();
    }
}
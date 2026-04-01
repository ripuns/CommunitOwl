package com.example.communityowl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {
    // this method is called when the system or another app sends a broadcast that this receiver is looking for
    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("CommunityOwl", "Broadcast Received successfully!");
        // this checks if the broadcast received is specifically our security alert
        if ("com.community.SECURITY_ALERT".equals(intent.getAction())) {
            Toast.makeText(context, "EMERGENCY: Suspicious activity detected in your area!", Toast.LENGTH_LONG).show();
        }
    }
}
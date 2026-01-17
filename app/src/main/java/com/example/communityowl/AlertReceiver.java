package com.example.communityowl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("CommunityOwl", "Broadcast Received successfully!");
        // This code runs when the "SECURITY_ALERT" broadcast is received
        if ("com.community.SECURITY_ALERT".equals(intent.getAction())) {
            Toast.makeText(context, "EMERGENCY: Suspicious activity detected in your area!", Toast.LENGTH_LONG).show();

            // Note: In a final version, you would trigger a Notification here
        }
    }
}
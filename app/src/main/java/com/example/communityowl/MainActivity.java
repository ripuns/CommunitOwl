package com.example.communityowl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View btnSecurity = findViewById(R.id.btnSecurity);
        View btnInfra = findViewById(R.id.btnInfra);
        View btnChat = findViewById(R.id.btnChat);
        View btnService = findViewById(R.id.btnService);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        btnSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecurityActivity.class);
            startActivity(intent);
        });

        btnInfra.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InfraActivity.class);
            startActivity(intent);
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        btnService.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, AlertService.class);
            startService(serviceIntent);

            String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // 1. Log to Security History
            logServiceStart(currentTime);

            // 2. Send automated message to Community Chat
            postToCommunityChat("System: Security threat detected by a neighbor at " + currentTime);

            Toast.makeText(MainActivity.this, "Security Service Started", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void logServiceStart(String time) {
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> history = new HashSet<>(prefs.getStringSet("security_history", new HashSet<>()));
        history.add("Service Started - " + time);
        prefs.edit().putStringSet("security_history", history).apply();
    }

    private void postToCommunityChat(String message) {
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        String currentChat = prefs.getString("chat_history", "System: Welcome to the community chat!\n\nNeighbor 1: Is the park open today?\nNeighbor 2: Yes, it is! Just walked by.\n");
        String updatedChat = currentChat + "\n" + message;
        prefs.edit().putString("chat_history", updatedChat).apply();
    }
}
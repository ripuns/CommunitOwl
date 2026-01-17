package com.example.communityowl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

        // Changed from Button to View because they are now LinearLayouts in the XML
        View btnSecurity = findViewById(R.id.btnSecurity);
        View btnInfra = findViewById(R.id.btnInfra);
        View btnChat = findViewById(R.id.btnChat);
        View btnService = findViewById(R.id.btnService);

        btnSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecurityActivity.class);
                startActivity(intent);
            }
        });

        btnInfra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfraActivity.class);
                startActivity(intent);
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        btnService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the service
                Intent serviceIntent = new Intent(MainActivity.this, AlertService.class);
                startService(serviceIntent);
                
                // Log the start time to SharedPreferences
                logServiceStart();

                Toast.makeText(MainActivity.this, "Security Service Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logServiceStart() {
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> history = new HashSet<>(prefs.getStringSet("security_history", new HashSet<String>()));
        
        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        history.add("Service Started - " + currentTime);
        
        prefs.edit().putStringSet("security_history", history).apply();
    }
}
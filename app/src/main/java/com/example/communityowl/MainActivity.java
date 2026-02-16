package com.example.communityowl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);
        
        // Configure Google Sign In for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

            // log to security history (SharedPreferences)
            logServiceStart(currentTime);
            
            // log to SQLite monitoring table
            databaseHelper.addMonitoringEvent(currentTime, "Service Started");
            
            // send automated message to community chat (now uses SQLite via ChatActivity logic, 
            // but here we still update prefs for compatibility if needed, or we could use DB helper)
            postToCommunityChat("Security threat detected by a neighbor at " + currentTime);

            Toast.makeText(MainActivity.this, "Security Service Started", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
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
        // Saving the automated alert to the messages table as well
        databaseHelper.addMessage("System", message);
        
        // Keeping SharedPreferences for legacy compatibility if other parts of the app use it
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        String currentChat = prefs.getString("chat_history", "");
        String updatedChat = currentChat + "\n" + message;
        prefs.edit().putString("chat_history", updatedChat).apply();
    }
}

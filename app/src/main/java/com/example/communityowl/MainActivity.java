package com.example.communityowl;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
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
            checkLocationPermissionAndStart();
        });

        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void checkLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startSecurityMonitoring();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSecurityMonitoring();
            } else {
                Toast.makeText(this, "Location permission denied. Starting without location.", Toast.LENGTH_SHORT).show();
                startSecurityMonitoring();
            }
        }
    }

    private void startSecurityMonitoring() {
        Intent serviceIntent = new Intent(MainActivity.this, AlertService.class);
        startService(serviceIntent);

        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        // log to security history
        logServiceStart(currentTime);
        
        // log to SQLite monitoring table
        databaseHelper.addMonitoringEvent(currentTime, "Service Started");

        // Try to get location and post message
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                String message = "Security threat detected by a neighbor at " + currentTime;
                if (location != null) {
                    message += "\nLocation: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                }
                postToCommunityChat(message);
            });
        } else {
            postToCommunityChat("Security threat detected by a neighbor at " + currentTime);
        }

        Toast.makeText(MainActivity.this, "Security Service Started", Toast.LENGTH_SHORT).show();
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

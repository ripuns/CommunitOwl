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
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
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

    // this sets up the main home screen of our app and handles all the menu button clicks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting ready to handle login, database, and location services
        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // configure google sign in for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // finding the buttons on the screen layout
        View btnSecurity = findViewById(R.id.btnSecurity);
        View btnInfra = findViewById(R.id.btnInfra);
        View btnChat = findViewById(R.id.btnChat);
        View btnService = findViewById(R.id.btnService);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        // navigation to the security history screen
        btnSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecurityActivity.class);
            startActivity(intent);
        });

        // navigation to the infrastructure reports screen
        btnInfra.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InfraActivity.class);
            startActivity(intent);
        });

        // navigation to the community chat screen
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // starting the security monitoring service
        btnService.setOnClickListener(v -> {
            checkLocationPermissionAndStart();
        });

        // opening the settings screen
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // triggering the logout process
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    // this checks if the user has given us permission to use their location before we start the security check
    private void checkLocationPermissionAndStart() {
        // if we don't have permission, we ask the user for it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // this pop-up asks the user to allow location access
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startSecurityMonitoring();
        }
    }

    // this deals with what happens right after the user clicks 'allow' or 'deny' on the location permission pop-up
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

    // this starts the background security service, records the start time, and tries to post the user's location to the community chat
    private void startSecurityMonitoring() {
        Intent serviceIntent = new Intent(MainActivity.this, AlertService.class);
        startService(serviceIntent);

        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        logServiceStart(currentTime);
        databaseHelper.addMonitoringEvent(currentTime, "Service Started");

        // if we have location permission, we try to get the exact coordinates to share in the chat
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fetching current location...", Toast.LENGTH_SHORT).show();
            
            // this tells the system to get a quick and accurate location fix
            CancellationTokenSource cts = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    String message = "Security threat detected by a neighbor at " + currentTime;
                    // if we got the location, we add a google maps link to the message
                    if (location != null) {
                        message += "\nLocation: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    } else {
                        message += "\n(Location unavailable)";
                    }
                    postToCommunityChat(message);
                })
                .addOnFailureListener(this, e -> {
                    postToCommunityChat("Security threat detected by a neighbor at " + currentTime + "\n(Failed to get location)");
                });
        } else {
            postToCommunityChat("Security threat detected by a neighbor at " + currentTime);
        }

        Toast.makeText(MainActivity.this, "Security Service Started", Toast.LENGTH_SHORT).show();
    }

    // this logs the user out of both firebase and google sign-in and takes them back to the login screen
    private void logout() {
        // signing out from firebase
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // this makes sure the user can't go back to the home screen by pressing the back button
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // this saves the exact time the security service started into the phone's shared memory for later viewing
    private void logServiceStart(String time) {
        // loading the current history list from our shared settings
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> history = new HashSet<>(prefs.getStringSet("security_history", new HashSet<>()));
        // adding the new start time to our history set and saving it back
        history.add("Service Started - " + time);
        prefs.edit().putStringSet("security_history", history).apply();
    }

    // this adds an automated security alert message to our chat history so everyone in the community can see it
    private void postToCommunityChat(String message) {
        // saving the automated alert to our messages database table
        databaseHelper.addMessage("System", message);
        
        // also saving it to our old shared preferences chat history for compatibility
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        String currentChat = prefs.getString("chat_history", "");
        String updatedChat = currentChat + "\n" + message;
        prefs.edit().putString("chat_history", updatedChat).apply();
    }
}

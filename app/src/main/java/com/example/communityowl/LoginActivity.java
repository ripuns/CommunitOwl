package com.example.communityowl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseHelper databaseHelper;

    // this sets up the login screen, initializes firebase and google sign-in, and handles button clicks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) 
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // finding all the input fields and buttons on our login layout
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        TextView tvGoToSignup = findViewById(R.id.tvGoToSignup);

        // deciding what happens when the login button is pressed
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, password);
                }
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        tvGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    // this checks if a user is already logged in when the app opens so they don't have to log in again
    @Override
    protected void onStart() {
        super.onStart();
        // first we check if firebase remembers the user session
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            // then we check our local phone settings for a saved login
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            if (prefs.contains("logged_in_user")) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    // this tries to log the user in using firebase, and if that fails, it tries the local database as a backup
    private void loginUser(String email, String password) {
        // trying to authenticate with firebase over the internet
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            if (databaseHelper.checkUser(email, password)) {
                                loginSuccessFallback(email);
                            } else if (checkPreferences(email, password)) {
                                // checking basic shared preferences as a last resort
                                loginSuccessFallback(email);
                            } else {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                                Toast.makeText(LoginActivity.this, "Online: " + errorMessage + "\nLocal: User not found", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    // this handles a successful login from the local database by saving the user's session
    private void loginSuccessFallback(String email) {
        // marking the user as logged in within our local settings
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("logged_in_user", email).apply();
        
        Toast.makeText(this, "Logged in via Local Fallback", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    // this checks if the user's credentials match what we have saved in the phone's local settings
    private boolean checkPreferences(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedPassword = sharedPreferences.getString(email, null);
        return savedPassword != null && savedPassword.equals(password);
    }

    // this triggers the google sign-in screen to pop up
    private void signInWithGoogle() {
        // launching the system's google account picker
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // this catches the result from the google sign-in screen and processes it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // checking if this result is actually from our google sign-in request
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed. Code: " + e.getStatusCode());
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // this takes the token from google and uses it to log the user into firebase
    private void firebaseAuthWithGoogle(String idToken) {
        // creating a firebase credential using the google token
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Google Auth failed";
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                        }
                    }
                });
    }
}

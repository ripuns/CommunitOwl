package com.example.communityowl;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity {

    private TextView chatDisplay;
    private ScrollView chatScroll;
    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;
    private final String DEFAULT_CHAT = "System: Welcome to the community chat!\n\nNeighbor 1: Is the park open today?\nNeighbor 2: Yes, it is! Just walked by.\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        chatDisplay = findViewById(R.id.chatDisplay);
        chatScroll = findViewById(R.id.chatScroll);
        final EditText messageInput = findViewById(R.id.messageInput);
        ImageButton btnSend = findViewById(R.id.btnSend);

        loadChat();

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                saveMessage(message);
                messageInput.setText("");
                loadChat();
            }
        });
    }

    private void saveMessage(String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        String sender = (user != null && user.getEmail() != null) ? user.getEmail() : "Anonymous";
        
        // Save to SQLite
        databaseHelper.addMessage(sender, content);
    }

    private void loadChat() {
        String history = databaseHelper.getAllMessages();
        if (history.isEmpty()) {
            chatDisplay.setText(DEFAULT_CHAT);
        } else {
            chatDisplay.setText(DEFAULT_CHAT + "\n" + history);
        }
        chatScroll.post(() -> chatScroll.fullScroll(android.view.View.FOCUS_DOWN));
    }
}

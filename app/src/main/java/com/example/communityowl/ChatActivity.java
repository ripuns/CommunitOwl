package com.example.communityowl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView chatDisplay;
    private final String DEFAULT_CHAT = "System: Welcome to the community chat!\n\nNeighbor 1: Is the park open today?\nNeighbor 2: Yes, it is! Just walked by.\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton btnBack = findViewById(R.id.btnBack);
        chatDisplay = findViewById(R.id.chatDisplay);
        final EditText messageInput = findViewById(R.id.messageInput);
        ImageButton btnSend = findViewById(R.id.btnSend);

        prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);

        loadChat();

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                String currentChat = prefs.getString("chat_history", DEFAULT_CHAT);
                String updatedChat = currentChat + "\nYou: " + message;
                prefs.edit().putString("chat_history", updatedChat).apply();
                chatDisplay.setText(updatedChat);
                messageInput.setText("");
            }
        });
    }

    private void loadChat() {
        String history = prefs.getString("chat_history", DEFAULT_CHAT);
        chatDisplay.setText(history);
    }
}
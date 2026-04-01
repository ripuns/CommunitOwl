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

    // this sets up the screen layout and connects all the buttons and text boxes when the chat starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        // finding all the parts of our chat screen by their id
        ImageButton btnBack = findViewById(R.id.btnBack);
        chatDisplay = findViewById(R.id.chatDisplay);
        chatScroll = findViewById(R.id.chatScroll);
        final EditText messageInput = findViewById(R.id.messageInput);
        ImageButton btnSend = findViewById(R.id.btnSend);

        loadChat();

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            // only sending the message if the user actually typed something
            if (!message.isEmpty()) {
                saveMessage(message);
                // clearing the input box
                messageInput.setText("");
                // refreshing the chat display
                loadChat();
            }
        });
    }

    // this figures out who is sending the message and saves it to our local database
    private void saveMessage(String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        String sender = (user != null && user.getEmail() != null) ? user.getEmail() : "Anonymous";
        
        // save to sqlite database
        databaseHelper.addMessage(sender, content);
    }

    // this pulls all the old messages from the database and shows them on the screen, then scrolls to the bottom
    private void loadChat() {
        String history = databaseHelper.getAllMessages();
        if (history.isEmpty()) {
            chatDisplay.setText(DEFAULT_CHAT);
        } else {
            chatDisplay.setText(DEFAULT_CHAT + "\n" + history);
        }
        // making sure the chat automatically scrolls down to show the newest messages
        chatScroll.post(() -> chatScroll.fullScroll(android.view.View.FOCUS_DOWN));
    }
}

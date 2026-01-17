package com.example.communityowl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final TextView chatDisplay = findViewById(R.id.chatDisplay);
        final EditText messageInput = findViewById(R.id.messageInput);
        final ImageButton btnSend = findViewById(R.id.btnSend);

        // Requirement D: Enable organized communication
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if (!message.isEmpty()) {
                    // Append the new message to the display
                    String currentChat = chatDisplay.getText().toString();
                    chatDisplay.setText(currentChat + "\nYou: " + message);
                    
                    // Clear the input field
                    messageInput.setText("");
                }
            }
        });
    }
}
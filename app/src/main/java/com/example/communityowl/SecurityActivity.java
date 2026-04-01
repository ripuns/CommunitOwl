package com.example.communityowl;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityActivity extends AppCompatActivity {
    // this sets up the security history screen and fills the list with all previous security events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        // linking the buttons and list view from the layout
        ImageButton btnBack = findViewById(R.id.btnBack);
        ListView listView = findViewById(R.id.alertListView);

        btnBack.setOnClickListener(v -> finish());
        Button btnClearHistory = findViewById(R.id.btnClearHistory);

        // grabbing the saved security alerts from the phone's storage
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> historySet = prefs.getStringSet("security_history", null);
        
        ArrayList<String> historyList;
        if (historySet == null || historySet.isEmpty()) {
            historyList = new ArrayList<>(List.of(
                    "No security issues reported"
            ));
        } else {
            // we sort the alerts so the most recent ones appear first
            historyList = new ArrayList<>(historySet);
            Collections.sort(historyList);
            Collections.reverse(historyList);
        }

        // custom arrayadapter to make sure our text matches the app's style
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, historyList) {
            // this method makes sure each item in our list is displayed with the correct text color
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
                return view;
            }
        };
        listView.setAdapter(adapter);

        // setting up the clear button to wipe out all the saved security logs
        btnClearHistory.setOnClickListener(v -> {
            // showing a pop-up to make sure the user didn't click it by accident
            AlertDialog.Builder builder = new AlertDialog.Builder(SecurityActivity.this);
            builder.setMessage("Are you sure you want to clear the history?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                prefs.edit().remove("security_history").apply();
                historyList.clear();
                adapter.notifyDataSetChanged();
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }
}

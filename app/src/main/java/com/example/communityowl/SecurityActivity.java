package com.example.communityowl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecurityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        ListView listView = findViewById(R.id.alertListView);

        // Load history from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> historySet = prefs.getStringSet("security_history", null);
        
        ArrayList<String> historyList;
        if (historySet == null) {
            // Default data if empty
            historyList = new ArrayList<>(Arrays.asList(
                "Suspicious Vehicle - 10:30 AM", 
                "Fire Drill - Yesterday", 
                "Stray Dog Alert - Monday"
            ));
        } else {
            historyList = new ArrayList<>(historySet);
            // Sort by time or reverse order could be added here
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
    }
}
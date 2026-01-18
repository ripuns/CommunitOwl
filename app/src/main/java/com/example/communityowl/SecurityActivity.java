package com.example.communityowl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class SecurityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ListView listView = findViewById(R.id.alertListView);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        Set<String> historySet = prefs.getStringSet("security_history", null);
        
        ArrayList<String> historyList;
        if (historySet == null) {
            historyList = new ArrayList<>(Arrays.asList(
                "Suspicious Vehicle - 10:30 AM", 
                "Fire Drill - Yesterday", 
                "Stray Dog Alert - Monday"
            ));
        } else {
            historyList = new ArrayList<>(historySet);
            
            // Simple sorting:
            // 1. Sort A-Z
            Collections.sort(historyList);
            // 2. Reverse it to get Z-A (Most recent dates/times at the top)
            Collections.reverse(historyList);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
    }
}
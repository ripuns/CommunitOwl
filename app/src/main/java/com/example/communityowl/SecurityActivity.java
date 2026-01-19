package com.example.communityowl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
        if (historySet == null || historySet.isEmpty()) {
            historyList = new ArrayList<>(List.of(
                    "No security issues reported"
            ));
        } else {
            historyList = new ArrayList<>(historySet);
            Collections.sort(historyList);
            Collections.reverse(historyList);
        }

        // Custom ArrayAdapter to set text color to BLACK
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, historyList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }
}

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
        if (historySet == null) {
            historyList = new ArrayList<>(List.of(
                    "No security issues reported"
            ));
        } else {
            historyList = new ArrayList<>(historySet);

            Collections.sort(historyList);
            Collections.reverse(historyList);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("chat_history", "Neighbor: Hello!");
        editor.apply();
        prefs.edit().remove("security_history").apply();
        prefs.edit().clear().apply();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
    }
}
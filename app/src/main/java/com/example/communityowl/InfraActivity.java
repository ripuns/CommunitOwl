package com.example.communityowl;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InfraActivity extends AppCompatActivity {
    
    private ArrayList<String> updatesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infra);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ListView listView = findViewById(R.id.infraListView);
        final EditText infraInput = findViewById(R.id.infraInput);
        ImageButton btnSend = findViewById(R.id.btnInfraSend);
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnUpload = findViewById(R.id.btnUpload);
        ImageButton btnRecord = findViewById(R.id.btnRecord);

        SharedPreferences prefs = getSharedPreferences("CommunityOwlPrefs", MODE_PRIVATE);
        btnBack.setOnClickListener(v -> {
            finish(); // goes back to MainActivity
        });
        Set<String> savedUpdates = prefs.getStringSet("infra_updates", null);

        if (savedUpdates == null) {
            // default data
            updatesList = new ArrayList<>(Arrays.asList(
                    "New Street Lights Installation - Park Avenue",
                    "Pothole Repair - 5th Cross Road",
                    "Park Renovation starting next Monday"
            ));
        } else {
            // load the previously saved list
            updatesList = new ArrayList<>(savedUpdates);
        }

        // custom ArrayAdapter to set text color to black
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, updatesList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = infraInput.getText().toString();
            if (!text.isEmpty()) {
                updatesList.add(0, "Update: " + text);
                Set<String> set = new HashSet<>(updatesList);
                prefs.edit().putStringSet("infra_updates", set).apply();
                adapter.notifyDataSetChanged();
                infraInput.setText("");
            }
        });

        View.OnClickListener multimediaListener = v -> Toast.makeText(InfraActivity.this, "Feature coming soon", Toast.LENGTH_SHORT).show();

        btnCamera.setOnClickListener(multimediaListener);
        btnUpload.setOnClickListener(multimediaListener);
        btnRecord.setOnClickListener(multimediaListener);
    }
}

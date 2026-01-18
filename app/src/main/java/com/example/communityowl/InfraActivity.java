package com.example.communityowl;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

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

        btnBack.setOnClickListener(v -> {
            finish(); // Goes back to MainActivity
        });

        updatesList = new ArrayList<>(Arrays.asList(
            "New Street Lights Installation - Park Avenue",
            "Pothole Repair - 5th Cross Road",
            "Park Renovation starting next Monday"
        ));

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, updatesList);
        listView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = infraInput.getText().toString();
            if (!text.isEmpty()) {
                updatesList.add(0, "Update: " + text);
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
package com.example.communityowl;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfraActivity extends AppCompatActivity {

    private ArrayList<String> updatesList;
    private ArrayAdapter<String> adapter;
    private EditText infraInput;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CommunityOwlPrefs";
    private static final String KEY_INFRA_UPDATES = "infra_updates_ordered_v2";
    private Uri photoUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    addUpdate("📷 Photo captured", photoUri.toString());
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Persist permission for the URI if possible
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            // Ignore if not possible
                        }
                        addUpdate("📁 File uploaded: " + uri.getLastPathSegment(), uri.toString());
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && !results.isEmpty()) {
                        infraInput.setText(results.get(0));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infra);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ListView listView = findViewById(R.id.infraListView);
        infraInput = findViewById(R.id.infraInput);
        ImageButton btnSend = findViewById(R.id.btnInfraSend);
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnUpload = findViewById(R.id.btnUpload);
        ImageButton btnRecord = findViewById(R.id.btnRecord);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        btnBack.setOnClickListener(v -> finish());

        loadUpdates();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getDisplayList()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = updatesList.get(position);
            if (item.contains("|uri=")) {
                String uriString = item.substring(item.indexOf("|uri=") + 5);
                openFile(Uri.parse(uriString));
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = infraInput.getText().toString().trim();
            if (!text.isEmpty()) {
                addUpdate("💬 " + text, null);
                infraInput.setText("");
            }
        });

        btnCamera.setOnClickListener(v -> openCamera());
        btnUpload.setOnClickListener(v -> openGallery());
        btnRecord.setOnClickListener(v -> startVoiceInput());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            try {
                File photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(this, "com.example.communityowl.file provider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(intent);
            } catch (IOException e) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        galleryLauncher.launch(intent);
    }

    private void openFile(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getContentResolver().getType(uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            voiceLauncher.launch(intent);
        }
    }

    private void addUpdate(String text, String uri) {
        String item = text + (uri != null ? "|uri=" + uri : "");
        updatesList.add(0, item);
        saveUpdates();
        refreshAdapter();
    }

    private void refreshAdapter() {
        adapter.clear();
        adapter.addAll(getDisplayList());
        adapter.notifyDataSetChanged();
    }

    private List<String> getDisplayList() {
        List<String> display = new ArrayList<>();
        for (String s : updatesList) {
            if (s.contains("|uri=")) {
                display.add(s.substring(0, s.indexOf("|uri=")));
            } else {
                display.add(s);
            }
        }
        return display;
    }

    private void saveUpdates() {
        String serialized = TextUtils.join(";;;", updatesList);
        prefs.edit().putString(KEY_INFRA_UPDATES, serialized).apply();
    }

    private void loadUpdates() {
        String serialized = prefs.getString(KEY_INFRA_UPDATES, null);
        if (serialized != null && !serialized.isEmpty()) {
            updatesList = new ArrayList<>(Arrays.asList(serialized.split(";;;")));
        } else {
            updatesList = new ArrayList<>(Arrays.asList(
                    "New Street Lights Installation - Park Avenue",
                    "Pothole Repair - 5th Cross Road",
                    "Park Renovation starting next Monday"
            ));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 100) {
                openCamera();
            } else if (requestCode == 101) {
                startVoiceInput();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}

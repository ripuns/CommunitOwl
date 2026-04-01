package com.example.communityowl;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfraActivity extends AppCompatActivity {

    private ArrayList<ReportItem> reportsList;
    private ArrayAdapter<ReportItem> adapter;
    private EditText infraInput;
    private Uri photoUri;

    // this is a simple container to hold the details of an infrastructure report
    static class ReportItem {
        long id;
        String description;
        String uri;

        // this creates a new report item with an id, text, and a link to a file if there is one
        ReportItem(long id, String description, String uri) {
            this.id = id;
            this.description = description;
            this.uri = uri;
        }

        // this returns the description of the report whenever we need to show it as text
        @Override
        public String toString() {
            return description;
        }
    }

    // this waits for the camera app to finish and then adds the photo to our list
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // if the photo was taken successfully, we add a record of it to our screen
                if (result.getResultCode() == RESULT_OK) {
                    addUpdate("📷 Photo captured", photoUri.toString());
                }
            }
    );

    // this waits for the user to pick a file from their gallery and saves it to our list
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // checking if the user actually picked a file
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            // this makes sure we still have permission to read the file even after the app restarts
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        addUpdate("📁 File uploaded: " + uri.getLastPathSegment(), uri.toString());
                    }
                }
            }
    );

    // this waits for the voice recognizer to finish and puts the spoken words into the text box
    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // if the speech was recognized, we grab the best guess of what was said
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && !results.isEmpty()) {
                        infraInput.setText(results.get(0));
                    }
                }
            }
    );

    // this sets up the screen, buttons, and the list of reports when the activity is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infra);

        // finding all the buttons and text boxes on the screen so we can use them
        ImageButton btnBack = findViewById(R.id.btnBack);
        ListView listView = findViewById(R.id.infraListView);
        infraInput = findViewById(R.id.infraInput);
        ImageButton btnSend = findViewById(R.id.btnInfraSend);
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnUpload = findViewById(R.id.btnUpload);
        ImageButton btnRecord = findViewById(R.id.btnRecord);
        View infraInputCard = findViewById(R.id.infraInputCard);

        btnBack.setOnClickListener(v -> finish());

        // check user role and hide messaging if resident
        checkUserRole(infraInputCard);

        reportsList = new ArrayList<>();
        // this adapter acts as a bridge between our list of data and the actual view on the screen
        adapter = new ArrayAdapter<ReportItem>(this, android.R.layout.simple_list_item_1, reportsList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                // making sure the text color matches our app's theme so it stays readable
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
                return view;
            }
        };
        listView.setAdapter(adapter);

        loadUpdatesFromProvider();

        // deciding what happens when a user clicks on an item in the list
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ReportItem item = reportsList.get(position);
            // if the item has a file link, we try to open it with the right app
            if (item.uri != null && !item.uri.isEmpty()) {
                openFile(Uri.parse(item.uri));
            } else {
                // otherwise, we open the screen that shows the full details of the report
                Intent intent = new Intent(this, ReportDetailActivity.class);
                intent.putExtra("report_id", item.id);
                startActivity(intent);
            }
        });

        // handling the send button click to add a new text update
        btnSend.setOnClickListener(v -> {
            String text = infraInput.getText().toString().trim();
            if (!text.isEmpty()) {
                addUpdate("💬 " + text, null);
                infraInput.setText("");
            }
        });

        // setting up the buttons for camera, file upload, and voice recording
        btnCamera.setOnClickListener(v -> openCamera());
        btnUpload.setOnClickListener(v -> openGallery());
        btnRecord.setOnClickListener(v -> startVoiceInput());
    }

    // this checks if the user is a resident or an admin and hides the input tools if they are just a resident
    private void checkUserRole(View inputCard) {
        // looking up the user's role in our settings database
        Cursor cursor = getContentResolver().query(UserProvider.CONTENT_URI_SETTINGS, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SETTING_ROLE));
                // residents can only view reports, so we hide the input section from them to keep it clean
                if ("Resident".equalsIgnoreCase(role)) {
                    inputCard.setVisibility(View.GONE);
                } else {
                    inputCard.setVisibility(View.VISIBLE);
                }
            }
            cursor.close();
        }
    }

    // this asks for camera permission and then opens the camera app to take a picture
    private void openCamera() {
        // checking if we already have permission to use the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            try {
                // creating a file to store the image and then starting the camera
                File photoFile = createImageFile();
                // getting a safe uri for the file using our fileprovider
                photoUri = FileProvider.getUriForFile(this, "com.example.communityowl.fileprovider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // telling the camera app where to save the picture
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(intent);
            } catch (IOException e) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // this creates a temporary file where the camera photo will be saved with a timestamped name
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // this creates a unique file name to avoid overwriting old photos
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    // this opens the file picker so the user can select a document or image from their gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // only showing files that can actually be opened
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // we use */* so the user can pick any type of file they want to upload
        intent.setType("*/*");
        galleryLauncher.launch(intent);
    }

    // this tries to open a file using whatever app on the phone can handle its file type
    private void openFile(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // letting the system figure out what kind of file it is so it can open the right app
            intent.setDataAndType(uri, getContentResolver().getType(uri));
            // giving the external app permission to read the file we are sharing from our app
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    // this starts the system's voice recognition so the user can speak their report description
    private void startVoiceInput() {
        // making sure we have permission to record audio first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            // setting up the speech recognizer intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            voiceLauncher.launch(intent);
        }
    }

    // this saves a new report update into the database and adds it to the list on the screen
    private void addUpdate(String text, String uri) {
        ContentValues values = new ContentValues();
        // filling in the report details
        values.put(DatabaseHelper.COLUMN_REPORT_DESC, text);
        values.put(DatabaseHelper.COLUMN_REPORT_RATING, 0.0f);
        values.put(DatabaseHelper.COLUMN_REPORT_STATUS, "Pending");
        
        // inserting the new report through our content provider so it's saved permanently
        Uri newUri = getContentResolver().insert(UserProvider.CONTENT_URI_REPORTS, values);
        if (newUri != null) {
            // grabbing the new id that was assigned by the database
            long id = ContentUris.parseId(newUri);
            // adding it to the top of our local list so it shows up immediately without refreshing
            reportsList.add(0, new ReportItem(id, text, uri));
            adapter.notifyDataSetChanged();
        }
    }

    // this fetches all the reports from the database and displays them in our list
    private void loadUpdatesFromProvider() {
        reportsList.clear();
        // sorting by id in descending order so the newest reports appear at the top
        Cursor cursor = getContentResolver().query(UserProvider.CONTENT_URI_REPORTS, null, null, null, DatabaseHelper.COLUMN_REPORT_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // pulling out the id and description for each report found
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_ID));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_DESC));
                reportsList.add(new ReportItem(id, desc, null));
            }
            cursor.close();
        }

        if (reportsList.isEmpty()) {
            addUpdate("New Street Lights Installation - Park Avenue", null);
            addUpdate("Pothole Repair - 5th Cross Road", null);
        }
        // telling the list to refresh itself and show the new items
        adapter.notifyDataSetChanged();
    }

    // this handles what happens after a user allows or denies a permission request like camera or audio
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // checking if the user actually clicked allow
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 100) openCamera();
            else if (requestCode == 101) startVoiceInput();
        }
    }
}

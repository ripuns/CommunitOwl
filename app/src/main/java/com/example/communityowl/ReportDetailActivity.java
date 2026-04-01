package com.example.communityowl;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.communityowl.R;

public class ReportDetailActivity extends AppCompatActivity {

    private TextView tvDescription;
    private RatingBar ratingBar;
    private Uri reportUri;

    // this sets up the screen when we click on a specific report to see its details
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        // setting up the top toolbar with a back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvDescription = findViewById(R.id.tvDescription);
        ratingBar = findViewById(R.id.ratingBar);

        long reportId = getIntent().getLongExtra("report_id", -1);
        if (reportId == -1) {
            Toast.makeText(this, "Error: Report not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // creating the specific database address for this one report
        reportUri = ContentUris.withAppendedId(UserProvider.CONTENT_URI_REPORTS, reportId);

        loadReportDetails();

        // business logic: update the database whenever the user changes the star rating
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // we only update if the user manually changed it, not if it was set by the code
            if (fromUser) {
                updateRating(rating);
            }
        });

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // this fetches the specific report's info from the database and displays it on the screen
    private void loadReportDetails() {
        // query our data provider for this specific report's info
        Cursor cursor = getContentResolver().query(reportUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_DESC));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_RATING));
            
            // showing the data in the text box and star bar
            tvDescription.setText(description);
            ratingBar.setRating(rating);
            cursor.close();
        }
    }

    // this updates the rating value in the database whenever the user slides the rating bar
    private void updateRating(float rating) {
        ContentValues values = new ContentValues();
        // preparing the new rating value to be saved
        values.put(DatabaseHelper.COLUMN_REPORT_RATING, rating);
        
        // sending the update to the database through the content resolver
        int rows = getContentResolver().update(reportUri, values, null, null);
        if (rows > 0) {
            Toast.makeText(this, "Rating updated", Toast.LENGTH_SHORT).show();
        }
    }

    // this creates the menu in the top corner of the screen with options like delete or share
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_detail_menu, menu);
        return true;
    }

    // this handles what happens when you click on one of the menu items like share, delete, or resolve
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // checking which menu button was clicked
        if (id == R.id.action_share) {
            shareReport();
            return true;
        } else if (id == R.id.action_delete) {
            deleteReport();
            return true;
        } else if (id == R.id.action_resolve) {
            markAsResolved();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // this opens up the phone's sharing menu so you can send the report details to other apps
    private void shareReport() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        // adding the report description
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Community Report: " + tvDescription.getText().toString());
        // letting the user pick which app they want to use for sharing
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // this removes the current report from the database and closes the screen
    private void deleteReport() {
        // sending the delete command to the database
        int rows = getContentResolver().delete(reportUri, null, null);
        if (rows > 0) {
            Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
            // closing the screen and going back since the report is now gone
            finish();
        }
    }

    // this updates the report's status to 'resolved' in the database
    private void markAsResolved() {
        ContentValues values = new ContentValues();
        // setting the status column to 'resolved'
        values.put(DatabaseHelper.COLUMN_REPORT_STATUS, "Resolved");
        getContentResolver().update(reportUri, values, null, null);
        Toast.makeText(this, "Report marked as resolved", Toast.LENGTH_SHORT).show();
    }
}

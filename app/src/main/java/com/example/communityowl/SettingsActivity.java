package com.example.communityowl;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgUserRole;
    private RadioButton rbResident, rbContractor, rbCityOfficial;
    private CheckBox cbNearbyUpdates, cbDarkMode;
    private MaterialButton btnSave;

    // this sets up the settings screen and connects all the switches and buttons when you open it
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rgUserRole = findViewById(R.id.rgUserRole);
        rbResident = findViewById(R.id.rbResident);
        rbContractor = findViewById(R.id.rbContractor);
        rbCityOfficial = findViewById(R.id.rbCityOfficial);
        cbNearbyUpdates = findViewById(R.id.cbNearbyUpdates);
        cbDarkMode = findViewById(R.id.cbDarkMode);
        btnSave = findViewById(R.id.btnSaveSettings);

        // initial state: query userprovider to load saved preferences
        loadSettings();

        btnSave.setOnClickListener(v -> saveSettings());

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // this pulls your saved choices from the database and sets the switches and buttons to match
    private void loadSettings() {
        // query userprovider for settings record (assuming single user/row for now)
        Cursor cursor = getContentResolver().query(UserProvider.CONTENT_URI_SETTINGS, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // set user role
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SETTING_ROLE));
            if ("Contractor".equals(role)) {
                rbContractor.setChecked(true);
            } else if ("City Official".equals(role)) {
                rbCityOfficial.setChecked(true);
            } else {
                rbResident.setChecked(true);
            }

            // set notification preferences (1 = true, 0 = false)
            int nearby = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SETTING_NEARBY));
            int dark = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SETTING_DARKMODE));

            cbNearbyUpdates.setChecked(nearby == 1);
            cbDarkMode.setChecked(dark == 1);

            cursor.close();
        }
    }

    // this takes all your current choices on the screen and saves them into the database so they are remembered next time
    private void saveSettings() {
        boolean isDarkMode = cbDarkMode.isChecked();
        ContentValues values = new ContentValues();
        
        // get selected role
        String role = "Resident";
        int checkedId = rgUserRole.getCheckedRadioButtonId();
        if (checkedId == R.id.rbContractor) role = "Contractor";
        else if (checkedId == R.id.rbCityOfficial) role = "City Official";
        
        values.put(DatabaseHelper.COLUMN_SETTING_ROLE, role);
        values.put(DatabaseHelper.COLUMN_SETTING_NEARBY, cbNearbyUpdates.isChecked() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_SETTING_DARKMODE, isDarkMode ? 1 : 0);

        // save into sqlite via userprovider update
        int rows = getContentResolver().update(UserProvider.CONTENT_URI_SETTINGS, values, null, null);
        if (rows <= 0) {
            // if no row existed, insert instead
            getContentResolver().insert(UserProvider.CONTENT_URI_SETTINGS, values);
        }

        // apply theme immediately
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    // this handles what happens when you click the back arrow in the top corner
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

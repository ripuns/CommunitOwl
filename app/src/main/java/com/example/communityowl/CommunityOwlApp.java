package com.example.communityowl;

import android.app.Application;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatDelegate;

public class CommunityOwlApp extends Application {

    // this is the starting point of our app, it runs when the app first launches
    @Override
    public void onCreate() {
        super.onCreate();
        applyTheme();
    }

    // this checks our saved settings to see if the user wants dark mode or light mode and applies it
    public void applyTheme() {
        Cursor cursor = getContentResolver().query(UserProvider.CONTENT_URI_SETTINGS, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int dark = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SETTING_DARKMODE));
            if (dark == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            cursor.close();
        } else {
            // default to follow system or light if no settings found
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}

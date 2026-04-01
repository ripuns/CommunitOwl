package com.example.communityowl;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserProvider extends ContentProvider {
    // this is the unique name that identifies our data provider to the rest of the android system
    public static final String AUTHORITY = "com.example.communityowl.provider";
    public static final Uri CONTENT_URI_REPORTS = Uri.parse("content://" + AUTHORITY + "/reports");
    public static final Uri CONTENT_URI_SETTINGS = Uri.parse("content://" + AUTHORITY + "/settings");
    private static final int REPORTS = 1;
    private static final int REPORT_ID = 2;
    private static final int SETTINGS = 3;

    // this tool helps us match the incoming uri (address) to one of our data codes
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // we register the different types of data paths our provider can handle
        uriMatcher.addURI(AUTHORITY, "reports", REPORTS);
        uriMatcher.addURI(AUTHORITY, "reports/#", REPORT_ID);
        uriMatcher.addURI(AUTHORITY, "settings", SETTINGS);
    }

    private DatabaseHelper dbHelper;

    // this runs when the provider is first created and sets up our database helper
    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    // this handles requests to fetch data from our tables like reports or settings
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        // checking which data the caller is asking for based on the uri
        switch (uriMatcher.match(uri)) {
            case REPORTS:
                // fetching all reports from the reports table
                cursor = db.query(DatabaseHelper.TABLE_REPORTS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REPORT_ID:
                // fetching a single report by its specific id number
                selection = DatabaseHelper.COLUMN_REPORT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DatabaseHelper.TABLE_REPORTS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SETTINGS:
                // fetching settings like dark mode or user role
                cursor = db.query(DatabaseHelper.TABLE_SETTINGS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // telling the system to watch this uri for any future data changes so screens can refresh automatically
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    // this lets us add new information into our database tables
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // getting access to the database so we can write new data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (uriMatcher.match(uri)) {
            case REPORTS:
                id = db.insert(DatabaseHelper.TABLE_REPORTS, null, values);
                break;
            case SETTINGS:
                id = db.insert(DatabaseHelper.TABLE_SETTINGS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // letting other parts of the app know that new data has been added
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    // this allows us to remove specific records from the database using their id
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriMatcher.match(uri)) {
            case REPORT_ID:
                // finding the specific report id from the uri and deleting it
                selection = DatabaseHelper.COLUMN_REPORT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(DatabaseHelper.TABLE_REPORTS, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // if something was actually deleted, we tell the system to refresh any related screens
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // this updates existing records in our database with new information
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriMatcher.match(uri)) {
            case REPORT_ID:
                // updating a specific report's info using its id
                selection = DatabaseHelper.COLUMN_REPORT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = db.update(DatabaseHelper.TABLE_REPORTS, values, selection, selectionArgs);
                break;
            case SETTINGS:
                // updating the general app settings
                rowsUpdated = db.update(DatabaseHelper.TABLE_SETTINGS, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // if anything was changed, we trigger a notification so the UI can update
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}

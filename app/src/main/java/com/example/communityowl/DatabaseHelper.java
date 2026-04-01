package com.example.communityowl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    // we give our database a name and a version number so the phone knows how to handle updates
    private static final String DATABASE_NAME = "CommunityOwl.db";
    private static final int DATABASE_VERSION = 3;

    // names for the users table and its columns
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // names for the chat messages table
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MSG_ID = "id";
    public static final String COLUMN_MSG_SENDER = "sender";
    public static final String COLUMN_MSG_CONTENT = "content";

    // names for the security monitoring logs
    public static final String TABLE_MONITORING = "monitoring";
    public static final String COLUMN_MON_ID = "id";
    public static final String COLUMN_MON_TIMESTAMP = "timestamp";
    public static final String COLUMN_MON_EVENT = "event";

    // names for the infrastructure reports table
    public static final String TABLE_REPORTS = "reports";
    public static final String COLUMN_REPORT_ID = "id";
    public static final String COLUMN_REPORT_DESC = "description";
    public static final String COLUMN_REPORT_RATING = "rating";
    public static final String COLUMN_REPORT_STATUS = "status";

    // names for the app settings table
    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_SETTING_ID = "id";
    public static final String COLUMN_SETTING_ROLE = "user_role";
    public static final String COLUMN_SETTING_NEARBY = "nearby_updates";
    public static final String  COLUMN_SETTING_DARKMODE = "dark_mode";

    // this sets up the database helper with our database name and version
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // this creates all the tables we need for users, messages, monitoring, reports, and settings when the database is first made
    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating the users table to store login details
        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")");

        // creating the messages table for the community chat
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MSG_SENDER + " TEXT,"
                + COLUMN_MSG_CONTENT + " TEXT" + ")");

        // creating the monitoring table to log security events
        db.execSQL("CREATE TABLE " + TABLE_MONITORING + "("
                + COLUMN_MON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MON_TIMESTAMP + " TEXT,"
                + COLUMN_MON_EVENT + " TEXT" + ")");

        // creating the reports table for infrastructure issues like potholes
        db.execSQL("CREATE TABLE " + TABLE_REPORTS + "("
                + COLUMN_REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REPORT_DESC + " TEXT,"
                + COLUMN_REPORT_RATING + " REAL,"
                + COLUMN_REPORT_STATUS + " TEXT" + ")");

        // creating the settings table for user preferences like dark mode
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "("
                + COLUMN_SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SETTING_ROLE + " TEXT,"
                + COLUMN_SETTING_NEARBY + " INTEGER,"
                + COLUMN_SETTING_DARKMODE + " INTEGER" + ")");

        Log.d("DatabaseHelper", "Tables created");
    }

    // this handles updating the database structure if we change it in a newer version of the app
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // if the old version was very basic, we add the monitoring feature now
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + TABLE_MONITORING + "("
                    + COLUMN_MON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MON_TIMESTAMP + " TEXT,"
                    + COLUMN_MON_EVENT + " TEXT" + ")");
        }
        // for version 3, we added reports and settings tables
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE " + TABLE_REPORTS + "("
                    + COLUMN_REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_REPORT_DESC + " TEXT,"
                    + COLUMN_REPORT_RATING + " REAL,"
                    + COLUMN_REPORT_STATUS + " TEXT" + ")");

            db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "("
                    + COLUMN_SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SETTING_ROLE + " TEXT,"
                    + COLUMN_SETTING_NEARBY + " INTEGER,"
                    + COLUMN_SETTING_DARKMODE + " INTEGER" + ")");

            // we insert some basic default settings so the app doesn't crash on first run
            // user's default role: 'resident' with notifications on
            ContentValues values = new ContentValues();
            values.put(COLUMN_SETTING_ROLE, "Resident");
            values.put(COLUMN_SETTING_NEARBY, 1);
            values.put(COLUMN_SETTING_DARKMODE, 0);
            db.insert(TABLE_SETTINGS, null, values);
        }
    }

    // this adds a new user to our database with their email and password
    public void addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // we bundle the email and password together to save them in one go
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        Log.d("DatabaseHelper", "User insert result: " + result);
    }

    // this checks if a user exists in our database with the matching email and password
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        // we look for a row where both the email and password match exactly and the '?' are placeholders
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        // if we found at least one match, the login is valid and we return true
        return count > 0;
    }

    // this saves a new message into the chat history table
    public void addMessage(String sender, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MSG_SENDER, sender);
        values.put(COLUMN_MSG_CONTENT, content);
        db.insert(TABLE_MESSAGES, null, values);
    }

    // this records a monitoring event like when security check starts
    public void addMonitoringEvent(String timestamp, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MON_TIMESTAMP, timestamp);
        values.put(COLUMN_MON_EVENT, event);
        db.insert(TABLE_MONITORING, null, values);
    }

    // this gets all the messages we've stored and builds them into a single string to show in the chat
    public String getAllMessages() {
        StringBuilder history = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();
        // we grab every single row from the messages table
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);

        if (cursor.moveToFirst()) {
            do {
                // we pull the sender and message text out of each row as we go through them
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_SENDER));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MSG_CONTENT));
                history.append(sender).append(": ").append(content).append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return history.toString();
    }
}

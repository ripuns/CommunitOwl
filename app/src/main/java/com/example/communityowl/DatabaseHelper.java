package com.example.communityowl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CommunityOwl.db";
    private static final int DATABASE_VERSION = 2; // Incremented version to add new table

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    // Messages Table
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MSG_ID = "id";
    private static final String COLUMN_MSG_SENDER = "sender";
    private static final String COLUMN_MSG_CONTENT = "content";

    // Monitoring Table
    private static final String TABLE_MONITORING = "monitoring";
    private static final String COLUMN_MON_ID = "id";
    private static final String COLUMN_MON_TIMESTAMP = "timestamp";
    private static final String COLUMN_MON_EVENT = "event";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MSG_SENDER + " TEXT,"
                + COLUMN_MSG_CONTENT + " TEXT" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        String CREATE_MONITORING_TABLE = "CREATE TABLE " + TABLE_MONITORING + "("
                + COLUMN_MON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MON_TIMESTAMP + " TEXT,"
                + COLUMN_MON_EVENT + " TEXT" + ")";
        db.execSQL(CREATE_MONITORING_TABLE);

        Log.d("DatabaseHelper", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_MONITORING_TABLE = "CREATE TABLE " + TABLE_MONITORING + "("
                    + COLUMN_MON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MON_TIMESTAMP + " TEXT,"
                    + COLUMN_MON_EVENT + " TEXT" + ")";
            db.execSQL(CREATE_MONITORING_TABLE);
            Log.d("DatabaseHelper", "Monitoring table added in upgrade");
        }
    }

    public void addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        Log.d("DatabaseHelper", "User insert result: " + result);
    }

    public void addMessage(String sender, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MSG_SENDER, sender);
        values.put(COLUMN_MSG_CONTENT, content);
        long result = db.insert(TABLE_MESSAGES, null, values);
        Log.d("DatabaseHelper", "Message insert result: " + result);
    }

    public void addMonitoringEvent(String timestamp, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MON_TIMESTAMP, timestamp);
        values.put(COLUMN_MON_EVENT, event);
        long result = db.insert(TABLE_MONITORING, null, values);
        Log.d("DatabaseHelper", "Monitoring event insert result: " + result);
    }

    public String getAllMessages() {
        StringBuilder history = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);

        if (cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_SENDER));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_CONTENT));
                history.append(sender).append(": ").append(content).append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d("DatabaseHelper", "Retrieved messages history length: " + history.length());
        return history.toString();
    }
}


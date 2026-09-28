package com.example.numberguess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASENAME = "result.db";
    private static final String TABLE_RECORD = "tblresult";
    private static final int DATABASEVERSION = 2; // Incremented to trigger onUpgrade for password column
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SCORE = "score";

    private static final String[] allColumns = {COLUMN_ID, COLUMN_NAME, COLUMN_PASSWORD, COLUMN_SCORE};

    private static final String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " +
            TABLE_RECORD + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NAME + " TEXT," +
            COLUMN_PASSWORD + " TEXT," +
            COLUMN_SCORE + " INTEGER );";

    private SQLiteDatabase database;

    public DBHelper(@Nullable Context context) {
        super(context, DATABASENAME, null, DATABASEVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
        onCreate(sqLiteDatabase);
    }

    /**
     * Checks if a user with the matching username and password exists in the database.
     */
    public boolean checkUser(String username, String password) {
        database = getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_RECORD,
                allColumns,
                COLUMN_NAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        database.close();
        return exists;
    }

    /**
     * Registers a new user with a username and password if the username isn't already taken.
     */
    public boolean registerUser(String username, String password) {
        if (!genericSelectByUserName(username).isEmpty()) {
            return false; // Username already exists
        }

        database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_SCORE, 0);

        long id = database.insert(TABLE_RECORD, null, values);
        database.close();
        return id != -1;
    }

    public ModelUser insert(ModelUser user) {
        database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, user.getUserName());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_SCORE, user.getScore());
        long id = database.insert(TABLE_RECORD, null, values);
        user.setId(id);
        database.close();
        return user;
    }

    public void deleteById(long id) {
        database = getWritableDatabase();
        database.delete(TABLE_RECORD, COLUMN_ID + " = " + id, null);
        database.close();
    }

    public void update(ModelUser user) {
        database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_NAME, user.getUserName());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_SCORE, user.getScore());
        database.update(TABLE_RECORD, values, COLUMN_ID + "=" + user.getId(), null);
        database.close();
    }

    public ArrayList<ModelUser> selectAll() {
        database = getReadableDatabase();
        ArrayList<ModelUser> users = new ArrayList<>();
        String sortOrder = COLUMN_SCORE + " DESC";
        Cursor cursor = database.query(TABLE_RECORD, allColumns, null, null, null, null, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));

                ModelUser user = new ModelUser(name, password, score, id);
                users.add(user);
            }
        }
        cursor.close();
        database.close();
        return users;
    }

    public ArrayList<ModelUser> genericSelectByUserName(String userName) {
        String[] vals = { userName };
        return select(COLUMN_NAME, vals);
    }

    public ArrayList<ModelUser> select(String column, String[] values) {
        database = getReadableDatabase();
        ArrayList<ModelUser> users = new ArrayList<>();
        Cursor cursor = database.query(TABLE_RECORD, allColumns, column + " = ?", values, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));

                ModelUser user = new ModelUser(name, password, score, id);
                users.add(user);
            }
        }
        cursor.close();
        database.close();
        return users;
    }
}
package com.turco_michael_weight_tracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.turco_michael_weight_tracking.data.LoginDataSource;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDatabase_OLD extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int VERSION = 1;

    public static String currentUsername;
    public static Long currentUserID;

    public UserDatabase_OLD(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class UsersTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
    }

    // Class for each user's weight data in their table
    private static final class UserDataTable {
        private static final String COL_ID = "_id";
        private static final String COL_DATE = "date";
        private static final String COL_VALUE = "weight";

        public static String getTableName(long userId) {
            return "user_data_" + userId;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + UsersTable.TABLE + " (" +
                UsersTable.COL_ID + " integer primary key autoincrement, " +
                UsersTable.COL_USERNAME + " text unique, " +
                UsersTable.COL_PASSWORD + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UsersTable.TABLE);
        onCreate(db);
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_USERNAME, username);
        values.put(UsersTable.COL_PASSWORD, password);

        long userId = db.insert(UsersTable.TABLE, null, values);
        // returns -1 if failed (duplicate name)

        if (userId != -1) {
            createUserDataTable(db, userId);
        }
        return userId;
    }

    private void createUserDataTable(SQLiteDatabase db, long userId) {
        db.execSQL("create table " + UserDataTable.getTableName(userId) + " (" +
                UserDataTable.COL_ID + " integer primary key autoincrement, " +
                UserDataTable.COL_DATE + " text, " +
                UserDataTable.COL_VALUE + " float)");
    }

    public long authenticateUser(String username, String password)
            throws LoginDataSource.UsernameUnknownException, LoginDataSource.PasswordIncorrectException {

        SQLiteDatabase db = getReadableDatabase();

        // check if username exists
        String usernameQuery = "select " + UsersTable.COL_ID + " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_USERNAME + " = ?";
        Cursor usernameCursor = db.rawQuery(usernameQuery, new String[]{username});

        if (!usernameCursor.moveToFirst()) {
            usernameCursor.close();
            throw new LoginDataSource.UsernameUnknownException();
        }
        usernameCursor.close();


        // check if password is correct
        String passwordQuery = "select " + UsersTable.COL_ID + " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_USERNAME + " = ? and " +
                UsersTable.COL_PASSWORD + " = ?";
        Cursor passwordCursor = db.rawQuery(passwordQuery, new String[]{username, password});

        if (!passwordCursor.moveToFirst()) {
            passwordCursor.close();
            throw new LoginDataSource.PasswordIncorrectException();
        }


        // username & password is correct
        long userId = passwordCursor.getLong(0);
        passwordCursor.close();

        // store user values to use later
        setCurrentUser(username, userId);

        return userId;
    }

    public void setCurrentUser(String username, long userID) {
        currentUsername = username;
        currentUserID = userID;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + UsersTable.COL_ID + " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{username});

        boolean isTaken = cursor.moveToFirst(); // true if username exists

        cursor.close();
        return isTaken;
    }


    // WeightEntry functions. Adding / Getting / Removing data
    public long addWeightEntry(long userId, Date date, float weight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserDataTable.COL_DATE, dateToString(date));
        values.put(UserDataTable.COL_VALUE, weight);

        return db.insert(UserDataTable.getTableName(userId), null, values);
    }

    public List<WeightEntry> getWeightEntries(long userId) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                UserDataTable.getTableName(userId),
                new String[]{UserDataTable.COL_ID, UserDataTable.COL_DATE, UserDataTable.COL_VALUE},
                null, null, null, null, UserDataTable.COL_DATE + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(1);
                Date entryDate = stringToDate(dateString);

                entries.add(new WeightEntry(cursor.getLong(0), entryDate, cursor.getFloat(2)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return entries;
    }

    public float getMostRecentWeight(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        float returnWeight = LocalStorage.UNKNOWN;

        // only select first sql result
        // https://stackoverflow.com/questions/5547440/is-there-a-way-to-fetch-the-first-entry-of-a-table-in-database-using-sql-query
        // query help from stackoverflow
        String query = String.format("select %s from %s order by %s desc limit 1",
                UserDataTable.COL_VALUE,
                UserDataTable.getTableName(userId),
                UserDataTable.COL_DATE);

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            returnWeight = cursor.getFloat(0);
        }

        cursor.close();
        return returnWeight;
    }

    public boolean deleteWeightEntry(long userId, long entryId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                UserDataTable.getTableName(userId),
                UserDataTable.COL_ID + " = ?",
                new String[]{String.valueOf(entryId)}
        ) > 0;
    }


    // Date conversion helpers
    // Got help from https://jenkov.com/tutorials/java-internationalization/simpledateformat.html
    private static String dateToString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
    }

    private static Date stringToDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString);
        } catch (Exception e) {
            return new Date(); // error
        }
    }
}
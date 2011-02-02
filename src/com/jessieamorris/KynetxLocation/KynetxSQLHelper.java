package com.jessieamorris.KynetxLocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KynetxSQLHelper {
    public static final String KEY_APPID = "appid";
    public static final String KEY_VERSION = "version";
    public static final String KEY_ROWID = "_id";

    public static final String KEY_TEXT = "text";
    public static final String KEY_ACTION = "action";
    public static final String KEY_TITLE = "title";

    
    private static final String TAG = "KynetxSQLHelper";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "Kynetx";
    private static final String APPS_TABLE = "apps";
    private static final String NOTIFICATIONS_TABLE = "notifications";
    private static final int DATABASE_VERSION = 10;

    /**
     * Database creation sql statement
     */
    private static final String APPS_CREATE =
        "create table " + APPS_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_APPID + " text not null, " + KEY_VERSION + " text not null);";
    private static final String NOTIFICATIONS_CREATE =
        "create table " + NOTIFICATIONS_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_TEXT + " text not null, " + KEY_ACTION + " text, " + KEY_TITLE + ");";
    
    private static final String INSERT_DEFAULT = "INSERT INTO " + APPS_TABLE + " VALUES (0,\"Select an app\",\"none\");";

    
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(APPS_CREATE);
            db.execSQL(NOTIFICATIONS_CREATE);
            db.execSQL(INSERT_DEFAULT);
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + APPS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATIONS_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public KynetxSQLHelper(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public KynetxSQLHelper open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    
    public long createApp(String appid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_APPID, appid);
        initialValues.put(KEY_VERSION, "dev");

        return mDb.insert(APPS_TABLE, null, initialValues);
    }
    
    public long createApp(String appid, String version) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_APPID, appid);
        initialValues.put(KEY_VERSION, version);

        return mDb.insert(APPS_TABLE, null, initialValues);
    }    

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteApp(long rowId) {
    	if(rowId != 0){
    		return mDb.delete(APPS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    	}
    	return false;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllApps() {

        return mDb.query(APPS_TABLE, new String[] {KEY_ROWID, KEY_APPID,
                KEY_VERSION}, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchApp(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, APPS_TABLE, new String[] {KEY_ROWID,
                    KEY_APPID, KEY_VERSION}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateApp(long rowId, String appid, String version) {
        ContentValues args = new ContentValues();
        args.put(KEY_APPID, appid);
        args.put(KEY_VERSION, version);

        return mDb.update(APPS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    
    
    // Start of the notification stuff
    public long createNotification(String title, String text, String url) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_TEXT, text);
        initialValues.put(KEY_ACTION, url);

        return mDb.insert(NOTIFICATIONS_TABLE, null, initialValues);
    }


    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNotification(long rowId) {
    	if(rowId != 0){
    		return mDb.delete(NOTIFICATIONS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    	}
    	return false;
    }

    public boolean deleteAllNotifications() {
    	return mDb.delete(NOTIFICATIONS_TABLE, null, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotifications() {

        return mDb.query(NOTIFICATIONS_TABLE, new String[] {KEY_ROWID,
        		KEY_TITLE, KEY_TEXT, KEY_ACTION}, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNotification(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, NOTIFICATIONS_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_TEXT, KEY_ACTION}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    
    public boolean updateNotification(long rowId, String title, String text, String action) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_TEXT, text);
        args.put(KEY_ACTION, action);

        return mDb.update(NOTIFICATIONS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
}

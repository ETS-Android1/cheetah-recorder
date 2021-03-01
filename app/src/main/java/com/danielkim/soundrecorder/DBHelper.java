package com.danielkim.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;

/**
 * Created by Daniel on 12/29/2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String LOG_TAG = "DBHelper";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME = "saved_recordings";
        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBHelperItem.TABLE_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";




    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                DBHelperItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };
        db.delete(DBHelperItem.TABLE_NAME, "_ID=?", whereArgs);
    }

    public void moveToDeletedFiles(int id) {

        // variables
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { "_id = " + id };
        String fileName = getItemAt(id).getName();
        String filePath = getItemAt(id).getFilePath();
        String newFilePath = Environment.getExternalStorageDirectory() + "/SoundRecorder/deleted/" + fileName;
        ContentValues cv = new ContentValues();


        // assign
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, newFilePath);

        // move the file to the deleted folder
        moveFile(filePath, newFilePath);

        db.update(DBHelperItem.TABLE_NAME, cv, null, whereArgs);
    }


    public void restoreDeletedFiles() {
        SQLiteDatabase db = getReadableDatabase();


        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, "/SoundRecorder");
        db.update(DBHelperItem.TABLE_NAME, cv, DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + "='/SoundRecorder/deleted/'", null);



        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }
    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItem> {
        public int compare(RecordingItem item1, RecordingItem item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void renameItem(RecordingItem item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        db.update(DBHelperItem.TABLE_NAME, cv,
                DBHelperItem._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, item.getName());
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(DBHelperItem._ID, item.getId());
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }

    private void moveFile(String inputPath, String outputPath) {

        // variables
        File file;
        InputStream inputStream;
        OutputStream outputStream;

        byte[] buffer;


        // assign
        inputStream = null;
        outputStream = null;
        buffer = new byte[1024];

        // attempt file operations
        try {

            //create output directory if it doesn't exist
            file = new File (outputPath);
            if (!file.exists())
                file.mkdirs();


            inputStream = new FileInputStream(inputPath);
            outputStream = new FileOutputStream(outputPath);


            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();

            // write the output file
            outputStream.flush();
            outputStream.close();

            // delete the original file
            new File(inputPath).delete();
        }
        catch (FileNotFoundException e) {
            Log.e("tag: ", e.getMessage());
        }
        catch (Exception e) {
            Log.e("tag: ", e.getMessage());
        }
    }
}

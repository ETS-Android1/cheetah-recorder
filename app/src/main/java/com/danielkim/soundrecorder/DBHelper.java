package com.danielkim.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.DropBoxManager;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.danielkim.soundrecorder.adapters.TagItem;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by Daniel on 12/29/2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    // private variables
    private static final String LOG_TAG = "DBHelper";
    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    private static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String DELETED = DBHelper.DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + " not like '%/deleted/%'";
    public static final String NOT_DELETED = DBHelper.DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + " like '%/deleted/%'";

    // public variables
    public static final String DATABASE_NAME = "saved_recordings.db";


    // set up the saved recordings table
    public static abstract class DBHelperItem implements BaseColumns {

        // saved recording table
        public static final String SAVED_RECORDINGS_NAME = "saved_recordings";
        public static final String SAVED_RECORDING_RECORDING_NAME = "recording_name";
        public static final String SAVED_RECORDING_RECORDING_FILE_PATH = "file_path";
        public static final String SAVED_RECORDING_RECORDING_LENGTH = "length";
        public static final String SAVED_RECORDING_TIME_ADDED = "time_added";
        public static final String SAVED_RECORDING_RECORDING_SIZE ="file_size";
        public static final String SAVED_RECORDING_TAG ="tag";
        public static final String SAVED_RECORDING_TAG_COLOUR="colour";
        public static final String SAVED_RECORDING_TEXT_COLOR= "text_color";
        public static final String SAVED_RECORDING_URL = "url";
        public static final String SAVED_RECORDING_CLOUD = "is_cloud";


        // tag system table
        public static final String TAG_SYSTEM_NAME = "tag_system";
        public static final String TAG_SYSTEM_TAG_NAME = "tag_name";
        public static final String TAG_SYSTEM_TAG_COLOR = "tag_color";
        public static final String TAG_SYSTEM_TEXT_COLOUR = "tag_text_colour";

        // cloud upload table
        public static final String UPLOAD_SYSTEM_NAME = "uploads";
        public static final String UPLOAD_URL = "url";
        public static final String UPLOAD_FILE_NAME = "file_name";
    }

    // create databases
    private static final String SQL_CREATE_SAVED_RECORDINGS_TABLE =
            "CREATE TABLE " + DBHelperItem.SAVED_RECORDINGS_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_TIME_ADDED + " INTEGER " + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_RECORDING_SIZE+ " DOUBLE " + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_TAG + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_TAG_COLOUR + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_TEXT_COLOR + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_URL + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.SAVED_RECORDING_CLOUD + " INTEGER " + ")";


    private static final String SQL_CREATE_TAG_SYSTEM_TABLE =
            "CREATE TABLE " + DBHelperItem.TAG_SYSTEM_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.TAG_SYSTEM_TAG_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.TAG_SYSTEM_TAG_COLOR + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.TAG_SYSTEM_TEXT_COLOUR + TEXT_TYPE + ")";

    private static final String SQL_CREATE_UPLOAD_TABLE =
            "CREATE TABLE " + DBHelperItem.UPLOAD_SYSTEM_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.UPLOAD_URL + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.UPLOAD_FILE_NAME + TEXT_TYPE + ")";


    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.SAVED_RECORDINGS_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {

        // create our tables
        db.execSQL(SQL_CREATE_SAVED_RECORDINGS_TABLE);
        db.execSQL(SQL_CREATE_TAG_SYSTEM_TABLE);
        db.execSQL(SQL_CREATE_UPLOAD_TABLE);


        // variables
        ContentValues cvNone;
        ContentValues cvWork;
        ContentValues cvSchool;
        ContentValues cvGrocery;
        ContentValues cvMemo;


        // default tag entries
        cvNone = new ContentValues();
        cvWork = new ContentValues();
        cvSchool = new ContentValues();
        cvGrocery = new ContentValues();
        cvMemo = new ContentValues();


        // create tag values
        cvNone.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, "No Tag");
        cvNone.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, "#EEEEEE");
        cvNone.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, "#FFFFFF");

        cvWork.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, "Work");
        cvWork.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, "#F2C446");
        cvWork.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, "#FFFFFF");

        cvSchool.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, "School");
        cvSchool.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, "#81DE59");
        cvSchool.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, "#FFFFFF");

        cvGrocery.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, "Grocery");
        cvGrocery.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, "#C07FE3");
        cvGrocery.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, "#FFFFFF");

        cvMemo.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, "Memo");
        cvMemo.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, "#8AADFF");
        cvMemo.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, "#FFFFFF");


        // add tags to the system
        db.insert(DBHelperItem.TAG_SYSTEM_NAME, null, cvNone);
        db.insert(DBHelperItem.TAG_SYSTEM_NAME, null, cvWork);
        db.insert(DBHelperItem.TAG_SYSTEM_NAME, null, cvSchool);
        db.insert(DBHelperItem.TAG_SYSTEM_NAME, null, cvGrocery);
        db.insert(DBHelperItem.TAG_SYSTEM_NAME, null, cvMemo);
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

    @Deprecated
    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_NAME,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
                DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH,
                DBHelperItem.SAVED_RECORDING_TIME_ADDED,
                DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,
                DBHelperItem.SAVED_RECORDING_TAG,
                DBHelperItem.SAVED_RECORDING_TAG_COLOUR,
                DBHelperItem.SAVED_RECORDING_TEXT_COLOR,
                DBHelperItem.SAVED_RECORDING_URL,
                DBHelperItem.SAVED_RECORDING_CLOUD
        };
        Cursor c = db.query(DBHelperItem.SAVED_RECORDINGS_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TIME_ADDED)));
            item.setSize(c.getDouble(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_SIZE)));
            item.setTag(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TAG)));
            item.setColour(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TAG_COLOUR)));
            item.setTextColor(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TEXT_COLOR)));
            item.setUrl(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_URL)));
            item.setIsCloud(c.getInt(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_CLOUD)));

            c.close();
            return item;
        }
        return null;
    }

    public RecordingItem getItemByFilePath(String filePath) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_NAME,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
                DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH,
                DBHelperItem.SAVED_RECORDING_TIME_ADDED,
                DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,
                DBHelperItem.SAVED_RECORDING_TAG,
                DBHelperItem.SAVED_RECORDING_TAG_COLOUR,
                DBHelperItem.SAVED_RECORDING_TEXT_COLOR,
                DBHelperItem.SAVED_RECORDING_URL,
                DBHelperItem.SAVED_RECORDING_CLOUD
        };

         Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + " like '%" + filePath + "%'",
                null,
                null,
                null,
                null);

        c.moveToFirst();

        if(!c.isAfterLast()) {
            if (!c.isNull(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH))) {
                RecordingItem item = new RecordingItem();
                item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
                item.setName(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_NAME)));
                item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH)));
                item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH)));
                item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TIME_ADDED)));
                item.setSize(c.getDouble(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_SIZE)));
                item.setTag(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TAG)));
                item.setColour(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TAG_COLOUR)));
                item.setTextColor(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_TEXT_COLOR)));
                item.setUrl(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_URL)));
                item.setIsCloud(c.getInt(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_CLOUD)));

                c.close();
                return item;
            }
        }
        return null;
    }

    public void addCloudUpload(String fileName, String url){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.UPLOAD_FILE_NAME, fileName);
        //cv.put(DBHelperItem.SAVED_RECORDING_TIME_ADDED, System.currentTimeMillis());
        cv.put(DBHelperItem.UPLOAD_URL, url);
        long rowId = db.insert(DBHelperItem.UPLOAD_SYSTEM_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
    }

    public LinkedList<RecordingItem> getCloudUploads() {

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.UPLOAD_FILE_NAME,
                DBHelperItem.UPLOAD_URL,
        };

        Cursor c = db.query(
                DBHelperItem.UPLOAD_SYSTEM_NAME,
                projection,
                null ,
                null,
                null,
                null,
                null);

        // variables
        LinkedList<RecordingItem> items;
        items = new LinkedList<RecordingItem>();
        c.moveToFirst();
        while(!c.isAfterLast()){

            // variables
            RecordingItem item;


            // build our limited item
            item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(DBHelperItem.UPLOAD_FILE_NAME)));
            item.setUrl(c.getString(c.getColumnIndex(DBHelperItem.UPLOAD_URL)));

            items.add(item);
            c.moveToNext();
        }

        c.close();

        return items;
    }

    public LinkedList<String> getFilePaths(){

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        LinkedList<String> filePaths;
        filePaths = new LinkedList<String>();
        c.moveToFirst();
        while(!c.isAfterLast()){

            // variables
            String currentPath;

            currentPath = c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH));


            filePaths.add(currentPath);
            c.moveToNext();
        }
        c.close();

        return filePaths;
    }

    public LinkedList<String> getFilePaths(String clause){

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                clause,
                null,
                null,
                null,
                null);

        LinkedList<String> filePaths;
        filePaths = new LinkedList<String>();
        c.moveToFirst();

        while(!c.isAfterLast()){

            // variables
            String currentPath;

            currentPath = c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH));


            filePaths.add(currentPath);
            c.moveToNext();
        }
        c.close();

        return filePaths;
    }
    public LinkedList<String> getFilePathsByTag(String[] whereArgs){

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                DBHelperItem.SAVED_RECORDING_TAG + " = ? AND "+DBHelperItem.SAVED_RECORDING_TAG_COLOUR+ " = ?",
                whereArgs,
                null,
                null,
                null);

        LinkedList<String> filePaths;
        filePaths = new LinkedList<String>();
        c.moveToFirst();

        while(!c.isAfterLast()){

            // variables
            String currentPath;

            currentPath = c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH));


            filePaths.add(currentPath);
            c.moveToNext();
        }
        c.close();

        return filePaths;
    }
    public void removeTagWWithName(String[] whereArgs)
    {
        SQLiteDatabase db = getWritableDatabase();
        String[] projection = {DBHelperItem._ID,
                DBHelperItem.TAG_SYSTEM_TAG_COLOR,
                DBHelperItem.TAG_SYSTEM_TAG_NAME
        };

        int i = 0;
        ContentValues cv = new ContentValues();

        db.delete(DBHelperItem.TAG_SYSTEM_NAME,"tag_name=? AND "+ DBHelperItem.TAG_SYSTEM_TAG_COLOR+" =?",whereArgs);
        Cursor c = db.query(
                DBHelperItem.TAG_SYSTEM_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            cv.put(DBHelperItem._ID, i);
            i++;
            System.out.println(i);
            c.moveToNext();

        }
        c.close();
    }
    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };

        db.delete(DBHelperItem.SAVED_RECORDINGS_NAME, "_ID=?", whereArgs);
    }
    public int getTagCount(){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.TAG_SYSTEM_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.SAVED_RECORDINGS_NAME, projection, null, null, null, null, null);
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
    public long addTag(String tagName, String colour, String textColour)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.TAG_SYSTEM_TAG_NAME, tagName);
        cv.put(DBHelperItem.TAG_SYSTEM_TAG_COLOR, colour);
        cv.put(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR, textColour);
        long rowId = db.insert(DBHelperItem.TAG_SYSTEM_NAME,null,cv);
        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }
    public long addRecording(String recordingName, String filePath, long length, double fileSize, String tag, String tagColour, String textColor, String url, int cloud) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH, filePath);
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH, length);
        cv.put(DBHelperItem.SAVED_RECORDING_TIME_ADDED, System.currentTimeMillis());
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_SIZE, fileSize);
        cv.put(DBHelperItem.SAVED_RECORDING_TAG, tag);
        cv.put(DBHelperItem.SAVED_RECORDING_TAG_COLOUR, tagColour);
        cv.put(DBHelperItem.SAVED_RECORDING_TEXT_COLOR, textColor);
        cv.put(DBHelperItem.SAVED_RECORDING_URL, url);
        cv.put(DBHelperItem.SAVED_RECORDING_CLOUD, cloud);
        long rowId = db.insert(DBHelperItem.SAVED_RECORDINGS_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void removeTags(LinkedList<String> filePaths, String[] whereArgs){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        int length = filePaths.size();
        if(filePaths != null)
        {
            for(int i =0;i<length;i++)
            {
                cv.put(DBHelperItem.SAVED_RECORDING_TAG,"");
                cv.put(DBHelperItem.SAVED_RECORDING_TAG_COLOUR,"#FFFFFF");
                //System.out.println(filePaths.get(i));
                db.update(DBHelperItem.SAVED_RECORDINGS_NAME, cv,
                        DBHelperItem._ID + "=" + getItemByFilePath(filePaths.get(i)).getId(), null);
                if (mOnDatabaseChangedListener != null) {
                    mOnDatabaseChangedListener.onDatabaseEntryRenamed();
                }
            }
        }

        removeTagWWithName(whereArgs);
    }

    public void renameItem(RecordingItem item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH, filePath);
        db.update(DBHelperItem.SAVED_RECORDINGS_NAME, cv,
                DBHelperItem._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
        Log.d("TEST", "RENNAMED" + recordingName + " " + filePath);
    }


    public void changeTag(RecordingItem item, String tagName, String tagColour){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.SAVED_RECORDING_TAG, tagName);
        cv.put(DBHelperItem.SAVED_RECORDING_TAG_COLOUR, tagColour);
        db.update(DBHelperItem.SAVED_RECORDINGS_NAME, cv,
                DBHelperItem._ID + "=" + item.getId(), null);

    }

    public void changeUrl(RecordingItem item, String url)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.SAVED_RECORDING_URL, url);
        db.update(DBHelperItem.SAVED_RECORDINGS_NAME,cv,
                DBHelperItem._ID + "=" + item.getId(), null);
        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public void setTag(String recordingFilePath, int tagId){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.TAG_SYSTEM_TAG_NAME,
                DBHelperItem.TAG_SYSTEM_TAG_COLOR,
                DBHelperItem.TAG_SYSTEM_TEXT_COLOUR
        };
        Cursor c = db.query(DBHelperItem.TAG_SYSTEM_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(tagId)) {

            // main content update
            ContentValues cv = new ContentValues();

            // Strings
            String tagName;
            String tagColor;
            String textColor;

            // get the current values of the position
            tagName = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TAG_NAME));
            tagColor = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TAG_COLOR));
            textColor = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR));


            if(tagName.equals("No Tag")){

                tagName = "";
                tagColor = "#FFFFFF";
                textColor = "#000000";
            }

            // update the table
            cv.put(DBHelperItem.SAVED_RECORDING_TAG, tagName);
            cv.put(DBHelperItem.SAVED_RECORDING_TAG_COLOUR, tagColor);
            cv.put(DBHelperItem.SAVED_RECORDING_TEXT_COLOR, textColor);
            db.update(DBHelperItem.SAVED_RECORDINGS_NAME, cv,
                    DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + " = '" + recordingFilePath + "'", null);

            c.close();

            if (mOnDatabaseChangedListener != null) {
                mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
            }
        }
    }

    public LinkedList<TagItem> getTags() {
        SQLiteDatabase db = getReadableDatabase();

        // variables
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.TAG_SYSTEM_TAG_NAME,
                DBHelperItem.TAG_SYSTEM_TAG_COLOR,
                DBHelperItem.TAG_SYSTEM_TEXT_COLOUR

        };

        Cursor c = db.query(
                DBHelperItem.TAG_SYSTEM_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);


        // create the linked list.
        LinkedList<TagItem> tagList;
        tagList = new LinkedList<TagItem>();
        c.moveToFirst();
        while(!c.isAfterLast()){

            // variables
            TagItem currentTagItem;
            int tagId;
            String tagName;
            String tagColor;
            String textColor;

            tagId = c.getInt(c.getColumnIndex(DBHelperItem._ID));
            tagName = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TAG_NAME));
            tagColor = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TAG_COLOR));
            textColor = c.getString(c.getColumnIndex(DBHelperItem.TAG_SYSTEM_TEXT_COLOUR));

            currentTagItem = new TagItem(tagId, tagName, tagColor, textColor);
            tagList.add(currentTagItem);
            c.moveToNext();
        }
        c.close();
        return tagList;
    }

    @Deprecated
    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_NAME, item.getName());
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH, item.getLength());
        cv.put(DBHelperItem.SAVED_RECORDING_TIME_ADDED, item.getTime());
        cv.put(DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,item.getSize());
        cv.put(DBHelperItem.SAVED_RECORDING_TAG, item.getTag());
        cv.put(DBHelperItem.SAVED_RECORDING_TAG_COLOUR,item.getColour());
        cv.put(DBHelperItem.SAVED_RECORDING_URL, item.getUrl());
        cv.put(DBHelperItem.SAVED_RECORDING_CLOUD,item.getIsCloud());
        cv.put(DBHelperItem._ID, item.getId());
        long rowId = db.insert(DBHelperItem.SAVED_RECORDINGS_NAME, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }

    public void restoreDeletedFiles() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_NAME,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
                DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH,
                DBHelperItem.SAVED_RECORDING_TIME_ADDED,
                DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,
                DBHelperItem.SAVED_RECORDING_TAG,
                DBHelperItem.SAVED_RECORDING_TAG_COLOUR,
                DBHelperItem.SAVED_RECORDING_TEXT_COLOR,
                DBHelperItem.SAVED_RECORDING_URL,
                DBHelperItem.SAVED_RECORDING_CLOUD

        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                NOT_DELETED,
                null,
                null,
                null,
                null);

        c.moveToFirst();
        while(!c.isAfterLast()){
            RecordingItem item = getItemByFilePath(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH)));
            String name = item.getName();
            String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + name;
            File f = new File(mFilePath);


            File oldFilePath = new File(item.getFilePath());
            oldFilePath.renameTo(f);
            renameItem(item, name, mFilePath);

            c.moveToNext();
        }

        c.close();

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
    }

    public void restoreDeletedFile(String filePath) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_NAME,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
                DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH,
                DBHelperItem.SAVED_RECORDING_TIME_ADDED,
                DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,
                DBHelperItem.SAVED_RECORDING_TAG,
                DBHelperItem.SAVED_RECORDING_TAG_COLOUR,
                DBHelperItem.SAVED_RECORDING_URL,
                DBHelperItem.SAVED_RECORDING_CLOUD
        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH + " like '%" + filePath + "%'",
                null,
                null,
                null,
                null);

        c.moveToFirst();
        if(!c.isAfterLast()){
            RecordingItem item = getItemByFilePath(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH)));
            String name = item.getName();
            String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + name;
            File f = new File(mFilePath);


            File oldFilePath = new File(item.getFilePath());
            oldFilePath.renameTo(f);
            renameItem(item, name, mFilePath);

        }

        c.close();

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
    }

    public void emptyTrash() {
        SQLiteDatabase db = getReadableDatabase();
        RecordingItem item;

        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.SAVED_RECORDING_RECORDING_NAME,
                DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH,
                DBHelperItem.SAVED_RECORDING_RECORDING_LENGTH,
                DBHelperItem.SAVED_RECORDING_TIME_ADDED,
                DBHelperItem.SAVED_RECORDING_RECORDING_SIZE,
                DBHelperItem.SAVED_RECORDING_TAG,
                DBHelperItem.SAVED_RECORDING_TAG_COLOUR,
                DBHelperItem.SAVED_RECORDING_TEXT_COLOR,
                DBHelperItem.SAVED_RECORDING_CLOUD,
                DBHelperItem.SAVED_RECORDING_URL
        };

        Cursor c = db.query(
                DBHelperItem.SAVED_RECORDINGS_NAME,
                projection,
                NOT_DELETED,
                null,
                null,
                null,
                null);


        c.moveToFirst();
        while(!c.isAfterLast()){
            item = getItemByFilePath(c.getString(c.getColumnIndex(DBHelperItem.SAVED_RECORDING_RECORDING_FILE_PATH)));
            File file = new File(item.getFilePath());
            removeItemWithId(item.getId());
            file.delete();

            c.moveToNext();
        }

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public void injectString(String string) {
        SQLiteDatabase db = getWritableDatabase();

        SQLiteStatement output = db.compileStatement(string);
        output.execute();
        
        //if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        //}
    }
    
    public String printDatabase() throws Exception {

        // variables
        SQLiteDatabase database;
        Cursor cursor;
        String tableString;


        // initialize
        database = getWritableDatabase();

        // generate rows
        tableString = String.format("Table %s:\n", DBHelperItem.SAVED_RECORDINGS_NAME);
        cursor = database.rawQuery("SELECT * FROM " + DBHelperItem.SAVED_RECORDINGS_NAME, null);

        // check for results, and iterate
        while (cursor.moveToNext()) {

            // variables
            String[] columnNames;


            // get columns while there is a next column
            columnNames = cursor.getColumnNames();

            // add each column plus the value
            for (String name : columnNames)
                tableString += String.format(
                        "%s: %s\n",
                        name,
                        cursor.getString(cursor.getColumnIndex(name)));

            // enter a newline
            tableString += "\n";
        }
        cursor.close();

        return tableString;
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

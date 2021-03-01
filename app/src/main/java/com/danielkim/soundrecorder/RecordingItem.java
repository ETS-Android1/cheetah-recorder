package com.danielkim.soundrecorder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Daniel on 12/30/2014.
 */
public class RecordingItem implements Parcelable {
    private String mName; // file name
    private String mFilePath; //file path
    private int mId; //id in database
    private int mLength; // length of recording in seconds
    private long mTime; // date/time of the recording
    private double mSize; // file size of the recording

    public RecordingItem() {
    }

    public RecordingItem(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
        mId = in.readInt();
        mLength = in.readInt();
        mTime = in.readLong();
        mSize = in.readDouble();
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public void setSize(double size) {
        mSize = size;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public double getSize() {

        return mSize;
    }

    public String getSizeFormatted() {

        // variables
        String[] unit;
        double size;
        int order;

        String output;


        // assign
        unit = new String[]{"KB", "MB", "GB", "TB"};
        size = this.mSize;

        // assume Bytes to start, divide to find the units order.
        order = 0;
        while (size >= 1024 && order < unit.length - 1){

            size = size / 1024;
            order++;
        }

        output = String.format("%.0f %s", size, unit[order]);
        return output;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public static final Parcelable.Creator<RecordingItem> CREATOR = new Parcelable.Creator<RecordingItem>() {
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mLength);
        dest.writeLong(mTime);
        dest.writeString(mFilePath);
        dest.writeString(mName);
        dest.writeDouble(mSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
package com.danielkim.soundrecorder.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.fragments.FileViewerFragment;
import com.danielkim.soundrecorder.fragments.FilterFragment;
import com.danielkim.soundrecorder.fragments.RecordFragment;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;

    //Database
    private DBHelper mDatabase;

    // store fragments
    private RecordFragment currentRecordFragment;
    private FileViewerFragment currentFileViewerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // variables
        Intent intent;

        // handle option menu
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_debug:
                intent = new Intent(this, SqlDebugActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                showFilterFragment();
            case R.id.action_refresh:
                currentFileViewerFragment.getAdapter().updateFilePaths();
                break;
            case R.id.action_cloudDownload:
                CloudDownloadDialog();
                break;
            case R.id.action_view_trash:
                currentFileViewerFragment.getAdapter().updateFilePaths(DBHelper.DELETED);
                viewTrash();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void viewTrash(){

    }

    public void CloudDownloadDialog () {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_cloud_download, null);

        final EditText input = (EditText) view.findViewById(R.id.url_input);

        final String tempUrl = "https://firebasestorage.googleapis.com/v0/b/soundrecorder-f12a3.appspot.com/o/SoundRecorder%2Ffd2ea7ee-5c0b-45d8-be28-8a4856bbd9c8.mp4?alt=media&token=c9092b68-de83-4294-9ad2-625dfb3e543e";

        renameFileBuilder.setTitle("Download from Cloud");
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton("Download",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String value = input.getText().toString().trim();

                        try {
//
                            System.out.println("----------------------INPUIT = " + value);
                            System.out.println("-----------------------------LENGTH = " + value.length());

                            //urlReachable is only for get Requests. Look into alternative for url audio file checking
//                            if (value.length() <= 0 || !(urlReachable(value)))

                            //input validation & url reachable flag

                                if (value.length() <= 0) {
                                Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                                return;
                            }
                                    doInBackground(value);
                                    dialog.cancel();

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                            Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        renameFileBuilder.setNegativeButton(this.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();

        //updateFilePaths();
    }

//    private boolean urlReachable(String file_url)  {
//        try {
//            URL url = new URL(file_url);
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            int code = connection.getResponseCode();
//            if (code == 200)
//                return true;
//        }catch (Exception e) {
//            return false;
//        }
//        return false;
//    }



    protected Boolean doInBackground(String url) {
        boolean flag = true;
        boolean downloading =true;
        mDatabase = new DBHelper(getApplicationContext());
        int count = 0;
        File f;
        String mFilePath;
        String mFileName;
        MediaPlayer mediaPlayer;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        try{

            DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request mRqRequest = new DownloadManager.Request(
                    Uri.parse(url));

            do{
                count++;

                //file name
                mFileName = "Cloud_Recording" + "_" + (mDatabase.getCount() + count) + ".mp4";

                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                mFilePath += "/SoundRecorder/" + mFileName;

                f = new File("/SoundRecorder/" + mFileName);

            }while (f.exists() && !f.isDirectory());


            mRqRequest.setDestinationInExternalPublicDir("/SoundRecorder/", mFileName);
            long idDownLoad=mManager.enqueue(mRqRequest);


            DownloadManager.Query query = null;
            query = new DownloadManager.Query();
            Cursor c = null;
            if(query!=null) {
                query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
            } else {
                return flag;
            }

            while (downloading) {
                c = mManager.query(query);
                if(c.moveToFirst()) {
                    Log.i ("FLAG","Downloading");
                    int status =c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if (status==DownloadManager.STATUS_SUCCESSFUL) {

                        Log.i ("FLAG","done");

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(this,Uri.parse(mFilePath));
                        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);



                        File newfile = new File(mFilePath);
                        double mFileSize = newfile.length() /1024;
                        int millSecond = Integer.parseInt(duration);

                        //Add file to database
                        try {
                            mDatabase.addRecording(mFileName, mFilePath, millSecond, mFileSize, "Cloud", "#95D9DA");
                        } catch (Exception e){
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed: " + e, Toast.LENGTH_LONG).show();

                            Log.e(LOG_TAG, "exception", e);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(this, "Success ", Toast.LENGTH_LONG).show();

                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {
                        Log.i ("FLAG","Fail");
                        downloading = false;
                        flag=false;

                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to download ", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }

            return flag;
        }catch (Exception e) {
            flag = false;
            return flag;
        }
    }


//    private static void getDuration(File file) {
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
//        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//
//        System.out.println("------------------------------------" + durationStr);
//
//        // return Utils.formateMilliSeccond(Long.parseLong(durationStr));
//    }


    public void showFilterFragment(){

        // create the filter fragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        android.app.Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        FilterFragment newFragment =
                FilterFragment.
                        newInstance(currentFileViewerFragment);
        newFragment.show(ft, "dialog");
    }


    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = { getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings)};

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:{

                    currentRecordFragment = RecordFragment.newInstance(position);
                    return currentRecordFragment;
                }
                case 1:{
                    currentFileViewerFragment = FileViewerFragment.newInstance(position);
                    return currentFileViewerFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


}

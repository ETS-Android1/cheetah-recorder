package com.danielkim.soundrecorder.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
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
                break;
            case R.id.action_cloudDownload:
                CloudDownloadDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }



    public void CloudDownloadDialog () {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_cloud_download, null);

        final EditText input = (EditText) view.findViewById(R.id.url_input);

        final String tempUrl = "https://firebasestorage.googleapis.com/v0/b/soundrecorder-f12a3.appspot.com/o/SoundRecorder%2Fbf3680c3-f31e-45e6-a5b4-c7460d7431ff.mp4?alt=media&token=e33d48d2-b825-45b9-8305-f4cb00c7f5c5";

        renameFileBuilder.setTitle("Download from Cloud");
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton("Download",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim();
//
                            //download code goes here
                            //saveToInternalStorage(tempUrl);
                            doInBackground(tempUrl);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
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

    private void saveToInternalStorage(String url) {
        File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");

        if (!folder.exists()) {
            folder.mkdir();
        }

        int count = 0;
        File f;
        mDatabase = new DBHelper(getApplicationContext());

        do{
            count++;

            //file name
            String mFileName = "Cloud_Recording"
                    + "_" + (mDatabase.getCount() + count) + ".mp4";

            //Need absolute path to see if file exists
            String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            //temp url
            Uri file_uri = Uri.parse(url);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(file_uri);

            //Setting title of request
            request.setTitle("Audio Download");

            //Setting description of request
            request.setDescription("Android Audio download using DownloadManager.");

            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir("/SoundRecorder/", mFileName);

            //Enqueue download
            downloadManager.enqueue(request);


            //NOT working...
            f = new File("/SoundRecorder/" + mFileName);

            double mFileSize = f.length();
            mFileSize = mFileSize / 1024;

            System.out.println("------------------------------Length = " + f.length());
            System.out.println("------------------------------Size = "+ mFileSize);


            //Add file to database
            try {

                mDatabase.addRecording(mFileName, mFilePath, 23, mFileSize);

            } catch (Exception e){
                Log.e(LOG_TAG, "exception", e);
            }



        }while (f.exists() && !f.isDirectory());

    }


    protected Boolean doInBackground(String url) {
        boolean flag = true;
        boolean downloading =true;
        try{
            DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request mRqRequest = new DownloadManager.Request(
                    Uri.parse(url));
            mRqRequest.setDestinationInExternalPublicDir("/SoundRecorder/", "Cloud123.mp4");

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
                        downloading = false;
                        flag=true;
                        File f = new File("/SoundRecorder/" + "Cloud_Recording_12.mp4");

                        double mFileSize = f.length();
                        mFileSize = mFileSize / 1024;

                        System.out.println("------------------------------Length = " + f.length());
                        System.out.println("------------------------------Size = "+ mFileSize);
                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {
                        Log.i ("FLAG","Fail");
                        downloading = false;
                        flag=false;
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


    private static void getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        System.out.println("------------------------------------" + durationStr);

        // return Utils.formateMilliSeccond(Long.parseLong(durationStr));
    }





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


        Toast.makeText(MainActivity.this, "TEST", Toast.LENGTH_LONG).show();
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

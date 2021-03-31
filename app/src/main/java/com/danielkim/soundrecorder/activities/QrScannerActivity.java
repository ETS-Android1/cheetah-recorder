package com.danielkim.soundrecorder.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.google.zxing.Result;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

public class QrScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private DBHelper mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);


        // check permission method is to check that the
        // camera permission is granted by user or not.
        // request permission method is to request the
        // camera permission if not given.
        if (checkPermission()) {
            // if permission is already granted display a toast message
            Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();

        } else {
            requestPermission();
        }


        if (checkPermission())
        {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = result.getText();
                        boolean success = QR_URL_Download(url);

                        if (success) {
                            Intent k = new Intent(QrScannerActivity.this, MainActivity.class);
                            startActivity(k);
                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private boolean checkPermission() {
        // here we are checking two permission that is vibrate
        // and camera which is granted by user and not.
        // if permission is granted then we are returning
        // true otherwise false.
        int camera_permission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int vibrate_permission = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        return camera_permission == PackageManager.PERMISSION_GRANTED && vibrate_permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // this method is to request
        // the runtime permission.
        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, VIBRATE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // this method is called when user
        // allows the permission to use camera.
        if (grantResults.length > 0) {
            boolean cameraaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean vibrateaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (cameraaccepted && vibrateaccepted) {
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denined \n You cannot use app without providing permssion", Toast.LENGTH_SHORT).show();
            }
        }
    }


    protected Boolean QR_URL_Download(String url) {
        boolean flag = true;
        boolean downloading =true;
        mDatabase = new DBHelper(getApplicationContext());
        int count = 0;
        File f;
        String mFilePath;
        String mFileName;
        MediaPlayer mediaPlayer;
        //System.out.println("--------------------> Link = " + url);

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
                            mDatabase.addRecording(mFileName, mFilePath, millSecond, mFileSize, "Cloud", "#95D9DA", url, 1);
                        } catch (Exception e){
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed: " + e, Toast.LENGTH_LONG).show();

                            Log.e("LOG_TAG", "exception", e);
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
}
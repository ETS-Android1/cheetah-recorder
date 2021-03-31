package com.danielkim.soundrecorder.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;
import com.google.zxing.WriterException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

import static android.content.Context.WINDOW_SERVICE;

public class MyUploadsAdapter extends RecyclerView.Adapter<MyUploadsAdapter.MyUploadHolder>
        implements OnDatabaseChangedListener {

    public final String LOG_TAG = "MyUploadsAdapter";

    // variables
    private DBHelper mDatabase;
    private Context mContext;
    private LinkedList<RecordingItem> uploadedFiles;
    private LinearLayoutManager llm;

    //QR code share
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";

    // data is passed into the constructor
    //MyUploadsAdapter(Context context) {
    //    this.mInflater = LayoutInflater.from(context);
    //}

    public static class MyUploadHolder extends RecyclerView.ViewHolder {

        // variables
        protected CardView card;
        protected TextView fileName;
        protected TextView url;
        protected ImageView image;


        public MyUploadHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.upload_card_view);
            fileName = itemView.findViewById(R.id.upload_file_name);
            url = itemView.findViewById(R.id.upload_url);
            image = (ImageView) itemView.findViewById((R.id.upload_Image));

        }
    }

    // pass in things in from the activity!
    public MyUploadsAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();

        mContext = context;
        mDatabase = new DBHelper(mContext);
        llm = linearLayoutManager;

        uploadedFiles = mDatabase.getCloudUploads();
    }

    @NonNull
    @Override
    public MyUploadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_my_uploads, parent, false);

        mContext = parent.getContext();

        return new MyUploadHolder(itemView);
    }

    // inflates the row layout from xml when needed
    @Override
    public void onBindViewHolder(@NonNull final MyUploadHolder holder, final int position) {

        // implement functionality of tet element

        //
        holder.fileName.setText(uploadedFiles.get(position).getName());
        holder.url.setText(uploadedFiles.get(position).getUrl());

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add("Share");
                entrys.add("Download");
                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));

                builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {
//                                    System.out.println("---------------- link = " + holder.url.getText().toString());
                                    QrCodeGenerator(holder.url.getText().toString());
                                } else if (item == 1) {
                                    CloudDownloadDialog(holder.url.getText().toString());
                                }
                            }
                        });

                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                // click code goes here/
               // CloudDownloadDialog(holder.url.getText().toString());
            }
        });
    }






    public void QrCodeGenerator (final String url) {

        //Showing Progress bar
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        // File rename dialog
        AlertDialog.Builder qrBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_qr_generate, null);

        final ImageView qrImage = (ImageView) view.findViewById(R.id.idIVQrcode);


        WindowManager manager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(url, null, QRGContents.Type.TEXT, dimen);


        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Log.e("Tag", e.toString());
        }

        //temporary save the file to disk in order to share it
        saveToFiles();

        qrBuilder.setView(view);
        qrBuilder.setTitle("QR code");
        qrBuilder.setCancelable(true);
        qrBuilder.setPositiveButton(("Share"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {

                            String path = Environment.getExternalStorageDirectory().toString()+"/QRCode/tempQr.jpg";
                            Bitmap bitmap1 = BitmapFactory.decodeFile(path);

                            String toSend = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "QR", "Download Recording");
                            Uri uri = Uri.parse(toSend);

                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/jpg");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            mContext.startActivity(Intent.createChooser(shareIntent, "Share image using"));


                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                            progressDialog.dismiss();
                        }

                        dialog.cancel();
                    }
                });
        qrBuilder.setNegativeButton(("Cancel"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        qrBuilder.setView(view);
        AlertDialog alert = qrBuilder.create();
        progressDialog.dismiss();
        alert.show();
    }


    private void saveToFiles(){
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                boolean save = new QRGSaver().save(savePath, "tempQr", bitmap, QRGContents.ImageType.IMAGE_JPEG);

                //Test to make sure image was saved
                if(!save){
                    Toast.makeText(mContext, "Error: Image Not saved", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }





    public void CloudDownloadDialog(String url) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_cloud_download, null);

        final EditText input = (EditText) view.findViewById(R.id.url_input);

        //final String tempUrl = "https://firebasestorage.googleapis.com/v0/b/soundrecorder-f12a3.appspot.com/o/SoundRecorder%2Ffd2ea7ee-5c0b-45d8-be28-8a4856bbd9c8.mp4?alt=media&token=c9092b68-de83-4294-9ad2-625dfb3e543e";

        try {
//
            System.out.println("----------------------INPUIT = " + url);
            System.out.println("-----------------------------LENGTH = " + url.length());

            //urlReachable is only for get Requests. Look into alternative for url audio file checking
//                            if (url.length() <= 0 || !(urlReachable(url)))

            //input validation & url reachable flag

            if (url.length() <= 0) {
                Toast.makeText(mContext, "Invalid URL", Toast.LENGTH_SHORT).show();
                return;
            }
            doInBackground(url);

        } catch (Exception e) {
            Log.e(LOG_TAG, "exception", e);
            Toast.makeText(mContext, "Invalid URL", Toast.LENGTH_SHORT).show();
        }


        //renameFileBuilder.setView(view);
        //AlertDialog alert = renameFileBuilder.create();
        //alert.show();

        //updateFilePaths();
    }

    protected Boolean doInBackground(String url) {
        boolean flag = true;
        boolean downloading =true;
        mDatabase = new DBHelper(mContext.getApplicationContext());
        int count = 0;
        File f;
        String mFilePath;
        String mFileName;
        MediaPlayer mediaPlayer;

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Downloading...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        try{

            DownloadManager mManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
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
                        retriever.setDataSource(mContext,Uri.parse(mFilePath));
                        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);



                        File newfile = new File(mFilePath);
                        double mFileSize = newfile.length() /1024;
                        int millSecond = Integer.parseInt(duration);

                        //Add file to database
                        try {
                            mDatabase.addRecording(mFileName, mFilePath, millSecond, mFileSize, "", "#FFFFFF", url, 1);
                        } catch (Exception e){
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "Failed: " + e, Toast.LENGTH_LONG).show();

                            Log.e(LOG_TAG, "exception", e);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Success ", Toast.LENGTH_LONG).show();

                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {
                        Log.i ("FLAG","Fail");
                        downloading = false;
                        flag=false;

                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Failed to download ", Toast.LENGTH_LONG).show();
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

    // total number of rows
    @Override
    public int getItemCount() {
        //Toast.makeText(mContext, "SIZE : " + uploadedFiles.size(), Toast.LENGTH_LONG).show();
        return uploadedFiles.size();
    }


    @Override
    public void onNewDatabaseEntryAdded() {
        this.notifyDataSetChanged();
    }

    @Override
    public void onDatabaseEntryRenamed() {
        this.notifyDataSetChanged();
    }
}
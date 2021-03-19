package com.danielkim.soundrecorder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.LinkedList;

public class MyUploadsAdapter extends RecyclerView.Adapter<MyUploadsAdapter.MyUploadHolder>
        implements OnDatabaseChangedListener {

    public final String LOG_TAG = "MyUploadsAdapter";

    // variables
    private DBHelper mDatabase;
    private Context mContext;
    private LinkedList<RecordingItem> uploadedFiles;
    private LinearLayoutManager llm;

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

                // click code goes here/
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return uploadedFiles.size();
    }


    @Override
    public void onNewDatabaseEntryAdded() {

    }

    @Override
    public void onDatabaseEntryRenamed() {

    }
}
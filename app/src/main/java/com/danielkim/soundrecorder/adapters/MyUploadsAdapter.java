package com.danielkim.soundrecorder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;

import java.util.LinkedList;

public class MyUploadsAdapter extends RecyclerView.Adapter<MyUploadsAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private DBHelper mDatabase;
    private LinkedList<String> fileNames;

    // data is passed into the constructor
    MyUploadsAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public void onBindViewHolder(final FileViewerAdapter.RecordingsViewHolder holder, final int position) {
        View view = mInflater.inflate(R.layout.card_my_uploads, parent, false);
        returm
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RecordingItem item = getItem(position);

        String animal = mData.get(position);
        holder.myTextView.setText(animal);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return filePaths.size();
    }


    public RecordingItem getItem(int position) {



        // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView file_name;
        TextView url;
        ImageView image;


        ViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.upload_file_name);
            url = itemView.findViewById(R.id.upload_url);
            image = (ImageView) itemView.findViewById((R.id.upload_Image));

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
package com.danielkim.soundrecorder.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TagViewerAdapter extends RecyclerView.Adapter<TagViewerAdapter.TagViewHolder>
        implements OnDatabaseChangedListener {

    public final String LOG_TAG = "TagViewerAdapter";

    // variables
    private DBHelper mDatabase;
    private LinkedList<TagItem> tagList;
    private String mRecordingFilePath;
    private Context mContext;
    private LinearLayoutManager llm;


    public static class TagViewHolder extends RecyclerView.ViewHolder {

        protected Button buttonColor;
        protected TextView tagName;

        public TagViewHolder(View v) {
            super(v);
            buttonColor = v.findViewById(R.id.buttonColor);
            tagName = v.findViewById(R.id.textTagName);
        }
    }

    public TagViewerAdapter(Context context, LinearLayoutManager linearLayoutManager, String recordingFilePath) {
        super();

        mContext = context;
        mDatabase = new DBHelper(mContext);
        llm = linearLayoutManager;
        mRecordingFilePath = recordingFilePath;
        tagList = mDatabase.getTags();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.tag_view, parent, false);

        mContext = parent.getContext();

        return new TagViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TagViewHolder holder, final int tagId) {

        // variables
        TagItem currentTag;


        // assign
        currentTag = tagList.get(tagId);
        holder.tagName.setText(currentTag.getTagName());
        holder.buttonColor.setBackgroundColor(Color.parseColor(currentTag.getTagColor()));
        holder.buttonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addTag(tagId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    @Override
    public void onNewDatabaseEntryAdded() {

    }

    @Override
    public void onDatabaseEntryRenamed() {

    }

    public void addTag(int tagId){

        // update the database
        mDatabase.setTag(mRecordingFilePath, tagId);
        //notifyDataSetChanged();
    }
}

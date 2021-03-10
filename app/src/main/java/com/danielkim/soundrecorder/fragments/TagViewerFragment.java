package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;

import android.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.TagViewerAdapter;


public class TagViewerFragment extends DialogFragment {
    private static final String ARG_RECORDING_ID = "recording_id";
    private static final String LOG_TAG = "TagViewerFragment";

    private String recordingFilePath;
    private TagViewerAdapter mTagViewerAdapter;

    public static TagViewerFragment newInstance(String recordingFilePath) {
        TagViewerFragment t = new TagViewerFragment();
        Bundle b = new Bundle();
        b.putString(ARG_RECORDING_ID, recordingFilePath);
        t.setArguments(b);

        return t;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordingFilePath = getArguments().getString(ARG_RECORDING_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tag_viewer, container, false);

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.tagRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mTagViewerAdapter = new TagViewerAdapter(getActivity(), llm, recordingFilePath);
        mRecyclerView.setAdapter(mTagViewerAdapter);

        return v;
    }

    public TagViewerAdapter getAdapter(){

        return this.mTagViewerAdapter;
    }
}
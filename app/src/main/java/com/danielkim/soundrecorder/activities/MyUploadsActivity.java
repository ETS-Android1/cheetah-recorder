package com.danielkim.soundrecorder.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;

public class MyUploadsActivity extends AppCompatActivity {
    private FileViewerAdapter mFileViewerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);
    }
}
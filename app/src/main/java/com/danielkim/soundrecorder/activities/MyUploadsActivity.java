package com.danielkim.soundrecorder.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.MyUploadsAdapter;

public class MyUploadsActivity extends AppCompatActivity {
    private MyUploadsAdapter myUploadsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);

        //View v = inflater.inflate(R.layout.fragment_tag_viewer, container, false);

        RecyclerView mRecyclerView = (RecyclerView) this.findViewById(R.id.upload_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        myUploadsAdapter = new MyUploadsAdapter(this, llm);
        mRecyclerView.setAdapter(myUploadsAdapter);
    }

    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.menu_main, menu);
    //    return true;
    //}
}
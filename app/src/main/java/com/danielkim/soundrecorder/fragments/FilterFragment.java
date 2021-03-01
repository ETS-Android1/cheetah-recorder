package com.danielkim.soundrecorder.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.Toast;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FilterFragment extends DialogFragment {

    // store current activity
    FileViewerFragment fileViewerFragment;

    // widgets
    EditText    searchText;
    Button      searchButton;

    Switch      doFilterFileDate;
    EditText    minDateText;
    EditText    maxDateText;

    Switch      doFilterTextSize;
    TextView    sizeText;
    SeekBar     selectFileSize;
    int         fileSize;

    // variables
    SimpleDateFormat sqlLiteDate;


    public static FilterFragment newInstance(FileViewerFragment fileViewerFragment) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        fragment.setFileViewerFragment(fileViewerFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater dialogInflater = getActivity().getLayoutInflater();
        View filterView = dialogInflater.inflate(R.layout.fragment_filter, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(filterView)
                .setTitle("FILTER");
                //.setPositiveButton("Search");


        searchText = filterView.findViewById(R.id.searchText);
        searchButton = filterView.findViewById(R.id.searchButton);

        doFilterFileDate = filterView.findViewById(R.id.doFilterFileDate);
        minDateText = filterView.findViewById(R.id.minDateText);
        minDateText.setFocusable(false);
        maxDateText = filterView.findViewById(R.id.maxDateText);
        maxDateText.setFocusable(false);

        doFilterTextSize = filterView.findViewById(R.id.doFilterTextSize);
        sizeText = filterView.findViewById(R.id.sizeText);
        selectFileSize = filterView.findViewById(R.id.selectFileSize);


        // create date format
        sqlLiteDate = new SimpleDateFormat(
                "YYYY-MM-DD",
                Locale.getDefault());


        // create default size
        fileSize = -1;

        // implement listeners and field updates
        createSizeFunctionality();
        createDateFunctionality();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), createSelectQuery(), Toast.LENGTH_LONG).show();
                Log.d(">>>>>>>>>>", createSelectQuery());

                String query;

                query = createSelectQuery();
                if(!query.equals(""))
                    fileViewerFragment.getAdapter().updateFilePaths(query);
                else
                    fileViewerFragment.getAdapter().updateFilePaths();
            }
        });

        return dialogBuilder.create();
    }

    private void createDateFunctionality(){

        // min and max date are only editable using a date picker dialog
        minDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // variables
                DatePickerDialog minDialog;


                // create date picker dialog
                minDialog = new DatePickerDialog(getContext());
                minDialog.show();
                minDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        // variables
                        Date date;
                        String parsedDate;


                        // create a standard date object
                        date = new Date(year,month,dayOfMonth);
                        parsedDate = sqlLiteDate.format(date);
                        minDateText.setText(parsedDate);
                    }
                });
            }
        });

        maxDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // variables
                DatePickerDialog minDialog;


                // create date picker dialog
                minDialog = new DatePickerDialog(getContext());
                minDialog.show();
                minDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        // variables
                        Date date;
                        String parsedDate;


                        // create a standard date object
                        //date = new Date(year,month,dayOfMonth);
                        //parsedDate = sqlLiteDate.format(date);
                        maxDateText.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                });
            }
        });
    }

    private void createSizeFunctionality(){

        // implement seek bar
        selectFileSize.setMax(1000000);
        selectFileSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                sizeText.setText(new String(progress + " kb"));
                fileSize = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private String createSelectQuery(){

        // variables
        StringBuilder textClause;
        StringBuilder dateClause;
        StringBuilder sizeClause;

        String finalSelect;
        String finalTextClause;
        String finalDateClause;
        String finalSizeClause;


        // initilize our string builder
        textClause = new StringBuilder();
        dateClause = new StringBuilder();
        sizeClause = new StringBuilder();


        // create text search
        if(!searchText.getText().toString().equals("")){

            textClause.append(DBHelper.DBHelperItem.COLUMN_NAME_RECORDING_NAME);
            textClause.append(" like '%");
            textClause.append(searchText.getText().toString());
            textClause.append("%' ");
        }

        // create date search
        if(!minDateText.getText().toString().equals("")){

            dateClause.append(DBHelper.DBHelperItem.COLUMN_NAME_TIME_ADDED);
            dateClause.append(" between '");
            dateClause.append(minDateText.getText().toString());
            dateClause.append("' and '");

            if(!minDateText.getText().toString().equals("")){

                dateClause.append(maxDateText.getText().toString());
                dateClause.append("' ");
            }
            else{

                dateClause.append("3000-01-01");
                dateClause.append("' ");
            }
        }
        else if(!maxDateText.getText().toString().equals("")){

            dateClause.append(DBHelper.DBHelperItem.COLUMN_NAME_TIME_ADDED);
            dateClause.append(" between '");
            dateClause.append("1971-01-01");
            dateClause.append("' and '");
            dateClause.append(maxDateText.getText().toString());
            dateClause.append("' ");
        }

        // sizeClause
        if(fileSize != -1){

            sizeClause.append(DBHelper.DBHelperItem.COLUMN_NAME_RECORDING_SIZE);
            sizeClause.append(" between '");
            sizeClause.append(minDateText);
            sizeClause.append("' and ");
        }





        
        return textClause.toString();
    }

    public void setFileViewerFragment(FileViewerFragment fileViewerFragment){

        this.fileViewerFragment = fileViewerFragment;
    }
}
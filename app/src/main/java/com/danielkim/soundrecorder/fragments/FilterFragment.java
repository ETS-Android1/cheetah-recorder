package com.danielkim.soundrecorder.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.Toast;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class FilterFragment extends DialogFragment {

    // constants
    static public final int MIN_FILE_SIZE = 0;
    static public final int MAX_FILE_SIZE = 1048576 * 2;
    static public final int LOG_MIN_FILE_SIZE;
    static public final int LOG_MAX_FILE_SIZE;
    static public final double LOG_SCALE;
    static public final String[] UNIT = new String[]{"KB", "MB", "GB", "TB", "PT"};

    // store current activity for updating
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


    static{
        if(MIN_FILE_SIZE == 0)
            LOG_MIN_FILE_SIZE = 0;
        else
            LOG_MIN_FILE_SIZE = (int) Math.log(MIN_FILE_SIZE);

        LOG_MAX_FILE_SIZE = (int) Math.log(MAX_FILE_SIZE);
        LOG_SCALE = (double) (LOG_MAX_FILE_SIZE - LOG_MIN_FILE_SIZE) / (MAX_FILE_SIZE - MIN_FILE_SIZE);
    }

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
        createEnabledFunctionality();

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
                        date = new Date();
                        parsedDate = sqlLiteDate.format(date);
                        minDateText.setText(year + "-" + month + "-" + dayOfMonth);
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

                        maxDateText.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                });
            }
        });
    }

    private void createSizeFunctionality(){

        // implement seek bar
        selectFileSize.setMax(MAX_FILE_SIZE);
        selectFileSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // variables
                int actualSize;
                int displaySize;
                int unitOrder;

                String display;

                // apply a logarithmic scale
                actualSize = (int) Math.exp(LOG_MIN_FILE_SIZE + (LOG_SCALE * (progress - MIN_FILE_SIZE)));
                displaySize = actualSize;

                // assume kilo bytes to start, divide to find the units order.
                unitOrder = 0;
                while (displaySize >= 1024 && unitOrder < UNIT.length - 1){

                    displaySize = displaySize / 1024;
                    unitOrder++;
                }


                display = "" + displaySize + UNIT[unitOrder];
                sizeText.setText(display);
                fileSize = actualSize;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void createEnabledFunctionality(){

        // assume false to start
        doFilterFileDate.setChecked(false);
        doFilterTextSize.setChecked(false);


        minDateText.setVisibility(View.INVISIBLE);
        maxDateText.setVisibility(View.INVISIBLE);


        doFilterFileDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    minDateText.setVisibility(View.VISIBLE);
                    maxDateText.setVisibility(View.VISIBLE);
                }
                else {
                    minDateText.setVisibility(View.INVISIBLE);
                    maxDateText.setVisibility(View.INVISIBLE);
                }

            }
        });

        doFilterFileDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    minDateText.setVisibility(View.VISIBLE);
                    maxDateText.setVisibility(View.VISIBLE);
                }
                else {
                    minDateText.setVisibility(View.INVISIBLE);
                    maxDateText.setVisibility(View.INVISIBLE);
                }

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
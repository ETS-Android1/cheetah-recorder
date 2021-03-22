package com.danielkim.soundrecorder.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.FileViewerAdapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;


public class FilterFragment extends DialogFragment {

    // constants
    static public final int MIN_FILE_SIZE = 0;
    static public final int MAX_FILE_SIZE = 1024 * 2;
    static public final int LOG_MIN_FILE_SIZE;
    static public final int LOG_MAX_FILE_SIZE;
    static public final double LOG_SCALE;
    static public final String[] UNIT = new String[]{"KB", "MB", "GB", "TB", "PT"};
    static public final long YEAR_3000 = 32503698000000l;

    // store current activity for updating
    FileViewerFragment fileViewerFragment;

    // widgets
    EditText    searchText;
    Button      searchButton;

    // Text View - set to GONE when Date is not selected
    TextView textMinDisplay;
    TextView textMaxDisplay;

    Switch      doFilterFileDate;
    boolean     filterDate;
    EditText    minDateText;
    Calendar    minDate;
    EditText    maxDateText;
    Calendar    maxDate;

    Switch      doFilterFileSize;
    boolean     filterSize;
    RadioGroup  sizeGroup;
    RadioButton lessThan;
    RadioButton greaterThan;
    TextView    sizeText;
    SeekBar     selectFileSize;
    int         fileSize;
    TextView    textSmallestSize;
    TextView    textLargestSize;

    // Tag
    boolean     filterTag;
    Switch      doFilterTag;
    TextView    tagText;
    EditText    tagEditText;

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


        // text search
        searchText = filterView.findViewById(R.id.searchText);
        searchButton = filterView.findViewById(R.id.searchButton);

        // date search
        doFilterFileDate = filterView.findViewById(R.id.doFilterFileDate);
        textMinDisplay = filterView.findViewById(R.id.textMinDisplay);
        textMaxDisplay = filterView.findViewById(R.id.textMaxDisplay);
        minDateText = filterView.findViewById(R.id.minDateText);
        minDateText.setFocusable(false);
        maxDateText = filterView.findViewById(R.id.maxDateText);
        maxDateText.setFocusable(false);

        // size search
        doFilterFileSize = filterView.findViewById(R.id.doFilterFileSize);
        lessThan = filterView.findViewById(R.id.radioLessThan);
        greaterThan = filterView.findViewById(R.id.radioGreaterThan);
        sizeText = filterView.findViewById(R.id.sizeText);
        selectFileSize = filterView.findViewById(R.id.selectFileSize);
        textSmallestSize =  filterView.findViewById(R.id.textSmallestSize);
        textLargestSize = filterView.findViewById(R.id.textLargestSize);


        // tag search
        doFilterTag = filterView.findViewById(R.id.doFilterTag);
        tagText = filterView.findViewById(R.id.tagTextView);
        tagEditText = filterView.findViewById(R.id.tagEditView);


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

                // variables
                String query;

                query = createSelectQuery();

                if(!query.equals(""))
                    fileViewerFragment.getAdapter().updateFilePaths(query + " and " + DBHelper.DELETED);
                else
                    fileViewerFragment.getAdapter().updateFilePaths();

                getActivity().getFragmentManager().popBackStack();


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

                        minDate = Calendar.getInstance();
                        minDate.set(year, month, dayOfMonth);
                        minDateText.setText(String.format("%04d-%02d-%02d", year, month, dayOfMonth));
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

                        maxDate = Calendar.getInstance();
                        maxDate.set(year, month, dayOfMonth);
                        maxDateText.setText(String.format("%04d-%02d-%02d", year, month, dayOfMonth));
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
        filterDate = false;
        minDateText.setVisibility(View.GONE);
        maxDateText.setVisibility(View.GONE);
        textMinDisplay.setVisibility(View.GONE);
        textMaxDisplay.setVisibility(View.GONE);

        doFilterFileSize.setChecked(false);
        filterSize = false;
        lessThan.setVisibility(View.GONE);
        greaterThan.setVisibility(View.GONE);
        sizeText.setVisibility(View.GONE);
        selectFileSize.setVisibility(View.GONE);
        textSmallestSize.setVisibility(View.GONE);
        textLargestSize.setVisibility(View.GONE);
        minDateText.setText("");
        maxDateText.setText("");

        // tag
        filterTag = false;
        doFilterTag.setChecked(false);
        tagText.setVisibility(View.GONE);
        tagEditText.setVisibility(View.GONE);

        // create listener functionality
        doFilterFileDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    filterDate = true;
                    minDateText.setVisibility(View.VISIBLE);
                    maxDateText.setVisibility(View.VISIBLE);
                    textMinDisplay.setVisibility(View.VISIBLE);
                    textMaxDisplay.setVisibility(View.VISIBLE);
                }
                else {

                    filterDate = false;
                    minDateText.setVisibility(View.GONE);
                    maxDateText.setVisibility(View.GONE);
                    textMinDisplay.setVisibility(View.GONE);
                    textMaxDisplay.setVisibility(View.GONE);
                    minDateText.setText("");
                    maxDateText.setText("");
                }

            }
        });

        doFilterFileSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    filterSize = true;
                    lessThan.setVisibility(View.VISIBLE);
                    greaterThan.setVisibility(View.VISIBLE);
                    sizeText.setVisibility(View.VISIBLE);
                    selectFileSize.setVisibility(View.VISIBLE);
                    textSmallestSize.setVisibility(View.VISIBLE);
                    textLargestSize.setVisibility(View.VISIBLE);
                }
                else {

                    filterSize = false;
                    lessThan.setVisibility(View.GONE);
                    greaterThan.setVisibility(View.GONE);
                    sizeText.setVisibility(View.GONE);
                    selectFileSize.setVisibility(View.GONE);
                    textSmallestSize.setVisibility(View.GONE);
                    textLargestSize.setVisibility(View.GONE);
                }

            }
        });

        doFilterTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    filterTag = true;
                    tagText.setVisibility(View.VISIBLE);
                    tagEditText.setVisibility(View.VISIBLE);

                }
                else {

                    filterTag = false;
                    tagText.setVisibility(View.GONE);
                    tagEditText.setVisibility(View.GONE);

                }

            }
        });


    }

    private String createSelectQuery(){

        // variables
        StringBuilder textClause;
        StringBuilder dateClause;
        StringBuilder sizeClause;
        LinkedList<String> clauseGrouper;
        StringBuilder finalClause;


        // initilize our string builder
        textClause = new StringBuilder();
        dateClause = new StringBuilder();
        sizeClause = new StringBuilder();
        finalClause = new StringBuilder();


        // create text search
        if(!searchText.getText().toString().equals("")){

            textClause.append(DBHelper.DBHelperItem.SAVED_RECORDING_RECORDING_NAME);
            textClause.append(" like '%");
            textClause.append(searchText.getText().toString());
            textClause.append("%' ");
        }

        // create date search
        if(filterDate) {
            if (!minDateText.getText().toString().equals("")) {

                dateClause.append(DBHelper.DBHelperItem.SAVED_RECORDING_TIME_ADDED);
                dateClause.append(" between '");
                dateClause.append(minDate.getTimeInMillis());
                dateClause.append("' and '");

                if(!maxDateText.getText().toString().equals("")) {

                    dateClause.append(maxDate.getTimeInMillis());
                    dateClause.append("' ");
                } else {

                    dateClause.append(YEAR_3000);
                    dateClause.append("' ");
                }
            } else if (!maxDateText.getText().toString().equals("")) {

                dateClause.append(DBHelper.DBHelperItem.SAVED_RECORDING_TIME_ADDED);
                dateClause.append(" between '");
                dateClause.append(0);
                dateClause.append("' and '");
                dateClause.append(maxDate.getTimeInMillis());
                dateClause.append("'");
            }
        }

        // sizeClause
        if(filterSize) {
            sizeClause.append(DBHelper.DBHelperItem.SAVED_RECORDING_RECORDING_SIZE);

            if(lessThan.isChecked())
                sizeClause.append(" < ");
            else
                sizeClause.append(" > ");
            sizeClause.append(fileSize);
        }

        // tagClause



        // create grouping for final clause
        clauseGrouper = new LinkedList<String>();
        if(!textClause.toString().equals(""))
            clauseGrouper.add(textClause.toString());

        if(!sizeClause.toString().equals(""))
            clauseGrouper.add(sizeClause.toString());

        if(!dateClause.toString().equals(""))
            clauseGrouper.add(dateClause.toString());

        for (int i = 0; i < clauseGrouper.size(); i++) {

            finalClause.append(clauseGrouper.get(i));
            if (i < clauseGrouper.size() - 1)
                finalClause.append(" and ");
        }


        return finalClause.toString();
    }

    public void setFileViewerFragment(FileViewerFragment fileViewerFragment){

        this.fileViewerFragment = fileViewerFragment;
    }
}
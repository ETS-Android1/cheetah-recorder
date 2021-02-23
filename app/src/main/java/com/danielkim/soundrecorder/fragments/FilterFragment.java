package com.danielkim.soundrecorder.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {

    // Initialize variables here
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";
    private int position;

    // time picker
    TextView timerMin, timerMax;
    int t1Hour, t1Minute, t2Hour, t2Minute;

    // search field
    EditText editTextSearch;
    // https://developer.android.com/training/basics/firstapp/building-ui


    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilterFragment newInstance(/*String param1, String param2*/ int position) {
//        FilterFragment fragment = new FilterFragment();
//        Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;

        FilterFragment f = new FilterFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
//        observer.startWatching();
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // assign values
        timerMin = (TextView) getView().findViewById(R.id.timer_min);
        timerMax = (TextView) getView().findViewById(R.id.timer_max);

        // listeners
        timerMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(), // might be error, would have been mainactivity.this
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int hourOfDay, int minute) {
                                // Initialize hour and minute
                                t1Hour = hourOfDay;
                                t1Minute = minute;
                                // initialize calendar
                                Calendar calendar = Calendar.getInstance();
                                // Set hour and minute
                                calendar.set(0, 0, 0, t1Hour, t1Minute);
                                // set selected time on text view
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                                timerMin.setText(sdf.format(calendar.getTime()));

                            }
                        }, 12, 0, false
                );
                // Displayed prev selected time
                timePickerDialog.updateTime(t1Hour, t1Minute);
                // show dialog
                timePickerDialog.show();

            }
        });

        timerMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog2 = new TimePickerDialog(
                        getContext(), // might be error, would have been mainactivity.this
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int hourOfDay, int minute) {
                                // Initialize hour and minute
                                t2Hour = hourOfDay;
                                t2Minute = minute;
                                // initialize calendar
                                Calendar calendar = Calendar.getInstance();
                                // Set hour and minute
                                calendar.set(0, 0, 0, t2Hour, t2Minute);
                                // set selected time on text view
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                                timerMax.setText(sdf.format(calendar.getTime()));

                            }
                        }, 12, 0, false
                );
                // Displayed prev selected time
                timePickerDialog2.updateTime(t2Hour, t2Minute);
                // show dialog
                timePickerDialog2.show();
            }
        });

        // edit text
        View v = inflater.inflate(R.layout.fragment_filter, container, false);
        editTextSearch = (EditText) v.findViewById(R.id.editText_search);
        editTextSearch.setHint("this value is changed");

        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                switch (checkedId) {
                    case R.id.radio_1:
                        // do something
                    case R.id.radio_2:
                        // do something
                    case R.id.radio_3:
                        // do something
                        break;
                }
            }
        });


        // Inflate the layout for this fragment
        return v;
    }
}
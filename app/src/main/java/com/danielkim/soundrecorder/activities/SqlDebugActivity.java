package com.danielkim.soundrecorder.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;

public class SqlDebugActivity extends AppCompatActivity {

    // activity widgets
    private EditText command;
    private EditText output;
    private Button execute;

    // database helper
    private DBHelper mDatabase;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_debug);


        // initialize widgets
        command = (EditText) findViewById(R.id.command);
        output = (EditText) findViewById(R.id.output);
        execute = (Button) findViewById(R.id.execute);

        // initialize database
        mDatabase = new DBHelper(getApplicationContext());

        // exccute sql
        execute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                executeCommand();
            }
        });
    }


    private void executeCommand(){

        // variables
        String debugMessage1;
        String debugMessage2;
        String finalMessage;

        try{

            // inject a the command
            mDatabase.injectString(command.getText().toString());
            debugMessage1 = "Command Executed:\n " + command.getText() + "\n";
        }
        catch(Exception e){

            debugMessage1 = "Command Failed:\n" + command.getText() + "\n" + e.getMessage() + "\n";
        }

        try{

            // Display the database
            debugMessage2 = "Database:\n" + mDatabase.printDatabase();
        }
        catch(Exception e) {


            debugMessage2 = "Database Failed:\n" + e.getMessage();
        }

        // display text
        finalMessage = debugMessage1 + debugMessage2;
        output.setText(finalMessage);
    }
}
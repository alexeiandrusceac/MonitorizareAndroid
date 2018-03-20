package com.example.alexei.monitorizare;

//import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;


public class MonitorizareMainActivity extends AppCompatActivity {
        DatePickerDialog datepicker;
        DataBaseHelper mydbHelper = new DataBaseHelper(this.getApplicationContext());
        final Context mContext = this;
        private EditText dateOuput;
        private EditText primitOutput;
        private EditText cheltuitOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        dateOuput = (EditText)findViewById(R.id.dateTextOutput);
        primitOutput = (EditText)findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (EditText)findViewById(R.id.outputCheltuitOutput);
    }

    public void myClickHandler (View view)
    {
        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);

        but.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                LayoutInflater li = LayoutInflater.from(mContext);
                View dialogView = li.inflate(R.layout.data_layout, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);
                //alertDialogBuilder.setIcon(R.drawable.ic_launcher);
                // set custom_dialog.xml to alertdialog builder
                alertDialogBuilder.setView(dialogView);
                final EditText primitInput = (EditText)findViewById(R.id.inputText);
                final EditText cheltuitInput = (EditText)findViewById(R.id.outputText);
                final  EditText dateInput  =  setDate(dialogView);
               // final TextView differenceInput = (TextView)findViewById(R.id.differenceText);


                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // get user input and set it to etOutput
                                        // edit text
                                        int primitValue = 0;
                                        int cheltuitValue = 0;

                                        int difference = 0;
                                        primitValue =  Integer.parseInt(primitInput.getText().toString());
                                        cheltuitValue = Integer.parseInt(cheltuitInput.getText().toString());

                                        difference = primitValue - cheltuitValue;

                                        mydbHelper.insertData(dateInput.getText().toString(),primitValue,cheltuitValue,difference,0,0);
                                        /*dateOuput.setText(dateInput.getText());
                                        primitOutput.setText(primitInput.getText());
                                        cheltuitOutput.setText(cheltuitInput.getText());*/

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }

        });
    }

    public EditText setDate(View dialogView)
    {
        final EditText dateinput = (EditText) dialogView.findViewById(R.id.dateText);
        dateinput.setInputType(InputType.TYPE_NULL);
        dateinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                datepicker = new DatePickerDialog(MonitorizareMainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        dateinput.setText(date + "/" + month + "/" + year);
                    }
                }, day, month, year);
                datepicker.show();
            }
        });
        return dateinput;
    }

}

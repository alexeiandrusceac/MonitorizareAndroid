package com.example.alexei.monitorizare;

//import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MonitorizareMainActivity extends AppCompatActivity {
        DatePickerDialog datepicker;
        private DataBaseHelper mydbHelper;
        final Context mContext = this;
        private List<InOut> listOfData = new ArrayList<>();
        private EditText dateOuput;
        private EditText primitOutput;
        private EditText cheltuitOutput;
        private TextView idOutput;
        private TableLayout dataTableLayout;
        ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        dateOuput = (EditText)findViewById(R.id.dateTextOutput);
        primitOutput = (EditText)findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (EditText)findViewById(R.id.outputCheltuitOutput);
        idOutput =(TextView)findViewById(R.id.idText);

        progressBar = new ProgressDialog(this);
        mydbHelper = new DataBaseHelper(this);


        dataTableLayout = (TableLayout)findViewById(R.id.table_Layout);
        dataTableLayout.setStretchAllColumns(true);

        startLoadDataFromDataBase();


    }

    private void startLoadDataFromDataBase() {
        progressBar.setCancelable(false);
        progressBar.setMessage("Incarcarea datelor....");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        new MyAsync().execute(0);
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
                final String  dateInsert = dateInput.getText().toString();
                final int inputInsert = Integer.parseInt(primitInput.getText().toString());
                final int outputInsert = Integer.parseInt(cheltuitInput.getText().toString());
                final int diferentaInsert = inputInsert - outputInsert;

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {


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
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createData(dateInsert, inputInsert, outputInsert, diferentaInsert, 0, 0);
                    }
                    // Show toast message when no text is entered
                        /*if (TextUtils.isEmpty(inputNote.getText().toString())) {
                            Toast.makeText(MonitorizareMainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            alertDialog.dismiss();
                        }*/

                    // check if user updating note
                       /* if (shouldUpdate && note != null) {
                            // update note by it's id
                            updateNote(inputNote.getText().toString(), position);
                        } else {
                            // create new note

                        }*/

                });
            }

        });
    }
    public void createData(String date, int primire,int cheltuire,int diferenta,int inputTotal,int outputTotal)
    {
        long id = mydbHelper.insertData(date,primire,cheltuire,diferenta,inputTotal,outputTotal);
        //InOut data = mydbHelper.getData();
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
////verifica aici
    public void  loadData() {
        ///sqlcon.open();
        listOfData.addAll(mydbHelper.getData());
        if(listOfData != null) {

            for (InOut item : listOfData) {
                TableRow tableRow = new TableRow(this);
                idOutput.setText(item.getId());
                dateOuput.setText(item.getDate());
                primitOutput.setText(item.getInput());
                cheltuitOutput.setText(item.getOutput());
                tableRow.addView(idOutput);
                tableRow.addView(dateOuput);
                tableRow.addView(primitOutput);
                tableRow.addView(cheltuitOutput);
                dataTableLayout.addView(tableRow);
            }
        }


    }


    class MyAsync extends AsyncTask<Integer, Integer,String>
    {
        @Override
        protected String doInBackground(Integer... params)
        {
            try
            {
                Thread.sleep(2000);
                loadData();
            }
            catch(InterruptedException ex){ex.printStackTrace();}
            return "Incarcarea sa facut cu succes!";
        }

        protected void onPostExecute(){
            progressBar.hide();
        }
        @Override
        protected void onPreExecute(){}
        @Override
        protected void onProgressUpdate(Integer... values){}
    }
}

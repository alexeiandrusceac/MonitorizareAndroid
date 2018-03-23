package com.example.alexei.monitorizare.view;

//import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MonitorizareMainActivity extends AppCompatActivity {
        DatePickerDialog datepicker;
        private DataBaseHelper mydbHelper;
        private SQLiteDatabase mDb;
        private RelativeLayout recycleView;
        private EditText dateOuput;
        private EditText primitOutput;
        private EditText cheltuitOutput;
        private TextView idOutput;
        private TableLayout dataTableLayout;
        ProgressDialog progressBar;

        private RecyclerView recyclerView;
        private CoordinatorLayout coordinatorLayout;
        private TextView noDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noDataView = findViewById(R.id.empty_data_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        dateOuput = (EditText) findViewById(R.id.dateTextOutput);
        primitOutput = (EditText) findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (EditText) findViewById(R.id.outputCheltuitOutput);
        idOutput = (TextView) findViewById(R.id.idText);
        recycleView = (RelativeLayout) findViewById(R.layout.activity_monitorizare_main);
        progressBar = new ProgressDialog(this);

        startLoadDataFromDataBase();

        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showdialog();
            }
        });



    }

    private void startLoadDataFromDataBase() {
        progressBar.setCancelable(false);
        progressBar.setMessage("Incarcarea datelor....");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        //loadData(dataBaseHelper);
         new MyAsync().execute(0);
    }

    public void showdialog () {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View dialogView = li.inflate(R.layout.data_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MonitorizareMainActivity.this);

        alertDialogBuilder.setView(dialogView);

        final EditText primitInput =  (EditText) dialogView.findViewById(R.id.inputText);
        final EditText cheltuitInput = (EditText) dialogView.findViewById(R.id.outputText);
        final EditText dateInput = setDate(dialogView);
        // final TextView differenceInput = (TextView)findViewById(R.id.differenceText);
        final InOut inOut = new InOut();

        final DataBaseHelper dataBaseHelper = DataBaseHelper.getsInstance(this);

        final String dateInsert = dateInput.getText().toString();
        final int inputInsert;
        final int outputInsert ;
        final int diferentaInsert ;

        inputInsert =  Integer.parseInt(primitInput.getText().toString());
        outputInsert = Integer.parseInt(cheltuitInput.getText().toString());
        diferentaInsert = inputInsert - outputInsert;

        inOut.DATE = dateInsert;
        inOut.INPUT = inputInsert;
        inOut.OUTPUT = outputInsert;
        inOut.DIFFERENCE = diferentaInsert;
        inOut.INPUTTOTAL= 0;
        inOut.OUTPUTTOTAL = 0;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.insertData(inOut);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
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
////verifica aici
    public void  loadData(DataBaseHelper dataBaseHelper) {

        List<InOut> listOfData = dataBaseHelper.getAllPosts();
            if (listOfData.size()>0) {
                noDataView.setVisibility(View.GONE);
                for (InOut inOut : listOfData) {
                    TableRow tableRow = new TableRow(this);
                    idOutput.setText(inOut.ID);
                    dateOuput.setText(inOut.DATE);
                    primitOutput.setText(inOut.INPUT);
                    cheltuitOutput.setText(inOut.OUTPUT);
                    tableRow.addView(idOutput);
                    tableRow.addView(dateOuput);
                    tableRow.addView(primitOutput);
                    tableRow.addView(cheltuitOutput);
                    dataTableLayout.addView(tableRow);
                }

            }
       else {


            }

    }

    class MyAsync extends AsyncTask<Integer, Integer,String>
    {
        @Override
        protected String doInBackground(Integer... params)
        {
            try
            {
                final DataBaseHelper dataBaseHelper = DataBaseHelper.getsInstance(MonitorizareMainActivity.this);

                Thread.sleep(2000);

                loadData(dataBaseHelper);
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

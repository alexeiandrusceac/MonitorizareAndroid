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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MonitorizareMainActivity extends AppCompatActivity {

    private DataBaseHelper mydbHelper;
    private SQLiteDatabase mDb;
    private RelativeLayout recycleView;
    private TextView dateOuput;
    private TextView primitOutput;
    private TextView cheltuitOutput;
    private TextView idOutput;
    private LinearLayout dataTableLayout;
    ProgressDialog progressBar;
    private Context context;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;
    private TextView noDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // noDataView = findViewById(R.id.empty_data_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        dateOuput = (TextView) findViewById(R.id.dateTextOutput);
        primitOutput = (TextView) findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (TextView) findViewById(R.id.outputCheltuitOutput);
        idOutput = (TextView) findViewById(R.id.idText);

        mydbHelper = new DataBaseHelper(getApplicationContext());
        try {
            mydbHelper.createDataBase();
        } catch (IOException ex) {
            ex.getMessage();
        }


        loadData();
        //countRecords();
        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);
        but.setOnClickListener(new OnClickListenerCreateData());

    }


    /*public void showdialog () {




        // set dialog message
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(dialogView)
                .setTitle("Creeaza inregistrare")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Context context = dialogView.getRootView().getContext();

                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

    }
*/

    public void countRecords() {

        int recordCount = new TableDataController(this).count();

    }

    public void loadData() {

        TableLayout tableLayoutRecords = (TableLayout) findViewById(R.id.table_layout);
        //tableLayoutRecords.removeAllViewsInLayout();
TableRow tableRow = (TableRow) findViewById(R.id.tableRowValue);
        List<InOut> listOfData = new TableDataController(this).getAllPosts();
        if (listOfData.size() > 0) {
        for(InOut inOut : listOfData)
        {

                /*idOutput.setText(String.valueOf(inOut.ID)); //.setText(inOut.ID);
                dateOuput.setText(String.valueOf(inOut.DATE));*/
                //primitOutput.setText(String.valueOf(inOut.INPUT));
                //cheltuitOutput.setText(String.valueOf(inOut.OUTPUT));
               /* tableLayoutRecords.addView(idOutput);
                tableLayoutRecords.addView(dateOuput);*/
            primitOutput.setText(String.valueOf(inOut.INPUT));
         tableRow.addView(primitOutput);
//                /tableRow.addView(textView);
                //tableRow.addView(cheltuitOutput);
tableLayoutRecords.addView(tableRow);

                    /*tableRow.addView(idOutput);
                    tableRow.addView(dateOuput);
                    tableRow.addView(primitOutput);
                    tableRow.addView(cheltuitOutput);
                    dataTableLayout.addView(tableRow);*/
            }

        } else {
            TextView locationItem = new TextView(this);
            locationItem.setPadding(8, 8, 8, 8);
            locationItem.setText("NU SUNT DATE");

            tableLayoutRecords.addView(locationItem);
            //  Toast.makeText(context, "NU SUNT DATE", Toast.LENGTH_SHORT).show();

        }

    }

}

package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
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
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
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
    private EditText primitOutput;
    private TextView cheltuitOutput;
    private TextView idOutput;
    private LinearLayout dataTableLayout;
    ProgressDialog progressBar;
    private Context context;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;
    private TextView noDataView;
    private final boolean fromExternalSource = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // noDataView = findViewById(R.id.empty_data_view);
       // coordinatorLayout = findViewById(R.id.coordinator_layout);
        dateOuput = (TextView) findViewById(R.id.dateTextOutput);
        primitOutput = (EditText) findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (TextView) findViewById(R.id.outputCheltuitOutput);
        idOutput = (TextView) findViewById(R.id.idText);


        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            loadData();
        }


        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);
        but.setOnClickListener(new OnClickListenerCreateData());

    }

    public void loadData() {
        DataBaseAccess dataBaseAccess;
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DataBaseHelper.DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // If external database is avaliable, deploy it
            dataBaseAccess = DataBaseAccess.getInstance(this, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(this, null);
        }

        TableLayout tableLayoutRecords = (TableLayout) findViewById(R.id.table_layout);
        //tableLayoutRecords.removeAllViewsInLayout();
       // TableRow tableRow = (TableRow) findViewById(R.id.tableRowValue);

        dataBaseAccess.open();
        List<InOut> listOfData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        TableRow rowHeader = new TableRow(this);
        rowHeader.setBackgroundColor(Color.parseColor("#c0c0c0"));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText={"ID","Data","Primire","Cheltuire","Diferenta"};

        for(String c:headerText) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(5, 5, 5, 5);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayoutRecords.addView(rowHeader);
        if (listOfData.size() > 0)
        {
            for(InOut inOut: listOfData) {
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                String[] colText = {inOut.ID +"",inOut.DATE,String.valueOf(inOut.INPUT),String.valueOf(inOut.OUTPUT),String.valueOf(inOut.DIFFERENCE)};
                for(String text : colText)
                {
                    TextView tv = new TextView(this);
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(16);
                    tv.setText(text);
                    row.addView(tv);
                }
                tableLayoutRecords.addView(row);
            }
        }
        else
        {
            TextView locationItem = new TextView(this);
            locationItem.setPadding(8, 8, 8, 8);
            locationItem.setText("NU SUNT DATE");

            tableLayoutRecords.addView(locationItem);
            //  Toast.makeText(context, "NU SUNT DATE", Toast.LENGTH_SHORT).show();

        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                loadData();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the quotes", Toast.LENGTH_SHORT).show();
            }
        }

    }

}

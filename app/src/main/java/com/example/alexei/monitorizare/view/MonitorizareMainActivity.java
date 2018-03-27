package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.LAYOUT_DIRECTION_LOCALE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static java.security.AccessController.getContext;


public class MonitorizareMainActivity extends AppCompatActivity {
   // private InOutAdapter mAdapter;
    private TableRow row;
    private RecyclerView recyclerView;
    private TextView dateOuput;
    private EditText primitOutput;
    private TextView cheltuitOutput;
    private TextView idOutput;
    private Context context;
    private List<InOut> listOfData = new ArrayList<>();
    private TableLayout tableLayoutRecords;
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

        tableLayoutRecords = (TableLayout) findViewById(R.id.table_layout);
        /*dateOuput = (TextView) findViewById(R.id.dateTextOutput);
        primitOutput = (EditText) findViewById(R.id.inputPrimitOutput);
        cheltuitOutput = (TextView) findViewById(R.id.outputCheltuitOutput);
        idOutput = (TextView) findViewById(R.id.idText);
*/      row = new TableRow(this);
        createColumns();

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            loadAllData();
        }
        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);
        but.setOnClickListener(new OnClickListenerCreateData());
    }
    public  void event(View view)
    {
    String  row =   view.getTag().toString();
   //int  row_id = tableLayoutRecords.indexOfChild(row);
        //View position = tableLayoutRecords.getChildAt(row.getFocusedChild().getId());

        /// / showDialogEditDelete(position);
    }
    private void showDialogEditDelete(final int position) {

        CharSequence colors[] = new CharSequence[]{"Editare", "Stergere"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alegeti optiunea");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    showDataDialog(true, listOfData.get(position), position);
                } else {
                    deleteData(position);
                }
            }
        });
        builder.show();
    }
    private void updateData(int position) {

        final DataBaseAccess dataBaseAccess;
        InOut data = listOfData.get(position);
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
        // deleting the note from db
        dataBaseAccess.open();

        // updating note in db
        dataBaseAccess.updateData(data);
        dataBaseAccess.close();
        // refreshing the list
        listOfData.set(position, data);
        //mAdapter.notifyItemChanged(position);

        //toggleEmptyNotes();
    }
    private void deleteData(int position) {
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
        // deleting the note from db
        dataBaseAccess.open();
        dataBaseAccess.deleteData(listOfData.get(position));
        dataBaseAccess.close();
        // removing the note from the list
        listOfData.remove(position);
        //mAdapter.notifyItemRemoved(position);

        //toggleEmptyNotes();
    }

    private void showDataDialog(final boolean shouldUpdate, final InOut inOut, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.data_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MonitorizareMainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final DataBaseAccess dataBaseAccess;
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
        // deleting the note from db


        final EditText dateInput = view.findViewById(R.id.dateText);
        final EditText input = view.findViewById(R.id.inputText);
        final EditText output = view.findViewById(R.id.outputText);

       // dialogTitle.setText(getString(R.string.));

        if (shouldUpdate && inOut != null) {
            dateInput.setText(String.valueOf(inOut.DATE));
            input.setText(String.valueOf(inOut.INPUT));
            output.setText(String.valueOf(inOut.OUTPUT));
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setTitle("Editare")
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(dateInput.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti data!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(input.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(output.getText().toString()))
                {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && inOut != null) {
                    // update note by it's id
                    updateData(position);
                } else {
                    // create new note
                   dataBaseAccess.open();
                   dataBaseAccess.insertData(inOut);
                   dataBaseAccess.close();

                }
            }
        });
    }
    public void createColumns() {
        TableRow rowHeader = new TableRow(this);
        rowHeader.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        rowHeader.setGravity(Gravity.CENTER);

        String[] headerText = {" Nr.crt ", " Data ", " Primire ", " Cheltuire ", " Diferenta "};

        for (String c : headerText) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(5, 5, 5, 5);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayoutRecords.addView(rowHeader);

    }

    public void loadInsertedRow() {
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

        dataBaseAccess.open();
        InOut inOut = dataBaseAccess.getInsertedRow();
        dataBaseAccess.close();

        TableRow tableRow = new TableRow(this);

        tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        String[] colText = {inOut.ID + "", inOut.DATE, String.valueOf(inOut.INPUT), String.valueOf(inOut.OUTPUT), String.valueOf(inOut.DIFFERENCE)};
        for (String text : colText) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            tv.setText(text);
            tableRow.addView(tv);
        }
        tableLayoutRecords.addView(tableRow);

    }



    public void loadAllData() {
        DataBaseAccess dataBaseAccess;

        TextView no_data_message = new TextView(this);
        no_data_message.setPadding(8, 8, 8, 8);
        no_data_message.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        no_data_message.setGravity(Gravity.CENTER);
        no_data_message.setText("Nu sunt date");
        no_data_message.setTextSize(20);

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

        dataBaseAccess.open();
        List<InOut> listOfData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        if (listOfData.size() > 0) {
            no_data_message.setVisibility(View.GONE);
            tableLayoutRecords.removeView(no_data_message);

            for (InOut inOut : listOfData) {

                TableRow tableRow = new  TableRow(this);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                String[] colText = {inOut.ID + "", inOut.DATE, String.valueOf(inOut.INPUT), String.valueOf(inOut.OUTPUT), String.valueOf(inOut.DIFFERENCE)};
                for (String text : colText) {
                    TextView tv = new TextView(this);
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(16);
                    tv.setText(text);
                    tableRow.addView(tv);
                }
                tableLayoutRecords.addView(tableRow);
            }
        } else {
            no_data_message.setVisibility(View.VISIBLE);
            tableLayoutRecords.addView(no_data_message);
            //  Toast.makeText(context, "NU SUNT DATE", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                loadAllData();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the quotes", Toast.LENGTH_SHORT).show();
            }
        }

    }

}

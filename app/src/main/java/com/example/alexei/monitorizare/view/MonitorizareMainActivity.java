package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.widget.Button;
import android.widget.DatePicker;
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

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

import static android.view.View.LAYOUT_DIRECTION_LOCALE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static java.security.AccessController.getContext;


public class MonitorizareMainActivity extends AppCompatActivity {

    private List<InOut> listOfData = new ArrayList<>();
    DataBaseAccess dataBaseAccess;
    //private TableLayout tableLayoutRecords;
    ///private TextView noDataView;
    private TableView<String[]> tb;
    private TableHelper tableHelper;
    DatePickerDialog datepicker;
    private final boolean fromExternalSource = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DataBaseHelper.DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // If external database is avaliable, deploy it
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, null);
        }

        dataBaseAccess.open();
        listOfData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        //noDataView = (TextView)findViewById(R.id.empty_notes_view);

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            tableHelper = new TableHelper(this);
            tb = (TableView<String[]>) findViewById(R.id.tableView);
            tb.setColumnCount(5);
            tb.setHeaderBackgroundColor(/*Color.parseColor("#2ecc71"))*/ContextCompat.getColor(MonitorizareMainActivity.this, R.color.colorAccent));

            /*tb.(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));*/
            //tb.setGra(Gravity.CENTER);

            tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, tableHelper.getHeaders()));
            tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
            tb.addDataClickListener(new TableDataClickListener<String[]>() {
                @Override
                public void onDataClicked(int rowIndex, String[] clickedData) {
                    showDialogEditDelete(rowIndex);
                }
            });


            //loadAllData();
        }
        FloatingActionButton but = (FloatingActionButton) findViewById(R.id.buttonFloating);
        but.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogInsertData();
                    }
                }
        );
    }

    private void showDialogEditDelete(final int position) {

        CharSequence colors[] = new CharSequence[]{"Editare", "Stergere"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alegeti optiunea");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    showDialogEditData(true, listOfData.get(position), position);
                } else {
                    deleteData(position);
                }
            }
        });
        builder.show();
    }

    private void updateData(InOut inOut, int position) {

        final DataBaseAccess dataBaseAccess;
        InOut data = listOfData.get(position);
        data.DATE = inOut.DATE;
        data.INPUT = inOut.INPUT;
        data.OUTPUT = inOut.OUTPUT;
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
        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
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
        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
        //mAdapter.notifyItemRemoved(position);

        //toggleEmptyNotes();
    }


    private void showDialogInsertData() {
        final DataBaseAccess dataBaseAccess;

        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog, null, false);


        final EditText primitInput = (EditText) formView.findViewById(R.id.inputText);
        final EditText cheltuitInput = (EditText) formView.findViewById(R.id.outputText);
        //final EditText dateInput = (EditText)formView.findViewById(R.id.dateText);
        final EditText dateInput = setDate(formView);
        // final TextView differenceInput = (TextView)findViewById(R.id.differenceText);

        //              final String dateInsert = dateInput.getText().toString();
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DataBaseHelper.DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // If external database is avaliable, deploy it
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, null);
        }


        new AlertDialog.Builder(MonitorizareMainActivity.this)
                .setView(formView)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final InOut inOut = new InOut();

                                inOut.DATE = dateInput.getText().toString();
                                inOut.INPUT = Integer.parseInt(primitInput.getText().toString());
                                inOut.OUTPUT = Integer.parseInt(cheltuitInput.getText().toString());
                                inOut.DIFFERENCE = Integer.parseInt(primitInput.getText().toString()) - Integer.parseInt(cheltuitInput.getText().toString());
                                inOut.INPUTTOTAL = 0;
                                inOut.OUTPUTTOTAL = 0;

                                dataBaseAccess.open();
                                boolean addSucces = dataBaseAccess.insertData(inOut);
                                dataBaseAccess.close();
                                if (addSucces) {
                                    Toast.makeText(MonitorizareMainActivity.this, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MonitorizareMainActivity.this, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
                                }

                                tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfData)));


                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
    }

    public EditText setDate(View dialogView) {
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


    private void showDialogEditData(final boolean shouldUpdate, final InOut inOut, final int position) {
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
        //final EditText primitInput = (EditText) formView.findViewById(R.id.inputText);
        //final EditText cheltuitInput = (EditText) formView.findViewById(R.id.outputText);
        //final EditText dateInput = (EditText)formView.findViewById(R.id.dateText);
        //final EditText dateInput = setDate(formView);

        final EditText dateInput =  setDate(view);
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
                //.setTitle("Editare")
                .setView(view)
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
                } else if (TextUtils.isEmpty(output.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && inOut != null) {
                    // update note by it's id
                    inOut.DATE = dateInput.getText().toString();
                    inOut.INPUT = Integer.parseInt(input.getText().toString());
                    inOut.OUTPUT = Integer.parseInt(output.getText().toString());


                    updateData(inOut,position);
                } else {
                    // create new note
                    dataBaseAccess.open();
                    dataBaseAccess.insertData(inOut);
                    dataBaseAccess.close();

                }
            }
        });
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
        listOfData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        if (listOfData.size() > 0) {
            no_data_message.setVisibility(View.GONE);
            //tableLayoutRecords.removeView(no_data_message);

            for (InOut inOut : listOfData) {

                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                String[] colText = {inOut.ID + "", inOut.DATE, String.valueOf(inOut.INPUT), String.valueOf(inOut.OUTPUT), String.valueOf(inOut.DIFFERENCE)};
                for (String text : colText) {
                    TextView tv = new TextView(this);
                    //tv.setOnClickListener(addButtonListener);
                    tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(16);
                    tv.setText(text);
                    tableRow.addView(tv);
                }
                //tableLayoutRecords.addView(tableRow);
            }
        } else {
            no_data_message.setVisibility(View.VISIBLE);
            // tableLayoutRecords.addView(no_data_message);
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

package com.example.alexei.monitorizare.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
import java.util.Calendar;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

/**
 * Created by Alexei on 3/24/2018.
 */

public class OnClickListenerCreateData implements View.OnClickListener {

    DatePickerDialog datepicker;
    Context context;

    private final boolean fromExternalSource = false;

    @Override
    public void onClick(View view) {
        final DataBaseAccess dataBaseAccess;
        context = view.getRootView().getContext();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog, null, false);
        final TableView<String[]> tb;
        final TableHelper tableHelper;

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
            dataBaseAccess = DataBaseAccess.getInstance(context, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(context, null);
        }
        new AlertDialog.Builder(context)
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
                                    Toast.makeText(context, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
                                }
                                ////Verifica aici
                                //tableHelper = new TableHelper(context);

                               // tb.setDataAdapter(new SimpleTableDataAdapter(context,tableHelper.getData()));
                                /// / ((TableHelper) context).loadAllData();
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

                datepicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
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
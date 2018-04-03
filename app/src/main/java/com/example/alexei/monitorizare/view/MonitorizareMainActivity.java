package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;

import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


import static com.example.alexei.monitorizare.database.DataBaseHelper.DATABASE_NAME;

// Google Play Services

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

public class MonitorizareMainActivity extends AppCompatActivity  implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Google Drive Activity";
    private  static final int REQUEST_CODE_OPENER = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private boolean backupOrRestore = true;
    private TextView inputTotalView;
    private TextView outputTotalView;
    private TextView differenceTotalView;
    private List<InOut> listOfData = new ArrayList<>();
    DataBaseAccess dataBaseAccess;
    private LinearLayout linearLayout;
    private TextView noDataView;
    private TableView<String[]> tb;
    private TableHelper tableHelper;
    DatePickerDialog datepicker;
    private final boolean fromExternalSource = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);

        noDataView = (TextView) findViewById(R.id.noDataView);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        inputTotalView = (TextView)findViewById(R.id.totalInput);
        outputTotalView = (TextView) findViewById(R.id.totalOutput);
        differenceTotalView = (TextView)findViewById(R.id.totalDifferenta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
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

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            tableHelper = new TableHelper(this);
            tb = (TableView<String[]>) findViewById(R.id.tableView);
            tb.setColumnCount(5);
            tb.setHeaderBackgroundColor(ContextCompat.getColor(MonitorizareMainActivity.this, R.color.colorAccent));
            tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, tableHelper.getHeaders()));
            tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
            tb.addDataClickListener(new TableDataClickListener<String[]>() {
                @Override
                public void onDataClicked(int rowIndex, String[] clickedData) {
                    showDialogEditDelete(rowIndex);
                }
            });
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
        showEmptyDataTextView();
        //connectToGoogleAPIClient();
        calculateSumTotal();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
            {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
                {
                    // wifi is enabled
                    Toast.makeText(MonitorizareMainActivity.this,"Este Online",Toast.LENGTH_SHORT).show();
                    connectToGoogleAPIClient();
                }
                else
                {
                    // wifi is disabled
                    Toast.makeText(MonitorizareMainActivity.this,"Nu sunteti online",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public void connectToGoogleAPIClient()
    {
        backupOrRestore = true;
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient = gApiClient(mGoogleApiClient);
        mGoogleApiClient.connect();
    }
    public void saveToDrive()
    {
        //database path on the device
        final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {

                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.i(TAG, "Nu sa executat o copie a bazei de date in Drive");
                    Toast.makeText(MonitorizareMainActivity.this, "Eroare la incarcarea in Google Drive, Mai incearca", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Backup to drive started.");

                try {

                    File dbFile = new File(inFileName);
                    FileInputStream fis = new FileInputStream(dbFile);
                    OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                    // Transfer bytes from the inputfile to the outputfile
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    //drive file metadata
                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setTitle("MonitorizareDB.db")
                            .setMimeType("application/db")
                            .build();

                    // Create an intent for the file chooser, and start it.
                    IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(driveContentsResult.getDriveContents())
                            .build(mGoogleApiClient);

                    startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void importFromDrive(DriveFile dbFile) {

        //database path on the device
        final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();

        dbFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {

                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to open Drive backup.");
                    Toast.makeText(MonitorizareMainActivity.this, "Error on loading from Google Drive. Retry", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "Backup to drive started.");

                // DriveContents object contains pointers to the actual byte stream
                DriveContents contents = driveContentsResult.getDriveContents();

                try {

                    ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                    // Open the empty db as the output stream
                    OutputStream outputStream = new FileOutputStream(inFileName);

                    // Transfer bytes from the inputfile to the outputfile
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    // Close the streams
                    outputStream.flush();
                    outputStream.close();
                    fileInputStream.close();

                    Toast.makeText(getApplicationContext(), "Import Completed", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error on loading", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public GoogleApiClient gApiClient(GoogleApiClient googleApiClient) {
        if(googleApiClient ==null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        return googleApiClient;
    }
    public void onConnectionFailed(ConnectionResult result) {

        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");

        //when the client is connected i have two possibility: backup (bckORrst -> true) or restore (bckORrst -> false)
        if (backupOrRestore)
            saveToDrive();
        else {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .setMimeType(new String[]{"application/db"})
                    .build(mGoogleApiClient);
            try {
                startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                Log.i(TAG, "Open File Intent send");
            } catch (IntentSender.SendIntentException e) {
                Log.w(TAG, "Unable to send Open File Intent", e);
            }
        }
    }

    private void showEmptyDataTextView() {

        if (listOfData.size() > 0) {
            noDataView.setVisibility(View.GONE);

        } else {
            noDataView.setVisibility(View.VISIBLE);
        }
    }

    private void showDialogEditDelete(final int position) {

        CharSequence colors[] = new CharSequence[]{"Editare", "Stergere"};

         final AlertDialog builder = new AlertDialog.Builder(MonitorizareMainActivity.this)
        .setTitle("Alegeti optiunea")
        .setItems(colors,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    showDialogEditData(true, listOfData.get(position), position);
                } else {
                    deleteData(position);
                }
            }
        }).show();
        doKeepDialog(builder);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_CREATOR:
                // Se apeleaza dupa ce se salveaza in Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Copia sa salvat cu succes.");
                    Toast.makeText(this, "Copia sa incarcat cu succes!", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    DriveFile file = driveId.asDriveFile();
                    importFromDrive(file);
                }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private static void doKeepDialog(Dialog dialog){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    private void updateData(InOut inOut, int position) {

        final DataBaseAccess dataBaseAccess;
        InOut data = listOfData.get(position);
        data.DATE = inOut.DATE;
        data.INPUT = inOut.INPUT;
        data.OUTPUT = inOut.OUTPUT;
        data.DIFFERENCE = inOut.INPUT - inOut.OUTPUT;
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
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
        boolean updateSuccess = dataBaseAccess.updateData(data);
        dataBaseAccess.close();
        if (updateSuccess) {
            Toast.makeText(MonitorizareMainActivity.this, "Informatia sa actualizat cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MonitorizareMainActivity.this, "Nu sa actualizat informatia", Toast.LENGTH_SHORT).show();

        }
        // refreshing the list
        listOfData.set(position, data);
        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
        //mAdapter.notifyItemChanged(position);
        tb.refreshDrawableState();
        showEmptyDataTextView();
    }

    private void deleteData(int position) {
        DataBaseAccess dataBaseAccess;
        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
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
        boolean deleteSucces = dataBaseAccess.deleteData(listOfData.get(position));
        dataBaseAccess.close();
        if (deleteSucces) {
            Toast.makeText(MonitorizareMainActivity.this, "Informatia sa sters cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MonitorizareMainActivity.this, "Nu sa sters informatia", Toast.LENGTH_SHORT).show();
        }

        listOfData.remove(position);
        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
        tb.refreshDrawableState();
        calculateSumTotal();
        showEmptyDataTextView();
    }

    public void calculateSumTotal() {
        int inputTotal = 0;
        int outputTotal = 0;
        int differenceTotal = 0;
        for (InOut inOut : listOfData)
        {
            inputTotal += inOut.INPUT;
            outputTotal += inOut.OUTPUT;
            differenceTotal += inOut.DIFFERENCE;
        }
        inputTotalView.setText(String.valueOf(inputTotal));
        outputTotalView.setText(String.valueOf(outputTotal));
        differenceTotalView.setText(String.valueOf(differenceTotal));
    }

    private void showDialogInsertData() {
        final DataBaseAccess dataBaseAccess;

        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog, null, false);


        final EditText primitInput = (EditText) formView.findViewById(R.id.inputText);
        final EditText cheltuitInput = (EditText) formView.findViewById(R.id.outputText);
        final EditText dateInput = setDate(formView);


        if (fromExternalSource) {
            // Check the external database file. External database must be available for the first time deployment.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // If external database is avaliable, deploy it
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, null);
        }

        final AlertDialog dialog = new AlertDialog.Builder(MonitorizareMainActivity.this)
                .setView(formView)
                .setCancelable(false)
                .setPositiveButton("Adauga",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final InOut inOut = new InOut();

                                inOut.DATE = dateInput.getText().toString();
                                inOut.INPUT = Integer.parseInt(primitInput.getText().toString());
                                inOut.OUTPUT =  Integer.parseInt(cheltuitInput.getText().toString());
                                inOut.DIFFERENCE = Integer.parseInt(primitInput.getText().toString()) - Integer.parseInt(cheltuitInput.getText().toString());

                                dataBaseAccess.open();
                                boolean addSucces = dataBaseAccess.insertData(inOut);
                                dataBaseAccess.close();
                                if (addSucces) {
                                    Toast.makeText(MonitorizareMainActivity.this, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MonitorizareMainActivity.this, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
                                }
                                dataBaseAccess.open();
                                listOfData = dataBaseAccess.getAllPosts();
                                dataBaseAccess.close();
                                tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfData)));

                                showEmptyDataTextView();
                                calculateSumTotal();
                            }
                        })
                .setNegativeButton("Anuleaza",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
        doKeepDialog(dialog);
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
                }, year, month, day);
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
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // If external database is avaliable, deploy it
            dataBaseAccess = DataBaseAccess.getInstance(this, externalDirectory);
        } else {
            // From assets
            dataBaseAccess = DataBaseAccess.getInstance(this, null);
        }

        final EditText dateInput = setDate(view);
        final EditText input = view.findViewById(R.id.inputText);
        final EditText output = view.findViewById(R.id.outputText);

        if (shouldUpdate && inOut != null) {
            dateInput.setText(String.valueOf(inOut.DATE));
            input.setText(String.valueOf(inOut.INPUT));
            output.setText(String.valueOf(inOut.OUTPUT));
            dateInput.setTextColor(Color.BLACK);
            input.setTextColor(Color.BLACK);
            output.setTextColor(Color.BLACK);
            input.setHintTextColor(Color.RED);
            output.setHintTextColor(Color.RED);
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(shouldUpdate ? "Actualizeaza" : "Salveaza", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Anuleaza",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        doKeepDialog(alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(input.getText().toString())) {
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

                    updateData(inOut, position);
                    calculateSumTotal();
                } else {
                    // create new note
                    dataBaseAccess.open();
                    dataBaseAccess.insertData(inOut);
                    dataBaseAccess.close();
                    calculateSumTotal();

                }
            }
        });
    }
}

package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;


import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;

import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


import static com.example.alexei.monitorizare.database.DataBaseHelper.DATABASE_NAME;


// Google Play Services

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MonitorizareMainActivity extends AppCompatActivity implements View.OnClickListener {
    private DriveResourceClient driveResourceClient;
    private DriveClient driveClient;
    private DriveResource driveResource;
    private GoogleSignInClient mGoogleSignIn;

    private static final String TAG = "Google Drive Activity";
    private static final int REQUEST_CODE_SIGNIN = 0;
    private static final int REQUEST_CODE_DATA = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private boolean buttonOpen = false;
    private TextView inputTotalView;
    private TextView outputTotalView;
    private TextView differenceTotalView;
    private List<InOut> listOfData = new ArrayList<>();
    private List<InOut> listOfNewData = new ArrayList<>();
    DataBaseAccess dataBaseAccess;

    private TextView noDataView;
    private TableView<String[]> tb;
    private TableHelper tableHelper;
    private Menu mainMenu;
    DatePickerDialog datepicker;
    private final boolean fromExternalSource = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    /// Floating Action Buttons
    private FloatingActionButton buttonAdd, buttonInput, buttonOutput;
    private Animation button_open, button_close, button_forward, button_backward;
    private TextView textViewInput, textViewOutput;
    private BroadcastReceiver broadcastReceiver;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.MIME_TYPE, "application/db"), Filters.eq(SearchableField.TITLE, "MonitorizareDB.db"))).build();

        noDataView = (TextView) findViewById(R.id.noDataView);

        inputTotalView = (TextView) findViewById(R.id.totalInput);
        outputTotalView = (TextView) findViewById(R.id.totalOutput);
        differenceTotalView = (TextView) findViewById(R.id.totalDifferenta);

        broadcastReceiver = new MyBroadcastReceiver();
        registerNetworkBroadcast();
        buttonAdd = (FloatingActionButton) findViewById(R.id.buttonFloating);
        buttonInput = (FloatingActionButton) findViewById(R.id.buttonFloatingPrimit);
        buttonOutput = (FloatingActionButton) findViewById(R.id.buttonFloatingCheltuit);

        textViewInput = (TextView) findViewById(R.id.inputTextFAB);
        textViewOutput = (TextView) findViewById(R.id.outputTextFAB);
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
        listOfNewData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            tableHelper = new TableHelper(this);
            tb = (TableView<String[]>) findViewById(R.id.tableView);
            tb.setColumnCount(4);
            tb.setHeaderBackgroundColor(ContextCompat.getColor(MonitorizareMainActivity.this, R.color.colorAccent));
            tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, tableHelper.getHeaders()));
            tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfNewData)));
            tb.addDataClickListener(new TableDataClickListener<String[]>() {
                @Override
                public void onDataClicked(int rowIndex, String[] clickedData) {
                    showDialogEditDelete(rowIndex);
                }
            });
        }

        button_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_open);
        button_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_close);
        button_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_forward);
        button_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_backward);

        buttonAdd.setOnClickListener(this);
        buttonInput.setOnClickListener(this);
        buttonOutput.setOnClickListener(this);

        showEmptyDataTextView();

        calculateSumTotal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

    private void registerNetworkBroadcast() {

        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void enableImportSaveIfExistDB() {
        final MenuItem downloadFromDrive = mainMenu.findItem(R.id.download_from_GDrive);
            MenuItem saveToDrive = mainMenu.findItem(R.id.import_to_drive);
        driveResourceClient.query(query)
                .addOnSuccessListener(this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                if(metadata.getCount() > 0) {
                    downloadFromDrive.setEnabled(true);
                }
                else
                {
                    downloadFromDrive.setEnabled(false);
                }
            }
        });
        saveToDrive.setEnabled(true);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonFloating:
                animateButtons();
                break;
            case R.id.buttonFloatingPrimit:
                showDialogInsertDataInput();
                break;
            case R.id.buttonFloatingCheltuit:
                showDialogInsertDataOutput();
                break;
        }
    }

    public void animateButtons() {
        if (buttonOpen) {
            buttonAdd.startAnimation(button_backward);
            buttonInput.startAnimation(button_close);
            buttonOutput.startAnimation(button_close);
            buttonInput.setClickable(false);
            buttonOutput.setClickable(false);
            buttonOpen = false;
            textViewInput.setVisibility(View.GONE);
            textViewOutput.setVisibility(View.GONE);
        } else {
            buttonAdd.startAnimation(button_forward);
            buttonInput.startAnimation(button_open);
            buttonOutput.startAnimation(button_open);
            buttonInput.setClickable(true);
            buttonOutput.setClickable(true);
            buttonOpen = true;
            textViewInput.setVisibility(View.VISIBLE);
            textViewOutput.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @SuppressLint("RestrictedApi")
    private void SignIn() {
        Log.i(TAG, "Logheaza - te");
        mGoogleSignIn = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignIn.getSignInIntent(), REQUEST_CODE_SIGNIN);
    }


    @NonNull
    @SuppressLint("RestrictedApi")
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(Drive.SCOPE_FILE).build();

        return GoogleSignIn.getClient(this, signInOptions);
    }


    public void saveToDrive() {
        //database path on the device
        final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();
        driveResourceClient.createContents().continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                Log.i(TAG, "Se creaza baza de date");
                File dbFile = new File(inFileName);
                FileInputStream fis = new FileInputStream(dbFile);
                OutputStream outputStream = task.getResult().getOutputStream();

                // Transfer bytes from the inputfile to the outputfile
                final byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                //drive file metadata
                final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle("MonitorizareDB.db")
                        .setMimeType("application/db")
                        .build();
                CreateFileActivityOptions createFileActivityOptions = new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(task.getResult())
                        .build();

                Task<MetadataBuffer> queryTask = driveResourceClient.query(query);

                queryTask.addOnSuccessListener(MonitorizareMainActivity.this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        for (Metadata m : metadata) {
                            driveResource = m.getDriveId().asDriveResource();
                            driveResourceClient.delete(driveResource);
                            driveResourceClient.trash(driveResource);
                        }
                    }
                }).addOnFailureListener(MonitorizareMainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                return driveClient
                        .newCreateFileActivityIntentSender(createFileActivityOptions)
                        .continueWith(
                                new Continuation<IntentSender, Void>() {
                                    @Override
                                    public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                        startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);

                                        return null;
                                    }
                                });
            }
        });
    }


    private void showEmptyDataTextView() {

        if (tb.getDataAdapter().getCount() > 0) {
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

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        MonitorizareMainActivity.super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGNIN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Sa logat cu succes");
                    // Se utilizeaza ultimul cont logat de cind are Google Drive
                    driveClient = Drive.getDriveClient(MonitorizareMainActivity.this, GoogleSignIn.getLastSignedInAccount(MonitorizareMainActivity.this));
                    // se creaza un Drive Resource Client
                    driveResourceClient =
                            Drive.getDriveResourceClient(MonitorizareMainActivity.this, GoogleSignIn.getLastSignedInAccount(MonitorizareMainActivity.this));
                   if(driveResourceClient !=null)
                   {enableImportSaveIfExistDB();}
                }
                break;
           /* case REQUEST_CODE_DATA:
                if(requestCode == RESULT_OK){
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    //Toast.makeText(this, driveId.toString(), Toast.LENGTH_SHORT).show();
                    DriveFile file = driveId.asDriveFile();

                }
                break;*/

            case REQUEST_CODE_CREATOR:
                // Se apeleaza dupa ce se salveaza in Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Copia sa salvat cu succes.");
                    Toast.makeText(MonitorizareMainActivity.this, "Copia sa incarcat cu succes!", Toast.LENGTH_SHORT).show();
                    enableImportSaveIfExistDB();
                }
                break;

        }
    }

    private void updateData(InOut inOut, int position) {

        final DataBaseAccess dataBaseAccess;
        InOut data = listOfData.get(position);
        data.DATE = inOut.DATE;
        data.INPUT = inOut.INPUT;
        data.OUTPUT = inOut.OUTPUT;

        if (fromExternalSource) {
            // Se verifica baza de date externa. Baza de date externa trebuie sa fie accesibila pentru prima lansare.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // Daca baza de date externa  este accesibila, atunci se lanseaza
            dataBaseAccess = DataBaseAccess.getInstance(this, externalDirectory);
        } else {
            // Daca nu este accesibila atunci se lanseaza din mapa Assets
            dataBaseAccess = DataBaseAccess.getInstance(this, null);
        }

        dataBaseAccess.open();

        // actualizarea inregistrarii in baza de date
        boolean updateSuccess = dataBaseAccess.updateData(data);
        dataBaseAccess.close();
        if (updateSuccess) {
            Toast.makeText(MonitorizareMainActivity.this, "Informatia sa actualizat cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MonitorizareMainActivity.this, "Nu sa actualizat informatia", Toast.LENGTH_SHORT).show();

        }

        // se reincarca lista de date
        listOfData.set(position, data);

        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfData)));
        tb.refreshDrawableState();
        showEmptyDataTextView();
    }

    private void deleteData(int position) {
        DataBaseAccess dataBaseAccess;
        if (fromExternalSource) {
            // Se verifica baza de date externa. Baza de date externa trebuie sa fie accesibila pentru prima lansare.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // Daca baza de date externa  este accesibila, atunci se lanseaza
            dataBaseAccess = DataBaseAccess.getInstance(this, externalDirectory);
        } else {
            // Daca nu este accesibila atunci se lanseaza din mapa Assets
            dataBaseAccess = DataBaseAccess.getInstance(this, null);
        }
        // stergerea inregistrarii din baza de date
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
        for (InOut inOut : listOfNewData) {
            inputTotal += inOut.INPUT;
            outputTotal += inOut.OUTPUT;

        }
        differenceTotal = inputTotal - outputTotal;
        inputTotalView.setText(String.valueOf(inputTotal));
        outputTotalView.setText(String.valueOf(outputTotal));
        differenceTotalView.setText(String.valueOf(differenceTotal));
    }

    private void showDialogInsertDataInput() {
        final DataBaseAccess dataBaseAccess;

        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog_input, null, false);

        final EditText primitInput = (EditText) formView.findViewById(R.id.inputText);

        final EditText dateInput = setDate(formView);


        if (fromExternalSource) {
            //Se verifica baza de date externa. Baza de date externa trebuie sa fie accesibila pentru prima lansare.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // Daca baza de date externa  este accesibila, atunci se lanseaza
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, externalDirectory);
        } else {
            // Daca nu este accesibila atunci se lanseaza din mapa Assets
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
                                 if(listOfNewData.size() == 0)
                                 {
                                    inOut.ID = 0;
                                 }
                                 else
                                 {
                                     inOut.ID =  listOfNewData.get(listOfNewData.size()-1).ID + 1;
                                 }
                                inOut.DATE = dateInput.getText().toString();
                                inOut.INPUT = Integer.parseInt(primitInput.getText().toString());
                                inOut.OUTPUT = 0;
                                listOfNewData.add(inOut);

                                dataBaseAccess.open();
                                boolean addSucces = dataBaseAccess.insertData(inOut);

                                dataBaseAccess.close();
                                if (addSucces) {
                                    Toast.makeText(MonitorizareMainActivity.this, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MonitorizareMainActivity.this, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
                                }
                                /*dataBaseAccess.open();
                                listOfNewData = dataBaseAccess.getAllPosts();
                                dataBaseAccess.close();*/
                                tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

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

    }

    private void showDialogInsertDataOutput() {
        final DataBaseAccess dataBaseAccess;

        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog_output, null, false);

        final EditText cheltuitInput = (EditText) formView.findViewById(R.id.outputText);
        final EditText dateInput = setDate(formView);


        if (fromExternalSource) {
            //Se verifica baza de date externa. Baza de date externa trebuie sa fie accesibila pentru prima lansare.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // Daca baza de date externa  este accesibila, atunci se lanseaza
            dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, externalDirectory);
        } else {
            // Daca nu este accesibila atunci se lanseaza din mapa Assets
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
                                if(listOfNewData.size() == 0)
                                {
                                    inOut.ID = 0;
                                }
                                else
                                {
                                    inOut.ID =  listOfNewData.get(listOfNewData.size()-1).ID + 1;
                                }

                                inOut.DATE = dateInput.getText().toString();
                                inOut.OUTPUT = Integer.parseInt(cheltuitInput.getText().toString());
                                inOut.INPUT = 0;
                                listOfNewData.add(inOut);
                                dataBaseAccess.open();
                                boolean addSucces = dataBaseAccess.insertData(inOut);

                                dataBaseAccess.close();
                                if (addSucces) {
                                    Toast.makeText(MonitorizareMainActivity.this, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MonitorizareMainActivity.this, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
                                }
                                /*dataBaseAccess.open();
                                listOfNewData = dataBaseAccess.getAllPosts();
                                dataBaseAccess.close();*/
                                tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

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

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem menuitem) {
        int id = menuitem.getItemId();

        switch (id) {
            case R.id.download_from_GDrive:
                try {

                        importFromDrive();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                break;
            case R.id.import_to_drive:
                saveToDrive();
                break;
            case R.id.exit_from_App:
                System.exit(1);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuitem);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void importFromDrive() throws IOException {
        //database path on the device


       /*final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();
        final OutputStream  localFile = new FileOutputStream(inFileName,true);*/
        final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();
        final FileOutputStream  localFile = new FileOutputStream(inFileName,true);

        //final int[] last = new int[1];
        driveResourceClient.query(query)
                .addOnSuccessListener(this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        DriveId driveid = metadata.get(0).getDriveId();
                        metadata.release();

                        Task<DriveContents> openFile = driveResourceClient.openFile(driveid.asDriveFile(), DriveFile.MODE_READ_ONLY);

                        openFile.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                DriveContents contents = task.getResult();
                                byte [] buffer = new byte[1024];
                                int read;

                                InputStream inputStream = contents.getInputStream();
                               // BufferedWriter br  = new BufferedWriter(localFile);

                                while ((read = inputStream.read(buffer)) != -1) {
                                    int flags =  Base64.DEFAULT;
                                    String byteArray = new String (Base64.encode(buffer,flags));
                                    localFile.write(Base64.decode(byteArray,flags),0,read);
                                             // br.newLine();
                                             // br.write(buffer.toString().toCharArray(),0,read);
                                  // localFile.write(buffer,0,read);
                                }
                                localFile.flush();
                                localFile.close();

                                inputStream.close();


                                final DataBaseAccess dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, null);
                                dataBaseAccess.open();
                                listOfNewData = dataBaseAccess.getAllPosts();
                                dataBaseAccess.close();


                                /*if(listOfNewData.size() >0) {

                                   InOut offlineData = listOfNewData.get(listOfNewData.size() - 1);

                                    for (InOut inOut : listOfData) {
                                        inOut.ID = inOut.ID + offlineData.ID;

                                        listOfNewData.add(inOut);
                                    }*/
                                    tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

                                /*}
                                else
                                {
                                    tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfData)));
                                }*/
                                tb.refreshDrawableState();
                                calculateSumTotal();

                                Toast.makeText(MonitorizareMainActivity.this, "Incarcarea datelor sa efectuat cu succes", Toast.LENGTH_SHORT).show();
                                Task<Void> discardTask = driveResourceClient.discardContents(contents);
                                return discardTask;
                            }

                        });


                    }
                })
       ;

    }

    private void showDialogEditData(final boolean shouldUpdate, final InOut inOut, final int position) {
        final LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        final View view;
        final DataBaseAccess dataBaseAccess;
        final String[] inputText = new String[1];
        final String[] outputText = new String[1];
        final EditText output;
        final EditText input;

        if (inOut.OUTPUT != 0) {
            view = layoutInflaterAndroid.inflate(R.layout.data_dialog_output, null);
            output = (EditText) view.findViewById(R.id.outputText);
            output.setText(String.valueOf(inOut.OUTPUT));
            output.setTextColor(Color.BLACK);
            output.setHintTextColor(Color.RED);

        } else {
            view = layoutInflaterAndroid.inflate(R.layout.data_dialog_input, null);
            input = view.findViewById(R.id.inputText);
            input.setText(String.valueOf(inOut.INPUT));
            input.setTextColor(Color.BLACK);
            input.setHintTextColor(Color.RED);


        }
        final EditText dateInput = setDate(view);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MonitorizareMainActivity.this);
        alertDialogBuilderUserInput.setView(view);


        if (fromExternalSource) {
            // Se verifica baza de date externa. Baza de date externa trebuie sa fie accesibila pentru prima lansare.
            String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/database";
            File dbFile = new File(externalDirectory, DATABASE_NAME);
            if (!dbFile.exists()) {
                return;
            }
            // Daca baza de date externa  este accesibila, atunci se lanseaza
            dataBaseAccess = DataBaseAccess.getInstance(this, externalDirectory);
        } else {
            // Daca nu este accesibila atunci se lanseaza din mapa Assets
            dataBaseAccess = DataBaseAccess.getInstance(this, null);
        }

        if (shouldUpdate && inOut != null) {
            dateInput.setText(String.valueOf(inOut.DATE));

            dateInput.setTextColor(Color.BLACK);
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                int id = view.getId();
                if (id == R.id.input) {
                    inputText[0] = ((EditText) (view.findViewById(R.id.inputText))).getText().toString();
                    if (TextUtils.isEmpty(inputText[0])) {
                        Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (id == R.id.output) {
                    outputText[0] = ((EditText) view.findViewById(R.id.outputText)).getText().toString();
                    if (TextUtils.isEmpty(outputText[0])) {
                        Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                alertDialog.dismiss();

                // verifica daca utilizatorul actualizeaza datele
                if (shouldUpdate && inOut != null) {
                    // actualizeaza datele
                    inOut.DATE = dateInput.getText().toString();
                    inOut.INPUT = (inputText[0] == null ? 0 : Integer.parseInt(inputText[0]));
                    inOut.OUTPUT = (outputText[0] == null ? 0 : Integer.parseInt(outputText[0]));

                    listOfNewData.get(position);
                    listOfNewData.set(position,inOut);

                    updateData(inOut, position);
                    calculateSumTotal();

                } else {
                    // creaza o inregistrare noua
                    dataBaseAccess.open();
                    dataBaseAccess.insertData(inOut);
                    dataBaseAccess.close();
                    calculateSumTotal();

                }
            }
        });
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @SuppressLint("RestrictedApi")
        @Override
        public void onReceive(Context context, Intent intent) {

            if (isOnline(context)) {

                // wifi and celular is enabled
                Toast.makeText(context, "Este Online", Toast.LENGTH_SHORT).show();
                SignIn();

            } else {
                // wifi and celular is disabled
                Toast.makeText(context, "Nu sunteti online", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean isOnline(Context context) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                //should check null because in airplane mode it will be null
                return (netInfo != null && netInfo.isConnected());
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }


    }


}
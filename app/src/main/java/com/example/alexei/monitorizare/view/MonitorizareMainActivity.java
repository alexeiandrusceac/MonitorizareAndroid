package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.File;
import java.io.FileInputStream;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;

import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

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
    /// Floating Action Buttons
    private FloatingActionButton buttonAdd, buttonInput, buttonOutput;
    private Animation button_open, button_close, button_forward, button_backward;
    private TextView textViewInput, textViewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);
        SignIn();
        noDataView = (TextView) findViewById(R.id.noDataView);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        inputTotalView = (TextView) findViewById(R.id.totalInput);
        outputTotalView = (TextView) findViewById(R.id.totalOutput);
        differenceTotalView = (TextView) findViewById(R.id.totalDifferenta);

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
        listOfData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            tableHelper = new TableHelper(this);
            tb = (TableView<String[]>) findViewById(R.id.tableView);
            tb.setColumnCount(4);
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

        button_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_open);
        button_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_close);
        button_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_forward);
        button_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_backward);

        buttonAdd.setOnClickListener(this);
        buttonInput.setOnClickListener(this);
        buttonOutput.setOnClickListener(this);
        // but.setOnClickListener(this);
               /* new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogInsertData();
                    }
                }*/
        // );
        showEmptyDataTextView();
        //connectToGoogleAPIClient();
        calculateSumTotal();
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

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
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
        unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                    // wifi is enabled
                    Toast.makeText(MonitorizareMainActivity.this, "Este Online", Toast.LENGTH_SHORT).show();
                    SignIn();//connectToGoogleAPIClient();
                } else {
                    // wifi is disabled
                    Toast.makeText(MonitorizareMainActivity.this, "Nu sunteti online", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @SuppressLint("RestrictedApi")
    private void SignIn() {
        Log.i(TAG, "Logheazate");
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

                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, "MonitorizareDB.db")).build();

                Task<MetadataBuffer> queryTask = driveResourceClient.query(query);

                queryTask.addOnSuccessListener(MonitorizareMainActivity.this, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        for (Metadata m : metadata) {
                            driveResource = m.getDriveId().asDriveResource();
                            driveResourceClient.delete(driveResource);
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

                }
                break;
            case REQUEST_CODE_CREATOR:
                // Se apeleaza dupa ce se salveaza in Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Copia sa salvat cu succes.");
                    Toast.makeText(MonitorizareMainActivity.this, "Copia sa incarcat cu succes!", Toast.LENGTH_SHORT).show();
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
        for (InOut inOut : listOfData) {
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

                                inOut.DATE = dateInput.getText().toString();
                                inOut.INPUT = Integer.parseInt(primitInput.getText().toString());
                                inOut.OUTPUT = 0;
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

                                inOut.DATE = dateInput.getText().toString();
                                inOut.OUTPUT = Integer.parseInt(cheltuitInput.getText().toString());
                                inOut.INPUT = 0;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuitem) {
        int id = menuitem.getItemId();

        switch (id) {
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
            outputText[0] = output.getText().toString();
        } else {
            view = layoutInflaterAndroid.inflate(R.layout.data_dialog_input, null);
            input = view.findViewById(R.id.inputText);
            input.setText(String.valueOf(inOut.INPUT));
            input.setTextColor(Color.BLACK);
            input.setHintTextColor(Color.RED);
            inputText[0] = input.getText().toString();

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


///////////////////////////////////Verifica ///////////////////////////////////////////////////////
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                //   String idView= String.valueOf(view.getId());// Show toast message when no text is entered

                int id = view.getId();
                if (id == R.id.input)
                {
                    if (TextUtils.isEmpty(inputText[0])) {
                        Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (id == R.id.output) {
                    if (TextUtils.isEmpty(outputText[0])) {
                        Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    alertDialog.dismiss();
                }


            // verifica daca utilizatorul actualizeaza datele
                if(shouldUpdate && inOut != null)

                {
                    // actualizeaza datele
                    inOut.DATE = dateInput.getText().toString();
                    inOut.INPUT = (inputText[0].equals(null) ? 0 : Integer.parseInt(inputText[0]));
                    if (outputText[0].equals(null))
                    {
                       inOut.OUTPUT = 0;
                    }
                    else
                    {
                        inOut.OUTPUT = Integer.parseInt(outputText[0]);
                    }
                //inOut.OUTPUT = (outputText[0].equals(null)? 0 : Integer.parseInt(outputText[0]));

                updateData(inOut, position);
                calculateSumTotal();

            } else
            {
                // creaza o inregistrare noua
                dataBaseAccess.open();
                dataBaseAccess.insertData(inOut);
                dataBaseAccess.close();
                calculateSumTotal();

            }
        }
    });
}

}
package com.example.alexei.monitorizare.view;

//import android.content.Intent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alexei.monitorizare.R;
import com.example.alexei.monitorizare.database.DataBaseAccess;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;
//import com.example.alexei.monitorizare.firebaseInteraction.ForceUpdateChecker;

import java.io.BufferedInputStream;
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
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;

import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


import static com.example.alexei.monitorizare.database.DataBaseHelper.DATABASE_NAME;


// Google Play Services

/*import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
*/
//com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
public class MonitorizareMainActivity extends AppCompatActivity implements /*ForceUpdateChecker.OnUpdateNeededListener,*/ View.OnClickListener {
    /* private DriveResourceClient driveResourceClient;
     private DriveClient driveClient;
     private DriveResource driveResource;
     private GoogleSignInClient mGoogleSignIn;

     private static final String TAG = "Google Drive Activity";
     private static final int REQUEST_CODE_SIGNIN = 0;
     private static final int REQUEST_CODE_DATA = 1;
     private static final int REQUEST_CODE_CREATOR = 2;*/
    private boolean buttonOpen = false;
    private TextView inputTotalView;
    private TextView outputTotalView;
    private TextView differenceTotalView;
    private List<InOut> listOfNewData = new ArrayList<>();
    DataBaseAccess dataBaseAccess;
    private boolean outputTrue;
    private boolean inputTrue;
    private TextView noDataView;
    private TableView<String[]> tb;
    private TableHelper tableHelper;
    private Menu mainMenu;
    //private ProgressBar progressBar;
    //private MenuItem getFromDrive;
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

    //private BroadcastReceiver broadcastReceiver;
    // private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitorizare_main);


        /*query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.MIME_TYPE, "application/db"),
                Filters.eq(SearchableField.TITLE, "MonitorizareDB.db"))).build();*/
        noDataView = (TextView) findViewById(R.id.noDataView);

        inputTotalView = (TextView) findViewById(R.id.totalInput);

        outputTotalView = (TextView) findViewById(R.id.totalOutput);
        differenceTotalView = (TextView) findViewById(R.id.totalDifferenta);
        /*progressBar = (ProgressBar) findViewById(R.id.progressBar);
        broadcastReceiver = new MyBroadcastReceiver();*/
      /*  registerNetworkBroadcast();
        updateConfig();
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
*/
        buttonAdd = (FloatingActionButton) findViewById(R.id.buttonFloating);
        buttonInput = (FloatingActionButton) findViewById(R.id.buttonFloatingPrimit);
        buttonOutput = (FloatingActionButton) findViewById(R.id.buttonFloatingCheltuit);

        textViewInput = (TextView) findViewById(R.id.inputTextFAB);
        textViewOutput = (TextView) findViewById(R.id.outputTextFAB);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkExternalStorage();

        dataBaseAccess.open();
        listOfNewData = dataBaseAccess.getAllPosts();
        dataBaseAccess.close();

        if (fromExternalSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            tableHelper = new TableHelper(this);
            tb = (TableView<String[]>) findViewById(R.id.tableView);
            tb.setColumnCount(3);
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
    public void onBackPressed() {
        if (buttonOpen) {
            buttonAdd.startAnimation(button_backward);
            buttonInput.startAnimation(button_close);
            buttonOutput.startAnimation(button_close);
            buttonInput.setClickable(false);
            buttonOutput.setClickable(false);
            buttonOpen = false;
            textViewInput.setVisibility(View.GONE);
            textViewOutput.setVisibility(View.GONE);
        }
        else {

            new AlertDialog.Builder(MonitorizareMainActivity.this)
                    .setTitle("Sunteti siguri")
                    .setMessage("Sunteti sigur ca doriti sa iesiti din aplicatie?")
                    .setPositiveButton("Da",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(1);
                }
            })
                    .setNegativeButton("Nu",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }
    }

    /*
            private void updateConfig() {

                final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

                FirebaseRemoteConfigSettings firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings.Builder().build();
                firebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);

                Map<String,Object> remoteConfigDefaults= new HashMap<>();
                remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED,false);
                remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_CURR_VERSION,"1.0.0");
                remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_STORE_URL,"https://github.com/alexeiandrusceac/MonitorizareAndroid/releases/latest");

                firebaseRemoteConfig.setDefaults(remoteConfigDefaults);
                long cacheExpiration = 3600;
                //onDevelopment make cacheExpiration as zero second;
                if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                    cacheExpiration = 0;
                }
                firebaseRemoteConfig.fetch(60)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Log.d(TAG,"remote config is fetched");
                                    firebaseRemoteConfig.activateFetched();
                                }
                            }
                        });

            }
        */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterNetworkChanges();
    }

    private void checkExternalStorage() {
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
    }

    /*  private void registerNetworkBroadcast() {

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
         final MenuItem  downloadFromDrive = mainMenu.findItem(R.id.download_from_GDrive);
          getFromDrive = downloadFromDrive;
          MenuItem saveToDrive = mainMenu.findItem(R.id.import_to_drive);
          driveResourceClient.query(query)
                  .addOnSuccessListener(this, new OnSuccessListener<MetadataBuffer>() {
                      @Override
                      public void onSuccess(MetadataBuffer metadata) {
                          if (metadata.getCount() > 0) {
                              downloadFromDrive.setVisible(true);
                              downloadFromDrive.setEnabled(true);
                              if(backupDone == false)
                              {
                                  buttonAdd.setVisibility(View.INVISIBLE);
                                  downloadFromDrive.setVisible(true);
                              }
                              else
                              {
                                  buttonAdd.setVisibility(View.VISIBLE);
                                  downloadFromDrive.setVisible(false);
                              }
                          } else {
                              downloadFromDrive.setVisible(false);
                                  buttonAdd.setVisibility(View.VISIBLE);
                          }
                      }
                  });
          saveToDrive.setVisible(true);
          }
  */
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
        } else{
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
        //try {
        //unregisterReceiver(broadcastReceiver);
       /* }
        catch (IOException ex)
        {

        }*/
        super.onPause();

    }


   /* @Override
    protected void onResume() {
       // registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        super.onResume();


    }
*/
    /*GOOGLE DRIVE API ACTIONS*/
        /*@SuppressLint("RestrictedApi")
        private void SignIn () {
            Log.i(TAG, "Logheaza - te");
            mGoogleSignIn = buildGoogleSignInClient();
            startActivityForResult(mGoogleSignIn.getSignInIntent(), REQUEST_CODE_SIGNIN);
        }


        @NonNull
        @SuppressLint("RestrictedApi")
        private GoogleSignInClient buildGoogleSignInClient () {
            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(Drive.SCOPE_FILE).build();

            return GoogleSignIn.getClient(this, signInOptions);
        }


        public void saveToDrive () {
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
*/

    private void showEmptyDataTextView() {

        if (tb.getDataAdapter().getCount() > 0) {
            noDataView.setVisibility(View.GONE);
        } else {
            noDataView.setVisibility(View.VISIBLE);
        }
    }

    private void showDialogEditDelete(final int position) {

        CharSequence colors[] = new CharSequence[]{"Editare", "Stergere"};

        new AlertDialog.Builder(MonitorizareMainActivity.this)
                .setTitle("Alegeti optiunea")
                .setItems(colors,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    showDialogEditData(true, listOfNewData.get(position), position);
                                } else {
                                    deleteData(listOfNewData.get(position), position);
                                }
                            }
                        }).show();

    }

    /*
            @Override
            protected void onActivityResult ( final int requestCode, final int resultCode,
            final Intent data){
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
                            if (driveResourceClient != null) {
                                enableImportSaveIfExistDB();
                            }
                        }
                        break;

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
    */



    private void updateData(InOut inOut, int position) {

        checkExternalStorage();

        dataBaseAccess.open();

        // actualizarea inregistrarii in baza de date
        dataBaseAccess.updateData(MonitorizareMainActivity.this, inOut);
        dataBaseAccess.close();

        // se reincarca lista de date
        listOfNewData.set(position, inOut);

        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfNewData)));
        tb.refreshDrawableState();
        calculateSumTotal();

    }

    private void deleteData(InOut inOut, int position) {

        checkExternalStorage();
        // stergerea inregistrarii din baza de date
        dataBaseAccess.open();
        dataBaseAccess.deleteData(MonitorizareMainActivity.this, inOut);

        dataBaseAccess.close();

        listOfNewData.remove(position);

        tb.setDataAdapter(new SimpleTableDataAdapter(this, tableHelper.getData(listOfNewData)));
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
        inputTrue = true;
        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog_input, null, false);

        final EditText primitInput = (EditText) formView.findViewById(R.id.inputText);

        final EditText dateInput = setDate(formView);

        checkExternalStorage();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MonitorizareMainActivity.this)
                .setView(formView)
                .setCancelable(false)
                .setPositiveButton("Adauga",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setNegativeButton("Anuleaza",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                final InOut inOut = new InOut();


                if (TextUtils.isEmpty(dateInput.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti data cind ati primit!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(primitInput.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                    return;
                }
                alertDialog.dismiss();

                // verifica daca utilizatorul actualizeaza datele
                if (inOut != null) {
                    // actualizeaza datele
                    inOut.DATE = dateInput.getText().toString();
                    inOut.INPUT = Integer.parseInt(primitInput.getText().toString());
                    inOut.OUTPUT = 0;


                    listOfNewData.add(inOut);

                    dataBaseAccess.open();
                    dataBaseAccess.insertData(MonitorizareMainActivity.this, inOut);

                    dataBaseAccess.close();

                    tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

                    showEmptyDataTextView();
                    calculateSumTotal();


                }
            }
        });


    }

    private void showDialogInsertDataOutput() {
        outputTrue = true;
        LayoutInflater layoutInflater = (LayoutInflater) MonitorizareMainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formView = layoutInflater.inflate(R.layout.data_dialog_output, null, false);

        final EditText cheltuitInput = (EditText) formView.findViewById(R.id.outputText);
        final EditText dateOutput = setDate(formView);

        checkExternalStorage();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MonitorizareMainActivity.this)
                .setView(formView)
                .setCancelable(false)
                .setPositiveButton("Adauga",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .setNegativeButton("Anuleaza",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final InOut inOut = new InOut();

                if (TextUtils.isEmpty(dateOutput.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti data cind ati cheltuit!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(cheltuitInput.getText().toString())) {
                    Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                    return;
                }
                alertDialog.dismiss();

                // verifica daca utilizatorul actualizeaza datele
                if (inOut != null) {
                    // actualizeaza datele
                    inOut.DATE = dateOutput.getText().toString();
                    inOut.INPUT = 0;
                    inOut.OUTPUT = Integer.parseInt(cheltuitInput.getText().toString());

                    listOfNewData.add(inOut);

                    dataBaseAccess.open();
                    dataBaseAccess.insertData(MonitorizareMainActivity.this, inOut);

                    dataBaseAccess.close();

                    tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

                    showEmptyDataTextView();
                    calculateSumTotal();


                }
            }

        });
    }

    public EditText setDate(View dialogView) {
        final EditText dateinput = (EditText) dialogView.findViewById(R.id.dateText);
        dateinput.setInputType(InputType.TYPE_NULL);
        dateinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH)+1;
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
                /*case R.id.download_from_GDrive:
                    try {
                        importFromDrive();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.import_to_drive:
                    saveToDrive();
                    break;*/
            case R.id.exit_from_App:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuitem);
    }

    /*
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.O)
            private void importFromDrive () throws IOException {
                //database path on the device
                final String inFileName = getApplicationContext().getDatabasePath(DATABASE_NAME).toString();
                final OutputStream localFile = new FileOutputStream(inFileName);
                final DataBaseAccess dataBaseAccess = DataBaseAccess.getInstance(MonitorizareMainActivity.this, null);
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
                                        byte[] bufferdrive = new byte[1024];
                                        InputStream inputStream = contents.getInputStream();
                                        int line;

                                        while ((line = inputStream.read(bufferdrive)) != -1) {
                                            localFile.write(bufferdrive, 0, line);
                                        }

                                        localFile.flush();
                                        localFile.close();

                                        inputStream.close();
                                        dataBaseAccess.open();
                                        listOfNewData = dataBaseAccess.getAllPosts();
                                        dataBaseAccess.close();

                                        backupDone = true;
                                        enableImportSaveIfExistDB();

                                        tb.setDataAdapter(new SimpleTableDataAdapter(MonitorizareMainActivity.this, tableHelper.getData(listOfNewData)));

                                        tb.refreshDrawableState();

                                        calculateSumTotal();

                                        Toast.makeText(MonitorizareMainActivity.this, "Incarcarea datelor sa efectuat cu succes", Toast.LENGTH_SHORT).show();
                                        Task<Void> discardTask = driveResourceClient.discardContents(contents);
                                        return discardTask;
                                    }

                                });

                            }
                        });
            }
    */
    private void showDialogEditData(final boolean shouldUpdate, final InOut inOut, final int position) {
        final LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        final View view;

        final String[] inputText = new String[1];
        final String[] outputText = new String[1];
        final EditText output;
        final EditText input;
        final EditText dateInputSet;
        final EditText dateOutputSet;
        final EditText dateInput,dateOutput;

        if (inOut.OUTPUT != 0) {
            view = layoutInflaterAndroid.inflate(R.layout.data_dialog_output, null);
            dateOutputSet = view.findViewById(R.id.dateText);
            output = view.findViewById(R.id.outputText);
            output.setText(String.valueOf(inOut.OUTPUT));
            output.setTextColor(Color.BLACK);
            output.setHintTextColor(Color.RED);
            dateOutputSet.setText(String.valueOf(inOut.DATE));
            dateOutputSet.setTextColor(Color.BLACK);
            dateOutputSet.setHintTextColor(Color.RED);
            dateOutput = setDate(view);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MonitorizareMainActivity.this);
            alertDialogBuilderUserInput.setView(view);

            checkExternalStorage();

            if (shouldUpdate && inOut != null) {

                dateOutput.setText(String.valueOf(inOut.DATE));

                dateOutput.setTextColor(Color.BLACK);
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

                    //int id = view.getId();
                    //if (id == R.id.input) {
                       /* inputText[0] = ((EditText) (view.findViewById(R.id.inputText))).getText().toString();
                        if (TextUtils.isEmpty(inputText[0])) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (TextUtils.isEmpty(dateInput.getText().toString())) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti data primirii!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (id == R.id.output) {*/
                        outputText[0] = ((EditText) view.findViewById(R.id.outputText)).getText().toString();
                        if (TextUtils.isEmpty(outputText[0])) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati cheltuit!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (TextUtils.isEmpty(dateOutput.getText().toString())) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti data ati cheltuit!", Toast.LENGTH_SHORT).show();
                            return;

                        }
                    //}
                    alertDialog.dismiss();

                    // verifica daca utilizatorul actualizeaza datele
                    if (shouldUpdate && inOut != null) {
                        // actualizeaza datele
                        inOut.DATE = dateOutput.getText().toString();
                        inOut.INPUT = 0;//(inputText[0] == null ? 0 : Integer.parseInt(inputText[0]));
                        inOut.OUTPUT = (outputText[0] == null ? 0 : Integer.parseInt(outputText[0]));


                        updateData(listOfNewData.get(position), position);
                        calculateSumTotal();

                    }
                }

            });

        } else {
            view = layoutInflaterAndroid.inflate(R.layout.data_dialog_input, null);
            input = view.findViewById(R.id.inputText);
            input.setText(String.valueOf(inOut.INPUT));
            input.setTextColor(Color.BLACK);
            input.setHintTextColor(Color.RED);
            dateInputSet = view.findViewById(R.id.dateText);
            dateInputSet.setText(String.valueOf(inOut.DATE));
            dateInputSet.setTextColor(Color.BLACK);
            dateInputSet.setHintTextColor(Color.RED);
            dateInput = setDate(view);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MonitorizareMainActivity.this);
            alertDialogBuilderUserInput.setView(view);

            checkExternalStorage();

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

                   inputText[0] = ((EditText) (view.findViewById(R.id.inputText))).getText().toString();
                        if (TextUtils.isEmpty(inputText[0])) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti cit ati primit!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (TextUtils.isEmpty(dateInput.getText().toString())) {
                            Toast.makeText(MonitorizareMainActivity.this, "Introduceti data primirii!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    alertDialog.dismiss();

                    // verifica daca utilizatorul actualizeaza datele
                    if (shouldUpdate && inOut != null) {
                        // actualizeaza datele
                        inOut.DATE = dateInput.getText().toString();
                        inOut.INPUT = (inputText[0] == null ? 0 : Integer.parseInt(inputText[0]));
                        inOut.OUTPUT = 0;

                        updateData(listOfNewData.get(position), position);
                        calculateSumTotal();

                    }
                }

            });
        }



    }
/*
        @Override
        public void onUpdateNeeded ( final String updateUrl){
            new AlertDialog.Builder(this).setTitle("O noua versiune este valabila")
                    .setMessage("Va rugam, actualizati aplicatia la o versiune mai noua ")
                    .setPositiveButton("Actualizare", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            redirectStore(updateUrl);
                        }
                    })
                    .setNegativeButton("Nu,multumesc", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();


        }
        private void redirectStore (String updateUrl)
        {
            String PATH = "/sdcard/";
            try {
                URL url  = new URL(updateUrl);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Accept","application/android.com.app");
                connection.setRequestMethod("GET");
//                connection.connect();
               // int fileLength = connection.getResponseCode();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(PATH);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                   // progressBar.setVisibility(View.VISIBLE);
                    //progressBar.setProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(PATH + "monitorizare.apk"));
                intent.setType("application/android.com.app");

                startActivity(intent);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        class MyBroadcastReceiver extends BroadcastReceiver {

            @SuppressLint("RestrictedApi")
            @Override
            public void onReceive(Context context, Intent intent) {

                if (isOnline(context)) {


                    // wifiul si datele mobile sunt activate
                    Toast.makeText(context, "Este Online", Toast.LENGTH_SHORT).show();

                    SignIn();


                } else {
                    // wifiul si datele mobile sunt dezactivate
                    if (driveResourceClient != null) {
                        enableImportSaveIfExistDB();
                    }
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
        }*/
    }

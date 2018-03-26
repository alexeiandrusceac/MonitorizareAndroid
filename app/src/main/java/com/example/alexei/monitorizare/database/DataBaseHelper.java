package com.example.alexei.monitorizare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.validation.Schema;

/**
 * Created by alexei.andrusceac on 19.03.2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

private static String DB_PATH = "/data/data/com.example.alexei.monitorizare/databases/";
    private  static  String DATABASE_NAME="MonitorizareDB";
    private static final int SCHEMA = 1;
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    /*private static final String INOUTTABLE = "InOutTable";

    private static final String ID = "input_ID";

    private static final String DATE = "Date";
    private static final String INPUT = "Input";
    private static final String OUTPUT = "Output";
    private static final String DIFFERENCE = "Difference";
    private static final String INPUTTOTAL = "InputTotal";
    private static final String OUTPUTTOTAL = "OutputTotal";
*/
   /* public static synchronized DataBaseHelper getsInstance(Context context)
    {
        if(sInstance == null)
        {
            sInstance = new DataBaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
*/
   public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
   this.myContext = context;
   }

   public void createDataBase()throws IOException{
       boolean dbExist = checkDataBase();
       if(dbExist)
       {

       }
       else
       {
           this.getReadableDatabase();
           try
           {
               copyDataBase();

           }
           catch(IOException ex)
           {
               throw  new Error("Eroare in copierea bazei de date");
           }
       }
   }

   private boolean checkDataBase()
   {
       SQLiteDatabase checkDB = null;
       try
       {
           String myPath= DB_PATH + DATABASE_NAME;
           checkDB = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READONLY);

       }
       catch(SQLException ex)
       {
           ex.getMessage();
       }
       if(checkDB != null)
       {
           checkDB.close();
       }
       return checkDB !=null ?true:false;
   }
   private void copyDataBase()throws IOException
   {
       InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

       // Path to the just created empty db
       String outFileName = DB_PATH + DATABASE_NAME;

       //Open the empty db as the output stream
       OutputStream myOutput = new FileOutputStream(outFileName);

       //transfer bytes from the inputfile to the outputfile
       byte[] buffer = new byte[1024];
       int length;
       while ((length = myInput.read(buffer))>0){
           myOutput.write(buffer, 0, length);
       }

       //Close the streams
       myOutput.flush();
       myOutput.close();
       myInput.close();
   }
    public void openDataBase() throws SQLException{

        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}
/*
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
     super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_DATA_TABLE = "create table if not exists " + INOUTTABLE + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE + " text not null, "
                + INPUT + " int, "
                + OUTPUT + " int, "
                + DIFFERENCE + "text, "
                + INPUTTOTAL + " text, "
                + OUTPUTTOTAL + " text)";
        db.execSQL(CREATE_DATA_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + INOUTTABLE);

            // Create tables again
            onCreate(db);
        }
    }
*/



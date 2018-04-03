package com.example.alexei.monitorizare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;
import com.example.alexei.monitorizare.view.MonitorizareMainActivity;
import com.example.mylibrary.ExternalSQLiteHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexei.andrusceac on 26.03.2018.
 */

public class DataBaseAccess {
    private ExternalSQLiteHelper openHelper;
    private SQLiteDatabase database;
    private static DataBaseAccess instance;

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context
     * @param sourceDirectory
     */
    private DataBaseAccess(Context context, String sourceDirectory) {
        if (sourceDirectory == null) {
            this.openHelper = new DataBaseHelper(context);
        } else {
            this.openHelper = new DataBaseHelper(context, sourceDirectory);
        }
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context         the Context
     * @param sourceDirectory optional external directory
     * @return the instance of DabaseAccess
     */
    public static DataBaseAccess getInstance(Context context, String sourceDirectory) {
        if (instance == null) {
            instance = new DataBaseAccess(context, sourceDirectory);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Read all quotes from the database.
     *
     * @return a List of quotes
     */
    public List<InOut> getAllPosts() {
        List<InOut> list = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM InOutTable", null);
        if (cursor.moveToFirst()) {
            do {
                InOut newdata = new InOut();
                newdata.ID = cursor.getInt(0);
                newdata.DATE = cursor.getString(1);
                newdata.INPUT = cursor.getInt(2);
                newdata.OUTPUT = cursor.getInt(3);
                newdata.DIFFERENCE = cursor.getInt(4);

                list.add(newdata);
            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return list;
    }

    public boolean updateData(InOut inOut) {
        ContentValues values = new ContentValues();
        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);
        values.put("Difference", inOut.DIFFERENCE);

         database.update("InOutTable", values, "input_ID = ?", new String[]{String.valueOf(inOut.ID)});
         database.close();
        if(database == null)
        {return false;}
        else
        {return true;}
    }

    public boolean deleteData(InOut inOut) {
        database.delete("InOutTable", "input_ID = ?", new String[]{String.valueOf(inOut.ID)});
        database.close();
        if (database != null) {
            return true;
        } else {
            return false;
        }
    }


    public boolean insertData(InOut inOut) {
        ContentValues values = new ContentValues();

        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);
        values.put("Difference", inOut.DIFFERENCE);


        database.insert("InOutTable", null, values);
        database.close();
        if (database == null) {
            return false;
        } else {
            return true;
        }
    }

    /*public void backupDataBase(String outFileName)
    {
        final String inFileName = database.getPath().toString();
        try
        {
            File dbfile = new File(inFileName);
            FileInputStream fileInputStream = new FileInputStream(dbfile);
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while((length = fileInputStream.read(buffer)) > 0)
                outputStream.write(buffer,0,length);
            outputStream.flush();
            outputStream.close();
            fileInputStream.close();
            Toast.makeText(MonitorizareMainActivity.this, " Copierea bazei de date sa executat cu succes",Toast.LENGTH_SHORT).show();

        }

    }*/

}
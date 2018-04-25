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

                list.add(newdata);
            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return list;
    }

    public void updateData(Context context, InOut inOut) {
        ContentValues values = new ContentValues();
        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);

        database.update("InOutTable", values, "ID = ?", new String[]{String.valueOf(inOut.ID)});
        database.close();
        if (database != null) {
            Toast.makeText(context, "Informatia sa actualizat cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Nu sa actualizat informatia", Toast.LENGTH_SHORT).show();

        }
    }

    public void deleteData(Context context, InOut inOut) {
        database.delete("InOutTable", "ID = ?", new String[]{String.valueOf(inOut.ID)});
        database.close();
        if (database != null) {
            Toast.makeText(context, "Informatia sa sters cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Nu sa sters informatia", Toast.LENGTH_SHORT).show();
        }
    }

    public void insertData(Context context, InOut inOut) {
        ContentValues values = new ContentValues();

        //values.put("ID", inOut.ID);
        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);

        database.insert("InOutTable", null, values);

        database.close();
        if (database != null) {
            Toast.makeText(context, "Informatia sa adaugat cu succes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Nu sa adaugat informatia", Toast.LENGTH_SHORT).show();
        }
    }

}
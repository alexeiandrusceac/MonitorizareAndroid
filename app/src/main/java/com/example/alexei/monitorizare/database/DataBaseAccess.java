package com.example.alexei.monitorizare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;
import com.example.mylibrary.ExternalSQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexei.andrusceac on 26.03.2018.
 */

public class DataBaseAccess {
    private ExternalSQLiteHelper openHelper;
    private SQLiteDatabase database;
    private static DataBaseAccess instance;
    /*private static final String INOUTTABLE = "InOutTable";

    private static final String ID = "input_ID";

    private static final String DATE = "Date";
    private static final String INPUT = "Input";
    private static final String OUTPUT = "Output";
    private static final String DIFFERENCE = "Difference";
    private static final String INPUTTOTAL = "InputTotal";
    private static final String OUTPUTTOTAL = "OutputTotal";
*/

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
                //newdata.INPUTTOTAL = cursor.getInt(5);
                //newdata.OUTPUTTOTAL = cursor.getInt(6);
                list.add(newdata);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public int updateData(InOut inOut) {
        ContentValues values = new ContentValues();
        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);
        values.put("Difference", inOut.DIFFERENCE);
        /*values.put(INPUTTOTAL, inOut.INPUTTOTAL);
        values.put(OUTPUTTOTAL, inOut.OUTPUTTOTAL);*/
        return database.update("InOutTable", values, "input_ID = ?", new String[]{String.valueOf(inOut.ID)});

    }

    public void deleteData(InOut inOut) {
        database.delete("InOutTable", "input_ID = ?", new String[]{String.valueOf(inOut.ID)});
        database.close();
    }

    public boolean insertData(InOut inOut) {
        ContentValues values = new ContentValues();
        //values.put(ID, inputID);
        values.put("Date", inOut.DATE);
        values.put("Input", inOut.INPUT);
        values.put("Output", inOut.OUTPUT);
        values.put("Difference", inOut.DIFFERENCE);
        /*values.put(INPUTTOTAL, inOut.INPUTTOTAL);
        values.put(OUTPUTTOTAL, inOut.OUTPUTTOTAL);*/


        database.insert("InOutTable", null, values);


        database.close();
        if (database == null) {
            return false;
        } else {
            return true;
        }
    }

    public InOut getInsertedRow() {
        InOut newdata = new InOut();

        Cursor cursor = database.rawQuery("SELECT  *  FROM InOutTable ORDER BY input_ID DESC LIMIT 1;", null);
        if (cursor.moveToFirst()) {

            newdata.ID = cursor.getInt(0);
            newdata.DATE = cursor.getString(1);
            newdata.INPUT = cursor.getInt(2);
            newdata.OUTPUT = cursor.getInt(3);
            newdata.DIFFERENCE = cursor.getInt(4);
            //newdata.INPUTTOTAL = cursor.getInt(5);
            //newdata.OUTPUTTOTAL = cursor.getInt(6);

        }
        cursor.close();
        return newdata;

    }
}
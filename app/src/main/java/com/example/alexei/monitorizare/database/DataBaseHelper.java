package com.example.alexei.monitorizare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;

/**
 * Created by alexei.andrusceac on 19.03.2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private  static final String DATABASE_NAME="MonitorizareDB";
    ///SCHEMA este versiunea BAZEI DE DATE
    //// initial este prima versiune. 1.
    private static final int SCHEMA = 1;

    public DataBaseHelper(Context context)
    {
        super(context,DATABASE_NAME,null, SCHEMA);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL(InOut.INOUTTABLE);
    }
    public long insertData(String dateTime,int primire,int cheltuire, int diferenta,int totalPrimire,int totalCheltuire)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InOut.DATE, dateTime);
        values.put(InOut.INPUT, primire);
        values.put(InOut.OUTPUT, cheltuire);
        values.put(InOut.DIFFERENCE, diferenta);
        values.put(InOut.INPUTTOTAL, totalPrimire);
        values.put(InOut.OUTPUTTOTAL, totalCheltuire);

        long id = database.insert(InOut.INOUTTABLE,null,values);
        database.close();
        return id;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + InOut.INOUTTABLE);

        // Create tables again
        onCreate(db);
    }
    public List<InOut> getData() {
        List<InOut> listOfData = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT * FROM " + InOut.INOUTTABLE + " ORDER BY "+InOut.ID +" ASC";
        Cursor c = database.rawQuery(query, null);
        if(c.moveToFirst())
        {
            do{
                InOut data = new InOut ();
                data.setId(c.getInt(c.getColumnIndex(InOut.ID)));
                data.setDate(c.getString(c.getColumnIndex(InOut.DATE)));
                data.setInput(c.getInt(c.getColumnIndex(InOut.INPUT)));
                data.setOutput(c.getInt(c.getColumnIndex(InOut.OUTPUT)));
                data.setDifference(c.getInt(c.getColumnIndex(InOut.DIFFERENCE)));
                data.setInputTotal(c.getInt(c.getColumnIndex(InOut.INPUTTOTAL)));
                data.setOutputTotal(c.getInt(c.getColumnIndex(InOut.OUTPUTTOTAL)));
                listOfData.add(data);
            }
            while(c !=null);
        }

        database.close();
        return listOfData;
    }
}

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

    private  static  String DATABASE_NAME="MonitorizareDB.db";
    private static   String DATABASE_PATH = "";
    ///SCHEMA este versiunea BAZEI DE DATE
    //// initial este prima versiune. 1.
    private static final int SCHEMA = 1;

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate =false;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        if (Build.VERSION.SDK_INT >= 17)
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";

         else

            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
            this.mContext = context;

        copyDataBase();
        this.getReadableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
       //db.execSQL(InOut.INOUTTABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + InOut.INOUTTABLE);

        // Create tables again
        onCreate(db);
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

    public List<InOut> getData() {
        List<InOut> listOfData = new ArrayList<>();

        String query = "SELECT * FROM " + InOut.INOUTTABLE + " ORDER BY "+InOut.ID +" ASC";
        SQLiteDatabase database = this.getWritableDatabase();
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
            while(c.moveToNext());
        }

        database.close();
        return listOfData;
    }

    public InOut getRow(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database .query(InOut.INOUTTABLE,
                new String[]{InOut.ID, InOut.DATE, InOut.INPUT,InOut.OUTPUT,InOut.DIFFERENCE, InOut.INPUTTOTAL,InOut.OUTPUTTOTAL},
                InOut.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        InOut data = new InOut(
                cursor.getInt(cursor.getColumnIndex(InOut.ID)),
                cursor.getString(cursor.getColumnIndex(InOut.DATE)),
                cursor.getInt(cursor.getColumnIndex(InOut.INPUT)),
                cursor.getInt(cursor.getColumnIndex(InOut.OUTPUT)),
                cursor.getInt(cursor.getColumnIndex(InOut.DIFFERENCE)),
                cursor.getInt(cursor.getColumnIndex(InOut.INPUTTOTAL)),
                cursor.getInt(cursor.getColumnIndex(InOut.OUTPUTTOTAL))
                );

        // close the db connection
        cursor.close();

        return data;
    }

    public int updateRow(InOut row)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InOut.DATE, row.DATE);
        values.put(InOut.INPUT, row.INPUT);
        values.put(InOut.OUTPUT, row.OUTPUT);
        values.put(InOut.DIFFERENCE, row.DIFFERENCE);
        values.put(InOut.INPUTTOTAL, row.INPUTTOTAL);
        values.put(InOut.OUTPUTTOTAL, row.OUTPUTTOTAL);
        return database.update(InOut.INOUTTABLE,values,InOut.ID + " = ?",new String[]{String.valueOf(row.getId())});
    }
    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }
    private boolean checkDataBase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }
    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }
    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }
    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }
    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

}

package com.example.alexei.monitorizare.view;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.alexei.monitorizare.database.DataBaseHelper;
import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexei on 3/24/2018.
 */

public class TableDataController extends DataBaseHelper {

    private static final String INOUTTABLE = "InOutTable";

    private static final String ID = "input_ID";

    private static final String DATE = "Date";
    private static final String INPUT = "Input";
    private static final String OUTPUT = "Output";
    private static final String DIFFERENCE = "Difference";
    private static final String INPUTTOTAL = "InputTotal";
    private static final String OUTPUTTOTAL = "OutputTotal";



    public TableDataController(Context context) {
        super(context);
    }

    public boolean insertData(InOut inOut) {
        ContentValues values = new ContentValues();
        //values.put(ID, inputID);
        values.put(DATE, inOut.DATE);
        values.put(INPUT, inOut.INPUT);
        values.put(OUTPUT, inOut.OUTPUT);
        values.put(DIFFERENCE, inOut.DIFFERENCE);
        values.put(INPUTTOTAL, inOut.INPUTTOTAL);
        values.put(OUTPUTTOTAL, inOut.OUTPUTTOTAL);

        SQLiteDatabase database = getWritableDatabase();

       long id = database.insert(INOUTTABLE, null, values) ;
        database.close();
if(id == -1)
{
    return false;
}
else
{
    return true;
}


}
    public int count() {

        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "SELECT * FROM"+INOUTTABLE;
        int recordCount = db.rawQuery(sql, null).getCount();
        db.close();

        return recordCount;

    }
    public List<InOut> getAllPosts() {
        List<InOut> data = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s",INOUTTABLE);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                InOut newdata = new InOut();
                newdata.DATE = cursor.getString(cursor.getColumnIndex(DATE));
                newdata.INPUT = cursor.getInt(cursor.getColumnIndex(INPUT));
                newdata.OUTPUT = cursor.getInt(cursor.getColumnIndex(OUTPUT));
                newdata.DIFFERENCE = cursor.getInt(cursor.getColumnIndex(DIFFERENCE));
                newdata.INPUTTOTAL = cursor.getInt(cursor.getColumnIndex(INPUTTOTAL));
                newdata.OUTPUTTOTAL = cursor.getInt(cursor.getColumnIndex(OUTPUTTOTAL));
                data.add(newdata);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return data;
    }


/*
    public int updateRow(InOut row)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, DATE);
        values.put(INPUT, INPUT);
        values.put(OUTPUT, OUTPUT);
        values.put(DIFFERENCE, DIFFERENCE);
        values.put(INPUTTOTAL, INPUTTOTAL);
        values.put(OUTPUTTOTAL, OUTPUTTOTAL);
        return database.update(INOUTTABLE,values, ID + " = ?",new String[]{String.valueOf(row.getId())});
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
    }*/

}

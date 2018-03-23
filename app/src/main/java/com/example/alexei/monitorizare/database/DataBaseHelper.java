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

    private static final String TAG = "DataBaseHelper";
    private  static  String DATABASE_NAME="MonitorizareDB";
    private static final int SCHEMA = 1;

    private static final String INOUTTABLE = "InOutTable";

    private static final String ID ="input_ID";

    private static final String DATE="Date";
    private static final String INPUT = "Input";
    private static final String OUTPUT = "Output";
    private static final String DIFFERENCE ="Difference";
    private static final String INPUTTOTAL = "InputTotal";
    private static final String OUTPUTTOTAL = "OutputTotal";

    private static DataBaseHelper sInstance;

    public static synchronized DataBaseHelper getsInstance(Context context)
    {
        if(sInstance == null)
        {
            sInstance = new DataBaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

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


    public void insertData(InOut inOut)
    {
        SQLiteDatabase database = getWritableDatabase();
        //ContentValues values = new ContentValues();

        database.beginTransaction();
        try {
            //long inputID = addOrUpdateData(inOut.ID);
            ContentValues values = new ContentValues();
            //values.put(ID, inputID);
            values.put(DATE, inOut.DATE);
            values.put(INPUT, inOut.INPUT);
            values.put(OUTPUT, inOut.OUTPUT);
            values.put(DIFFERENCE, inOut.DIFFERENCE);
            values.put(INPUTTOTAL, inOut.INPUTTOTAL);
            values.put(OUTPUTTOTAL, inOut.OUTPUTTOTAL);

            database.insertOrThrow(INOUTTABLE,null,values);
            database.setTransactionSuccessful();
        }
        catch(Exception ex)
        {
            Log.d(TAG, "Erorare");
        }
        finally {
            database.endTransaction();
        }


    }
   /* public long addOrUpdateUser(User user) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, user.userName);
            values.put(KEY_USER_PROFILE_PICTURE_URL, user.profilePictureUrl);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_USERS, values, KEY_USER_NAME + "= ?", new String[]{user.userName});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_USER_ID, TABLE_USERS, KEY_USER_NAME);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.userName)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_USERS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }
*/

    /*public List<InOut> getRow(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database .query(INOUTTABLE,
                new String[]{ID, DATE, INPUT, OUTPUT, DIFFERENCE, INPUTTOTAL, OUTPUTTOTAL},
                ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        InOut data = new InOut(
                cursor.getInt(cursor.getColumnIndex(ID)),
                cursor.getString(cursor.getColumnIndex(DATE)),
                cursor.getInt(cursor.getColumnIndex(INPUT)),
                cursor.getInt(cursor.getColumnIndex(OUTPUT)),
                cursor.getInt(cursor.getColumnIndex(DIFFERENCE)),
                cursor.getInt(cursor.getColumnIndex(INPUTTOTAL)),
                cursor.getInt(cursor.getColumnIndex(OUTPUTTOTAL))
                );

        // close the db connection
        cursor.close();

        return data;
    }
*/
    public List<InOut> getAllPosts() {
        List<InOut> data = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s",INOUTTABLE);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
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
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
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

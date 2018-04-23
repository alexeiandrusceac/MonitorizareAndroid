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
import android.widget.Toast;

import com.example.alexei.monitorizare.database.inOutmodel.InOut;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.validation.Schema;

import com.example.alexei.monitorizare.view.MonitorizareMainActivity;
import com.example.mylibrary.ExternalSQLiteHelper;
/**
 * Created by alexei.andrusceac on 19.03.2018.
 */

public class DataBaseHelper extends ExternalSQLiteHelper {
    public  static  String DATABASE_NAME="MonitorizareDB.db";
    private static final int SCHEMA = 1;
    private Context context;
    public DataBaseHelper (Context context)
    {
        super(context,DATABASE_NAME,null,SCHEMA);
    }
    public DataBaseHelper(Context context,String sourceDirectory)
    {
        super(context,DATABASE_NAME,sourceDirectory,null);
    }

    /*public void backupDataBase(String outFileName)
    {
        final String inFileName = context.getDatabasePath(DATABASE_NAME).toString();
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
            Toast.makeText(context, " Copierea bazei de date sa executat cu succes",Toast.LENGTH_SHORT).show();

        }
        catch(Exception exception)
        {
            Toast.makeText(context, "Nu sa copiat baza de date!!!", Toast.LENGTH_SHORT).show();
            exception.printStackTrace();
        }

    }*/

   /*public void importDataBase(String inFileName)
    {
        final String outFileName = context.getDatabasePath(DATABASE_NAME).toString();
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
            Toast.makeText(context, " Copierea bazei de date sa executat cu succes",Toast.LENGTH_SHORT).show();

        }
        catch(Exception exception)
        {
            Toast.makeText(context, "Nu sa copiat baza de date!!!", Toast.LENGTH_SHORT).show();
            exception.printStackTrace();
        }

    }*/

}

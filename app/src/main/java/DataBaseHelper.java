import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import javax.xml.validation.Schema;

/**
 * Created by alexei.andrusceac on 19.03.2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private  static final String DATABASE_NAME="MonitorizareDB";
    private static final int SCHEMA = 1;
    public static final String INOUTTABLE = "InOutTable";

    //InOutTable
    public static final String DATE = "Date";
    public static final String INPUT = "Input";
    public static final String OUTPUT = "Output";
    public static final String DIFFERENCE = "Difference";
    public static final String INPUTTOTAL = "InputTotal";
    public static final String OUTPUTTOTAL = "OutputTotal";
    public static  String  QUERY = "";
    public DataBaseHelper(Context context)
    {
        super(context,DATABASE_NAME,null, SCHEMA);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String InputOutputTable = "create table if not exists " + INOUTTABLE + " ( " + BaseColumns._ID + " integer primary key autoincrement, "
                + DATE + " text not null, "
                + INPUT + " int, "
                + OUTPUT + " int, "
                + DIFFERENCE + "text, "
                + INPUTTOTAL + " text, "
                + INPUTTOTAL + " text);";

        db.execSQL(InputOutputTable);

    }
    public void insertData(String dateTime,int primire,int cheltuire,String diferenta,int totalPrimire,int totalCheltuire)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        String inputRow  = "INSERT INTO "
                + INOUTTABLE + " ("
                + DATE +", "
                + INPUT +", "
                + OUTPUT + ", "
                + DIFFERENCE + ", "
                + INPUTTOTAL+ ", "
                + OUTPUTTOTAL +
                ") Values ("
                + dateTime + ", "
                + primire + ", "
                + cheltuire + ", "
                + diferenta + ", "
                + totalPrimire + ", "
                + totalCheltuire + " )";

       database.execSQL(inputRow);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

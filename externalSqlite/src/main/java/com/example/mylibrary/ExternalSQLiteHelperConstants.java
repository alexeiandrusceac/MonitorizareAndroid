package com.example.mylibrary;

/**
 * Created by alexei.andrusceac on 26.03.2018.
 */

public class ExternalSQLiteHelperConstants {
    static final String TAG = ExternalSQLiteHelper.class.getSimpleName();

    /**
     * Permission required for ExternalSQLiteOpenHelper.
     */
    public static final String READ_EXTERNAL_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";

    /**
     * Name of the file which contains the external version.
     */
    public static final String VERSION_INFO = "version.info";

    /**
     * SQL upgrade script pattern.
     */
    public static final String UPGRADE_SCRIPT = "upgrade_from_%d_to_%d.sql";

    /**
     * Directory in assets which contains the database and optional SQL script.
     */
    public static final String ASSETS_DATABASE = "database";

    /**
     * The attached database name.
     */
    public static final String ATTACHED_EXTERNAL_DATABASE_NAME = "externalDB";
}

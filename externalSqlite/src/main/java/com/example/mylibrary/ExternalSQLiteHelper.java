package com.example.mylibrary;

/**
 * Created by alexei.andrusceac on 26.03.2018.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;

/**
 * Importa o baza de date externa in aplicatie....
 */
public abstract  class ExternalSQLiteHelper {

    private final SQLiteOpenHelper sqLiteOpenHelper;
    private String sourceDirectory;
    private final Context context;
    private final String dataBaseName;
    private boolean fromAssets;
    private boolean upgradeFromExternal;
    private int oldVersion;
    private int newVersion;

    public ExternalSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this.sqLiteOpenHelper = new InternalOpenHelper(context, name, factory, version);
        this.context = context;
        this.dataBaseName = name;
        this.fromAssets = true;
    }

    /**
     * Construct a DatabaseOpenHelper to deploy database from the given source directory.
     * The database file name must be same as the name given to this constructor.
     *
     * @param context         the Android context
     * @param name            name of the database
     * @param sourceDirectory the external directory contains the database and version.info
     * @param factory         the optional cursor factory
     */
    public ExternalSQLiteHelper(Context context, String name, String sourceDirectory, SQLiteDatabase.CursorFactory factory) {
        Utility.validatePermissions(context);
        int version = Utility.readVersionFrom(sourceDirectory + File.separator + ExternalSQLiteHelperConstants.VERSION_INFO);
        this.sqLiteOpenHelper = new InternalOpenHelper(context, name, factory, version);
        this.sourceDirectory = sourceDirectory;
        this.context = context;
        this.dataBaseName = name;
    }

    /**
     * Construct a DatabaseOpenHelper to deploy database from assets/database/ directory.
     * The database file name must be same as the name given to this constructor.
     * This DatabaseOpenHelper will not use any external resources.
     *
     * @param context      the Android context
     * @param name         name of the database
     * @param factory      optional cursor factory
     * @param version      version of the database
     * @param errorHandler error handler
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExternalSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        this.sqLiteOpenHelper = new InternalOpenHelper(context, name, factory, version, errorHandler);
        this.context = context;
        this.dataBaseName = name;
        this.fromAssets = true;
    }

    /**
     * Construct a DatabaseOpenHelper to deploy database from the given source directory.
     * The database file name must be same as the name given to this constructor.
     *
     * @param context         the Android context
     * @param name            name of the database
     * @param sourceDirectory the external directory contains the database and version.info
     * @param factory         optional cursor factory
     * @param errorHandler    error handler
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExternalSQLiteHelper(Context context, String name, String sourceDirectory, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        Utility.validatePermissions(context);
        int version = Utility.readVersionFrom(sourceDirectory + File.separator + ExternalSQLiteHelperConstants.VERSION_INFO);
        this.sqLiteOpenHelper = new InternalOpenHelper(context, name, factory, version, errorHandler);
        this.sourceDirectory = sourceDirectory;
        this.context = context;
        this.dataBaseName = name;
    }
    /**
     * Returns the name of the database.
     *
     * @return the name of the database
     */
    public final String getDatabaseName() {
        return this.dataBaseName;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public final synchronized void setWriteAheadLoggingEnabled(boolean enabled) {
        this.sqLiteOpenHelper.setWriteAheadLoggingEnabled(enabled);
    }

    /**
     * Get a writable database.
     *
     * @return writable database
     */
    public final synchronized SQLiteDatabase getWritableDatabase() {
        deployExternalDatabase();
        return this.sqLiteOpenHelper.getWritableDatabase();
    }

    /**
     * Get a read only database.
     *
     * @return read only database
     */
    public final synchronized SQLiteDatabase getReadableDatabase() {
        deployExternalDatabase();
        return this.sqLiteOpenHelper.getReadableDatabase();
    }

    /**
     * Close the connection.
     */
    public final synchronized void close() {
        this.sqLiteOpenHelper.close();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public final void onConfigure(SQLiteDatabase db) {
        this.sqLiteOpenHelper.onConfigure(db);
    }

    /**
     * Called when the database deployed from assets directory is upgraded.
     *
     * @param database       current database
     * @param currentVersion current version
     * @param newVersion     new version
     */
    public void onUpgradeInternally(SQLiteDatabase database, int currentVersion, int newVersion) {
        // Do nothing
    }

    /**
     * Called when the database deployed from assets directory is downgraded.
     *
     * @param database       current database
     * @param currentVersion current version
     * @param newVersion     new version
     */
    public void onDowngradeInternally(SQLiteDatabase database, int currentVersion, int newVersion) {
        // Do nothing
    }

    /**
     * Called when the database deployed from external directory is upgraded using another database.
     * This method is called before the onUpgradeExternally with attached database.
     *
     * @param writableDatabase         writable current database
     * @param readOnlyExternalDatabase read only external database
     * @param currentVersion           current version
     * @param newVersion               new version
     */
    public void onUpgradeExternally(SQLiteDatabase writableDatabase, SQLiteDatabase readOnlyExternalDatabase, int currentVersion, int newVersion) {
        // Do nothing
    }

    /**
     * This method is called after onUpgradeExternally with current and external databases.
     * The given attachedDatabase is the current database attached with the external database.
     *
     * @param attachedDatabase current database attached with external database
     * @param currentVersion   current version
     * @param newVersion       new version
     */
    public void onUpgradeExternally(SQLiteDatabase attachedDatabase, int currentVersion, int newVersion) {
        // Do nothing
    }

    /**
     * Called when the database deployed from external directory is downgraded using another database.
     * This method is called before the onDowngradeExternally with attached database.
     *
     * @param writableDatabase         writable current database
     * @param readOnlyExternalDatabase read only external database
     * @param currentVersion           current version
     * @param newVersion               new version
     */
    public void onDowngradeExternally(SQLiteDatabase writableDatabase, SQLiteDatabase readOnlyExternalDatabase, int currentVersion, int newVersion) {
        // Do nothing
    }

    /**
     * Called when the database deployed from external directory is downgraded using another database.
     * This method is called before the onDowngradeExternally with attached database.
     *
     * @param attachedDatabase current database attached with external database
     * @param currentVersion   current version
     * @param newVersion       new version
     */
    public void onDowngradeExternally(SQLiteDatabase attachedDatabase, int currentVersion, int newVersion) {
        // Do nothing
    }


    private synchronized void deployExternalDatabase() throws ExternalSQLiteHelperException {
        // Destination location
        File destination = new File(context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator + this.dataBaseName);

        // Source location
        File source = new File(this.sourceDirectory, this.dataBaseName);

        if (!destination.exists())
        {
            try {
                // This is the first time, deploy the database
                this.sqLiteOpenHelper.getReadableDatabase().close();
                if (fromAssets) {
                    String assetsFilePath = ExternalSQLiteHelperConstants.ASSETS_DATABASE + File.separator + this.dataBaseName;
                    Utility.copyDatabaseFromAssets(this.context, assetsFilePath, destination);
                } else {
                    Utility.copyDatabaseFromExternalSource(source, destination);
                }
            } catch (Throwable ex) {
                context.deleteDatabase(this.dataBaseName);  // Roll back the changes
                throw ex;
            }
        }
        else {
            // Trigger upgrade
            this.sqLiteOpenHelper.getReadableDatabase().close();
            // Upgrade or downgrade
            if (upgradeFromExternal) {
                upgradeFromExternalDatabase();
            }
        }
    }

    private synchronized void upgradeFromExternalDatabase() {
        SQLiteDatabase sqLiteDatabase = this.sqLiteOpenHelper.getWritableDatabase();

        // Rollback to older version
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.setVersion(this.oldVersion);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }

        File databaseSource = new File(sourceDirectory, dataBaseName);
        String script = Utility.readUpgradeScriptFromExternalSource(Utility.getUpgradeScriptPath(this.sourceDirectory, this.oldVersion, this.newVersion));
        boolean externalDatabaseExists = databaseSource.exists();
        if (!externalDatabaseExists && script == null) {
            // No resource available to upgrade
            throw new ExternalSQLiteHelperException("Neither database nor upgrade script found to upgrade");
        }

        sqLiteDatabase = this.sqLiteOpenHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();

        try {
            // Execute sql upgrade script
            if (script != null) {
                sqLiteDatabase.execSQL(script);
                if (externalDatabaseExists) {
                    updateFromExternalDatabase(sqLiteDatabase, databaseSource);
                }
            } else if (externalDatabaseExists) {
                updateFromExternalDatabase(sqLiteDatabase, databaseSource);
            }
            // If everything goes smoothly, update the version
            sqLiteDatabase.setVersion(this.newVersion);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }

        // Upgrade using attachment
        if (externalDatabaseExists) {
            sqLiteDatabase = this.sqLiteOpenHelper.getReadableDatabase();
            try {
                sqLiteDatabase.execSQL("ATASAREA BAZEI DE DATE '" + databaseSource.getAbsolutePath() + "' CA " + ExternalSQLiteHelperConstants.ATTACHED_EXTERNAL_DATABASE_NAME + ";");

                if (oldVersion < newVersion) {
                    onUpgradeExternally(sqLiteDatabase, oldVersion, newVersion);
                } else {
                    onDowngradeExternally(sqLiteDatabase, oldVersion, newVersion);
                }

                sqLiteDatabase.execSQL("DETACH " + ExternalSQLiteHelperConstants.ATTACHED_EXTERNAL_DATABASE_NAME + ";");
            } finally {
                sqLiteDatabase.close();
            }
        }
    }

    private void updateFromExternalDatabase(SQLiteDatabase sqLiteDatabase, File databaseSource) {
        SQLiteDatabase externalDatabase = SQLiteDatabase.openDatabase(databaseSource.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        try {
            if (externalDatabase == null) {
                throw new ExternalSQLiteHelperException("Sursa bazei de date nu este valida sau corupta");
            }
            // Let the user to do whatever required
            if (oldVersion < newVersion) {
                onUpgradeExternally(sqLiteDatabase, externalDatabase, oldVersion, newVersion);
            } else {
                onDowngradeExternally(sqLiteDatabase, externalDatabase, oldVersion, newVersion);
            }
        } finally {
            if (externalDatabase != null) {
                externalDatabase.close();
            }
        }
    }


    /**
     * Internal helper class to deal with underlying database.
     */
    private class InternalOpenHelper extends SQLiteOpenHelper {

        public InternalOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public InternalOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // Do nothing
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            System.out.println("Upgrade method: " + oldVersion + " to " + newVersion);
            if (fromAssets) {
                String script = Utility.readIUpgradeScriptFromAssets(context, Utility.getUpgradeScriptPath(ExternalSQLiteHelperConstants.ASSETS_DATABASE, oldVersion, newVersion));
                if (script != null) {
                    sqLiteDatabase.execSQL(script);
                }
                ExternalSQLiteHelper.this.onUpgradeInternally(sqLiteDatabase, oldVersion, newVersion);
            } else {
                synchronized (ExternalSQLiteHelper.this) {
                    ExternalSQLiteHelper.this.upgradeFromExternal = true;
                    ExternalSQLiteHelper.this.oldVersion = oldVersion;
                    ExternalSQLiteHelper.this.newVersion = newVersion;
                }
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            System.out.println("Downgrade method: " + oldVersion + " to " + newVersion);
            if (fromAssets) {
                String script = Utility.readIUpgradeScriptFromAssets(context, Utility.getUpgradeScriptPath(ExternalSQLiteHelperConstants.ASSETS_DATABASE, oldVersion, newVersion));
                if (script != null) {
                    sqLiteDatabase.execSQL(script);
                }
                ExternalSQLiteHelper.this.onDowngradeInternally(sqLiteDatabase, oldVersion, newVersion);
            } else {
                synchronized (ExternalSQLiteHelper.this) {
                    upgradeFromExternal = true;
                    ExternalSQLiteHelper.this.oldVersion = oldVersion;
                    ExternalSQLiteHelper.this.newVersion = newVersion;
                }
            }
        }
    }
}

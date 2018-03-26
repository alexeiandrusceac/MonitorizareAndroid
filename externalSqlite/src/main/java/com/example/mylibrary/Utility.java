package com.example.mylibrary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by alexei.andrusceac on 26.03.2018.
 */

class Utility {
    private Utility() {

    }

    public static int readVersionFrom(String path)
    {
        int externalVersion;
        Scanner scanner = null;
        try
        {
            File versionFile = new File(path);
            scanner = new Scanner(versionFile, Charset.defaultCharset().name());
            if(scanner.hasNextInt()) {
                externalVersion = scanner.nextInt();
                if (externalVersion < 1) {
                    throw new IllegalArgumentException("Versiunea trebuie sa fie >=1,dar este " + externalVersion);
                }
            } else
                {
                    throw new ExternalSQLiteHelperException(path + "nu contine un numar de versiune valid");
                }

        }
        catch (FileNotFoundException e)
        {
            throw new ExternalSQLiteHelperException(path + "fisierul nu exista");
        }
        finally {
            if(scanner!=null)
            {
                scanner.close();
            }
        }
        System.out.println("version.info = " + externalVersion);
return externalVersion;
    }

    public static String readIUpgradeScriptFromAssets (Context context, String path)
    {
        String script = null;
        InputStream inputStream = null;
        try{
            inputStream = context.getAssets().open(path);
            script = readFromStream(inputStream);
        }
        catch(IOException e)
        {

        }
        finally {
            if(inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch(IOException ex)
                {
                    Log.e(ExternalSQLiteHelperConstants.TAG,"inchiderea fisierului nu sa efectuat cu succes");
                }
            }
        }
        return script;
    }
    public static String readUpgradeScriptFromExternalSource(String path) {
        String script = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            script = readFromStream(inputStream);
        } catch (FileNotFoundException e) {
            // Do nothing since the script is optional
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(ExternalSQLiteHelperConstants.TAG, "Esec la inchiderea fisierului script");
                }
            }
        }

        return script;
    }
    public static boolean assetFileExists(Context context, String path) {
        InputStream inputStream = null;
        boolean exists = false;
        try {
            inputStream = context.getAssets().open(path);
            exists = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(ExternalSQLiteHelperConstants.TAG, "Eroare la inchiderea fisierului " + path + " din folderul Assets");
                }
            }
        }

        return exists;
    }

    public static void copyDatabaseFromAssets(Context context, String path, File destination) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
            copyStreamTo(inputStream, destination);
        } catch (IOException e) {
            throw new ExternalSQLiteHelperException("Nu sa efectuat deschiderea bazei de date din Assets/" + path);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(ExternalSQLiteHelperConstants.TAG, "Inchiderea destinatiei sa efectuat cu esec");
            }
        }
    }

    public static void copyDatabaseFromExternalSource(File source, File destination) {
        if (source.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(source);
                copyStreamTo(inputStream, destination);
            } catch (FileNotFoundException e) {
                throw new ExternalSQLiteHelperException("Deschiderea bazei de date din locatie externa sa efectuat cu esec");
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(ExternalSQLiteHelperConstants.TAG, "Esec la inchiderea destinatiei");
                }
            }
        }
    }

    private static String readFromStream(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        String script = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            if (builder.length() > 0) {
                script = builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(ExternalSQLiteHelperConstants.TAG, "Esec la inchiderea fisierului script");
                }
            }
        }

        return script;
    }

    private static void copyStreamTo(InputStream inputStream, File destination) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (FileNotFoundException e) {
            throw new ExternalSQLiteHelperException("Esec la deschiderea destinatiei bazei de date");
        } catch (IOException e) {
            throw new ExternalSQLiteHelperException("Esec la copierea bazei de date externe la destinatie");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e(ExternalSQLiteHelperConstants.TAG, "Esec la inchiderea bazei de date externe");
            }
        }
    }

    /**
     * This method check for the necessary permissions. If any of them are not provided,
     * will throw an exception.
     *
     * @param context the Android context
     */
    public static void validatePermissions(Context context) {
        // Check for READ_EXTERNAL_STORAGE_PERMISSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context, ExternalSQLiteHelperConstants.READ_EXTERNAL_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            throw new ExternalSQLiteHelperException(ExternalSQLiteHelperConstants.READ_EXTERNAL_STORAGE_PERMISSION + " permisiunea nu este garantata");
        }
    }

    public static String getUpgradeScriptPath(String parent, int oldVersion, int newVersion) {
        String path = parent + File.separator + String.format(ExternalSQLiteHelperConstants.UPGRADE_SCRIPT, oldVersion, newVersion);
        return path;
    }

}

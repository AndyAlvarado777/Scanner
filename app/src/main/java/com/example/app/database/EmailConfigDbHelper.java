// com.example.app.database/EmailConfigDbHelper.java
package com.example.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EmailConfigDbHelper extends SQLiteOpenHelper {
    // Si cambias el esquema de la base de datos, debes incrementar la versión.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EmailConfig.db";

    // Sentencia SQL para crear la tabla de configuración de correo
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EmailConfigContract.EmailConfigEntry.TABLE_NAME + " (" +
                    EmailConfigContract.EmailConfigEntry._ID + " INTEGER PRIMARY KEY," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS + " TEXT)";

    // Sentencia SQL para borrar la tabla (útil para actualizaciones o reseteos)
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EmailConfigContract.EmailConfigEntry.TABLE_NAME;

    public EmailConfigDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Se llama solo la primera vez que se accede a la base de datos
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Esta política de actualización es simplemente descartar los datos y empezar de nuevo.
        // En una app real, podrías querer migrar datos.
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
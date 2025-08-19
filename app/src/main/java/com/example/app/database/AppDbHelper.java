package com.example.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2; // ¡Incrementa la versión!
    public static final String DATABASE_NAME = "AppDatabase.db";

    // SQL para la tabla de correos (la que ya tienes)
    private static final String SQL_CREATE_EMAIL_ENTRIES =
            "CREATE TABLE " + EmailConfigContract.EmailConfigEntry.TABLE_NAME + " (" +
                    EmailConfigContract.EmailConfigEntry._ID + " INTEGER PRIMARY KEY," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS + " TEXT)";

    // Nuevo SQL para la tabla de transportistas
    private static final String SQL_CREATE_TRANSPORTISTA_ENTRIES =
            "CREATE TABLE " + TransportistaContract.TransportistaEntry.TABLE_NAME + " (" +
                    TransportistaContract.TransportistaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO + " TEXT UNIQUE NOT NULL," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA + " TEXT DEFAULT 'sal'," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE + " TEXT)";

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EMAIL_ENTRIES);
        db.execSQL(SQL_CREATE_TRANSPORTISTA_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En una app real, aquí harías una migración de datos.
        // Para este ejemplo, eliminaremos y recrearemos las tablas.
        if (oldVersion < 2) {
            // Solo crea la nueva tabla si la versión anterior es menor a 2
            db.execSQL(SQL_CREATE_TRANSPORTISTA_ENTRIES);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
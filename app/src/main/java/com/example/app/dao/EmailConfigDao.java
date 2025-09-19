// com.example.app.dao/EmailConfigDao.java
package com.example.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.app.database.AppDbHelper;
import com.example.app.database.EmailConfigContract;
import com.example.app.model.EmailConfig;

public class EmailConfigDao {
    private AppDbHelper dbHelper;

    public EmailConfigDao(Context context) {
        dbHelper = new AppDbHelper(context);
    }

    // Método para guardar (o actualizar si ya existe) la configuración
    public long saveOrUpdateConfig(EmailConfig config) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EmailConfigContract.EmailConfigEntry._ID, 1); // ID fijo
        values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL, config.getRemitenteEmail());
        values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD, config.getAppPassword());
        values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS, config.getDestinatariosEmails());

        // Intentar actualizar primero la fila con _ID = 1
        int rowsUpdated = db.update(
                EmailConfigContract.EmailConfigEntry.TABLE_NAME,
                values,
                EmailConfigContract.EmailConfigEntry._ID + " = ?",
                new String[]{"1"});

        long id;
        if (rowsUpdated == 0) {
            // No existía la fila, insertar con _ID = 1
            id = db.insert(EmailConfigContract.EmailConfigEntry.TABLE_NAME, null, values);
        } else {
            id = 1;
        }

        db.close();
        return id; // Retorna el ID fijo 1 si fue exitoso, o -1 si falló el insert
    }

    // Método para obtener la única configuración (asumimos solo una fila)
    public EmailConfig getConfig() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        EmailConfig config = null;

        String[] projection = {
                EmailConfigContract.EmailConfigEntry._ID,
                EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL,
                EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD,
                EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS
        };

        Cursor cursor = db.query(
                EmailConfigContract.EmailConfigEntry.TABLE_NAME,
                projection,
                null, // No WHERE clause
                null, // No WHERE args
                null, // No GROUP BY
                null, // No HAVING
                null  // No ORDER BY
        );

        if (cursor != null && cursor.moveToFirst()) {
            config = new EmailConfig();
            config.setId(cursor.getLong(cursor.getColumnIndexOrThrow(EmailConfigContract.EmailConfigEntry._ID)));
            config.setRemitenteEmail(cursor.getString(cursor.getColumnIndexOrThrow(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL)));
            config.setAppPassword(cursor.getString(cursor.getColumnIndexOrThrow(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD)));
            config.setDestinatariosEmails(cursor.getString(cursor.getColumnIndexOrThrow(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS)));
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return config;
    }

    // Método para eliminar la configuración (útil para resetear)
    public int deleteConfig() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Elimina todas las filas (solo debería haber una)
        int deletedRows = db.delete(EmailConfigContract.EmailConfigEntry.TABLE_NAME, null, null);
        db.close();
        return deletedRows;
    }
}
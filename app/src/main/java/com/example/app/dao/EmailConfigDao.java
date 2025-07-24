// com.example.app.dao/EmailConfigDao.java
package com.example.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.app.database.EmailConfigContract;
import com.example.app.database.EmailConfigDbHelper;
import com.example.app.model.EmailConfig;

public class EmailConfigDao {
    private EmailConfigDbHelper dbHelper;

    public EmailConfigDao(Context context) {
        dbHelper = new EmailConfigDbHelper(context);
    }

    // Método para guardar (o actualizar si ya existe) la configuración
    public long saveOrUpdateConfig(EmailConfig config) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Primero intenta leer la configuración existente (solo debería haber una)
        EmailConfig existingConfig = getConfig();
        long newRowId = -1;

        if (existingConfig != null) { // Si ya existe, actualiza
            ContentValues values = new ContentValues();
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL, config.getRemitenteEmail());
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD, config.getAppPassword());
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS, config.getDestinatariosEmails());

            String selection = EmailConfigContract.EmailConfigEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(existingConfig.getId()) };

            int count = db.update(
                    EmailConfigContract.EmailConfigEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            if (count > 0) {
                newRowId = existingConfig.getId(); // Si se actualizó, devuelve el ID existente
            }

        } else { // Si no existe, inserta
            ContentValues values = new ContentValues();
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL, config.getRemitenteEmail());
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD, config.getAppPassword());
            values.put(EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS, config.getDestinatariosEmails());

            newRowId = db.insert(EmailConfigContract.EmailConfigEntry.TABLE_NAME, null, values);
        }

        db.close();
        return newRowId; // Devuelve el ID de la fila insertada/actualizada
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
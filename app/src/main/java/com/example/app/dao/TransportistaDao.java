package com.example.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.app.database.AppDbHelper;
import com.example.app.database.TransportistaContract;
import com.example.app.model.Transportista;

import java.util.ArrayList;
import java.util.List;

public class TransportistaDao {
    private AppDbHelper dbHelper;

    public TransportistaDao(Context context) {
        dbHelper = new AppDbHelper(context);
    }

    // Método para insertar un nuevo transportista
    public long insertTransportista(Transportista transportista) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO, transportista.getCodigoTransportista());
        values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA, transportista.getCodigoEmpresa());
        values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE, transportista.getNombreTransportista());
        long newRowId = db.insert(TransportistaContract.TransportistaEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    // Método para obtener todos los transportistas
    public List<Transportista> getAllTransportistas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Transportista> transportistas = new ArrayList<>();
        Cursor cursor = db.query(
                TransportistaContract.TransportistaEntry.TABLE_NAME,
                null, // Todas las columnas
                null, // Sin WHERE clause
                null, // Sin WHERE args
                null, // No GROUP BY
                null, // No HAVING
                TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE + " ASC" // Ordenar por nombre
        );

        if (cursor.moveToFirst()) {
            do {
                Transportista t = new Transportista();
                t.setCodigoTransportista(cursor.getString(cursor.getColumnIndexOrThrow(TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO)));
                t.setCodigoEmpresa(cursor.getString(cursor.getColumnIndexOrThrow(TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA)));
                t.setNombreTransportista(cursor.getString(cursor.getColumnIndexOrThrow(TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE)));
                transportistas.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transportistas;
    }

    // Método para actualizar un transportista
    public int updateTransportista(Transportista transportista) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA, transportista.getCodigoEmpresa());
        values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE, transportista.getNombreTransportista());
        String selection = TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO + " LIKE ?";
        String[] selectionArgs = { transportista.getCodigoTransportista() };
        int count = db.update(
                TransportistaContract.TransportistaEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    // Método para eliminar un transportista
    public int deleteTransportista(String codigo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO + " LIKE ?";
        String[] selectionArgs = { codigo };
        int deletedRows = db.delete(TransportistaContract.TransportistaEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        return deletedRows;
    }
}
package com.example.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.app.database.AppDbHelper;
import com.example.app.database.DespachadorContract;
import com.example.app.model.Despachador;

import java.util.ArrayList;
import java.util.List;

public class DespachadorDao {
    private AppDbHelper dbHelper;

    public DespachadorDao(Context context) {
        dbHelper = new AppDbHelper(context);
    }

    // Método para insertar un nuevo despachador
    public long insertDespachador(Despachador despachador) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO, despachador.getCodigoDespachador());
        values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE, despachador.getNombreDespachador());
        values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_EMPRESA, despachador.getCodigoEmpresa()); // <-- Añade esta línea
        long newRowId = db.insert(DespachadorContract.DespachadorEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    // Método para obtener todos los despachadores
    public List<Despachador> getAllDespachador() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Despachador> despachadores = new ArrayList<>();
        Cursor cursor = db.query(
                DespachadorContract.DespachadorEntry.TABLE_NAME, // La tabla correcta
                null,
                null,
                null,
                null,
                null,
                DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                Despachador d = new Despachador();
                d.setCodigoDespachador(cursor.getString(cursor.getColumnIndexOrThrow(DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO)));
                d.setNombreDespachador(cursor.getString(cursor.getColumnIndexOrThrow(DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE)));
                d.setCodigoEmpresa(cursor.getString(cursor.getColumnIndexOrThrow(DespachadorContract.DespachadorEntry.COLUMN_NAME_EMPRESA))); // <-- Añade esta línea
                despachadores.add(d);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return despachadores;
    }

    // Método para actualizar un despachador
    public int updateDespachador(Despachador despachador) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE, despachador.getNombreDespachador());
        values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_EMPRESA, despachador.getCodigoEmpresa()); // <-- Añade esta línea
        String selection = DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO + " LIKE ?";
        String[] selectionArgs = { despachador.getCodigoDespachador() };
        int count = db.update(
                DespachadorContract.DespachadorEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    // Método para eliminar un despachador
    public int deleteDespachador(String codigo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO + " LIKE ?";
        String[] selectionArgs = { codigo };
        int deletedRows = db.delete(
                DespachadorContract.DespachadorEntry.TABLE_NAME, // La tabla correcta
                selection,
                selectionArgs
        );
        db.close();
        return deletedRows;
    }
}
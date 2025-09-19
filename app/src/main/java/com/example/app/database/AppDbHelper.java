package com.example.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppDbHelper extends SQLiteOpenHelper {
    // Incrementa la versión si la base de datos ya existía sin los datos precargados
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "AppDatabase.db";

    // --- Sentencias SQL para crear las tablas (sin cambios) ---
    private static final String SQL_CREATE_EMAIL_ENTRIES =
            "CREATE TABLE " + EmailConfigContract.EmailConfigEntry.TABLE_NAME + " (" +
                    EmailConfigContract.EmailConfigEntry._ID + " INTEGER PRIMARY KEY," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_REMITENTE_EMAIL + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_APP_PASSWORD + " TEXT," +
                    EmailConfigContract.EmailConfigEntry.COLUMN_NAME_DESTINATARIOS_EMAILS + " TEXT)";

    private static final String SQL_CREATE_TRANSPORTISTA_ENTRIES =
            "CREATE TABLE " + TransportistaContract.TransportistaEntry.TABLE_NAME + " (" +
                    TransportistaContract.TransportistaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO + " TEXT UNIQUE NOT NULL," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA + " TEXT," +
                    TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE + " TEXT)";

    private static final String SQL_CREATE_DESPACHADOR_ENTRIES =
            "CREATE TABLE " + DespachadorContract.DespachadorEntry.TABLE_NAME + " (" +
                    DespachadorContract.DespachadorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO + " TEXT UNIQUE NOT NULL," +
                    DespachadorContract.DespachadorEntry.COLUMN_NAME_EMPRESA + " TEXT," +
                    DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE + " TEXT)";

    // 2. AÑADE LA SENTENCIA SQL PARA CREAR LA NUEVA TABLA
    private static final String SQL_CREATE_PRODUCTOS_ENTRIES =
            "CREATE TABLE " + ProductoContract.ProductoEntry.TABLE_NAME + " (" +
                    ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_PROVEEDOR + " TEXT PRIMARY KEY," + // Clave primaria para búsquedas rápidas
                    ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_INTERNO + " TEXT NOT NULL)";


    // ✅ Variable para el Context, necesaria para leer 'assets'
    private final Context context;

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("AppDbHelper", "Creando base de datos y tablas...");
        // 1. Crear la estructura de las tablas
        db.execSQL(SQL_CREATE_EMAIL_ENTRIES);
        db.execSQL(SQL_CREATE_TRANSPORTISTA_ENTRIES);
        db.execSQL(SQL_CREATE_DESPACHADOR_ENTRIES);
        db.execSQL(SQL_CREATE_PRODUCTOS_ENTRIES);

        // 2. Precargar los datos usando una transacción para eficiencia
        db.beginTransaction();
        try {
            Log.d("AppDbHelper", "Iniciando carga de datos iniciales (seeding)...");
            seedTransportistas(db);
            seedDespachadores(db);
            seedProductos(db);
            db.setTransactionSuccessful(); // Marcar la transacción como exitosa
            Log.d("AppDbHelper", "Carga de datos iniciales finalizada con éxito.");
        } catch (Exception e) {
            Log.e("AppDbHelper", "Error durante la carga de datos iniciales", e);
        } finally {
            db.endTransaction(); // Finalizar la transacción
        }
    }

    /**
     * ✅ Lee transportistas.csv de 'assets' e inserta los datos en la base de datos.
     */
    private void seedTransportistas(SQLiteDatabase db) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("transportistas.csv")))) {
            reader.readLine(); // Omitir la línea de cabecera
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 3) continue;

                ContentValues values = new ContentValues();
                values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_EMPRESA, columns[0].trim());
                values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_CODIGO, columns[1].trim());
                values.put(TransportistaContract.TransportistaEntry.COLUMN_NAME_NOMBRE, columns[2].trim());

                db.insertWithOnConflict(TransportistaContract.TransportistaEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            Log.d("AppDbHelper", "Datos de transportistas cargados.");
        } catch (IOException e) {
            Log.e("AppDbHelper", "Error al leer transportistas.csv", e);
        }
    }

    /**
     * ✅ Lee despachadores.csv de 'assets' e inserta los datos en la base de datos.
     */
    private void seedDespachadores(SQLiteDatabase db) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("despachadores.csv")))) {
            reader.readLine(); // Omitir la línea de cabecera
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 3) continue;

                ContentValues values = new ContentValues();
                values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_EMPRESA, columns[0].trim());
                values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_CODIGO, columns[1].trim());
                values.put(DespachadorContract.DespachadorEntry.COLUMN_NAME_NOMBRE, columns[2].trim());

                db.insertWithOnConflict(DespachadorContract.DespachadorEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            Log.d("AppDbHelper", "Datos de despachadores cargados.");
        } catch (IOException e) {
            Log.e("AppDbHelper", "Error al leer despachadores.csv", e);
        }
    }

    // 5. AÑADE EL NUEVO MÉTODO PARA CARGAR LOS DATOS DE PRODUCTOS
    /**
     * Lee mapeo_productos.csv de 'assets' e inserta los datos en la base de datos.
     */
    private void seedProductos(SQLiteDatabase db) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("mapeo_productos.csv")))) {
            reader.readLine(); // Omitir la línea de cabecera si existe
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length < 2) continue; // Necesitamos al menos 2 columnas

                ContentValues values = new ContentValues();
                values.put(ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_PROVEEDOR, columns[0].trim());
                values.put(ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_INTERNO, columns[1].trim());

                db.insertWithOnConflict(ProductoContract.ProductoEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            Log.d("AppDbHelper", "Datos de mapeo de productos cargados.");
        } catch (IOException e) {
            Log.e("AppDbHelper", "Error al leer mapeo_productos.csv", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("AppDbHelper", "Actualizando base de datos de v" + oldVersion + " a v" + newVersion + ". Se borrarán los datos antiguos.");
        // Elimina las tablas existentes
        db.execSQL("DROP TABLE IF EXISTS " + EmailConfigContract.EmailConfigEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TransportistaContract.TransportistaEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DespachadorContract.DespachadorEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductoContract.ProductoEntry.TABLE_NAME);
        // Vuelve a crear la base de datos (y carga los datos de nuevo)
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
// En: com.example.app.dao/ProductoDao.java
package com.example.app.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.app.database.AppDbHelper;
import com.example.app.database.ProductoContract;
import com.example.app.model.Producto;

public class ProductoDao {
    private AppDbHelper dbHelper;

    public ProductoDao(Context context) {
        dbHelper = new AppDbHelper(context);
    }

    /**
     * Busca un producto por su código de proveedor.
     * @param codigoProveedor El código escaneado.
     * @return Un objeto Producto si se encuentra, de lo contrario null.
     */
    public Producto findProductoByProveedorCodigo(String codigoProveedor) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Producto producto = null;

        String[] projection = {
                ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_PROVEEDOR,
                ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_INTERNO
        };

        String selection = ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_PROVEEDOR + " = ?";
        String[] selectionArgs = { codigoProveedor };

        Cursor cursor = db.query(
                ProductoContract.ProductoEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String codigoInterno = cursor.getString(cursor.getColumnIndexOrThrow(ProductoContract.ProductoEntry.COLUMN_NAME_CODIGO_INTERNO));
            producto = new Producto(codigoProveedor, codigoInterno);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return producto;
    }
}
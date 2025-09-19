// En: com.example.app.database/ProductoContract.java
package com.example.app.database;

import android.provider.BaseColumns;

public final class ProductoContract {
    private ProductoContract() {}

    public static class ProductoEntry implements BaseColumns {
        public static final String TABLE_NAME = "productos";
        // Código de barras que escanea el dispositivo (del proveedor)
        public static final String COLUMN_NAME_CODIGO_PROVEEDOR = "codigo_proveedor";
        // Código equivalente interno de la empresa
        public static final String COLUMN_NAME_CODIGO_INTERNO = "codigo_interno";
        // Podrías añadir más columnas si es necesario, como la descripción del producto.
    }
}
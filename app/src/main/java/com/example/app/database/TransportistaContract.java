package com.example.app.database;

import android.provider.BaseColumns;

public final class TransportistaContract {
    private TransportistaContract() {}

    public static class TransportistaEntry implements BaseColumns {
        public static final String TABLE_NAME = "transportistas";
        public static final String COLUMN_NAME_CODIGO = "codigo_transportista";
        public static final String COLUMN_NAME_EMPRESA = "codigo_empresa"; // Valor por defecto 'sal'
        public static final String COLUMN_NAME_NOMBRE = "nombre_transportista";
    }
}
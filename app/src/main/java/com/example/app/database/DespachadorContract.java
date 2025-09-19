package com.example.app.database;

import android.provider.BaseColumns;

public class DespachadorContract {
    private DespachadorContract() {}

    public static class DespachadorEntry implements BaseColumns {
        public static final String TABLE_NAME = "despachadores";
        public static final String COLUMN_NAME_CODIGO = "codigo_despachador";
        public static final String COLUMN_NAME_EMPRESA = "codigo_empresa"; // Valor por defecto 'sal'
        public static final String COLUMN_NAME_NOMBRE = "nombre_despachador";
    }
}

// com.example.app.database/EmailConfigContract.java
package com.example.app.database;

import android.provider.BaseColumns;

public final class EmailConfigContract {
    // Para prevenir instanciación accidental
    private EmailConfigContract() {}

    /* Inner class that defines the table contents */
    public static class EmailConfigEntry implements BaseColumns {
        public static final String TABLE_NAME = "email_config";
        public static final String COLUMN_NAME_REMITENTE_EMAIL = "remitente_email";
        public static final String COLUMN_NAME_APP_PASSWORD = "app_password";
        public static final String COLUMN_NAME_DESTINATARIOS_EMAILS = "destinatarios_emails";

        // _ID es heredado de BaseColumns, que es útil para SQLite
        // Por ser una configuración única, solo necesitaremos una fila,
        // pero la estructura es la misma que si hubiera múltiples.
    }
}
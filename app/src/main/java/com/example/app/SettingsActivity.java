package com.example.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Asegúrate de importar Log
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.dao.EmailConfigDao;
import com.example.app.model.EmailConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity"; // Define un TAG para los logs

    private TextInputEditText editEmailRemitente;
    private TextInputEditText editAppPassword;
    private TextInputEditText editEmailsDestino;
    private MaterialButton btnSaveSettings;
    private MaterialButton btnClearSettings;

    private EmailConfigDao emailConfigDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "onCreate: SettingsActivity iniciada.");

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        emailConfigDao = new EmailConfigDao(this);

        // *** MUY IMPORTANTE: Inicializar las vistas ANTES de cargar los datos ***
        editEmailRemitente = findViewById(R.id.edit_email_remitente);
        editAppPassword = findViewById(R.id.edit_app_password);
        editEmailsDestino = findViewById(R.id.edit_emails_destino);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        // btnClearSettings = findViewById(R.id.btn_clear_settings); // Descomentar si usas el botón

        // Cargar la configuración actual
        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        // if (btnClearSettings != null) { // Siempre verifica si el botón existe antes de asignar listener
        //     btnClearSettings.setOnClickListener(v -> clearSettings());
        // }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_trips) {
            Toast.makeText(this, "Navegando a Viajes...", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Ya estás en Configuraciones", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSettings() {
        Log.d(TAG, "loadSettings: Cargando configuración...");
        EmailConfig config = emailConfigDao.getConfig(); // Llama al DAO para obtener la configuración

        if (config != null) {
            Log.d(TAG, "loadSettings: Configuración encontrada. Remitente: " + config.getRemitenteEmail() +
                    ", Destinatarios: " + config.getDestinatariosEmails());
            // ¡¡¡Verifica que los IDs aquí (R.id.edit_email_remitente, etc.)
            // coincidan exactamente con los IDs en tu activity_settings.xml!!!
            editEmailRemitente.setText(config.getRemitenteEmail());
            editAppPassword.setText(config.getAppPassword()); // ¡Asegúrate que se asigna aquí!
            editEmailsDestino.setText(config.getDestinatariosEmails());
            Log.d(TAG, "loadSettings: Campos de texto actualizados.");
        } else {
            Log.d(TAG, "loadSettings: No se encontró configuración. Estableciendo valores por defecto.");
            // Esto es importante para el primer inicio o si se borra la configuración
            editEmailRemitente.setText("alvaradoandy097@gmail.com");
            editAppPassword.setText(""); // Nunca deberías poner una clave real aquí en el código.
            editEmailsDestino.setText("andy.alvarado@pbs.group");
        }
    }

    private void saveSettings() {
        String remitenteEmail = editEmailRemitente.getText().toString().trim();
        String appPassword = editAppPassword.getText().toString().trim();
        String destinatariosEmails = editEmailsDestino.getText().toString().trim();

        if (TextUtils.isEmpty(remitenteEmail) || TextUtils.isEmpty(appPassword) || TextUtils.isEmpty(destinatariosEmails)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveSettings: Campos vacíos detectados.");
            return;
        }

        EmailConfig config = new EmailConfig(remitenteEmail, appPassword, destinatariosEmails);
        long result = emailConfigDao.saveOrUpdateConfig(config);

        if (result != -1) {
            Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveSettings: Configuración guardada con éxito. ID: " + result);
            // Recargar configuración para verificar
            loadSettings();
            // Aquí puedes comentar finish() para hacer pruebas
            // finish();
        } else {
            Toast.makeText(this, "Error al guardar la configuración", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveSettings: Error al guardar la configuración.");
        }
    }

    // Opcional: Método para borrar la configuración
    private void clearSettings() {
        Log.d(TAG, "clearSettings: Borrando configuración...");
        int deletedRows = emailConfigDao.deleteConfig();
        if (deletedRows > 0) {
            Toast.makeText(this, "Configuración borrada", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "clearSettings: Filas borradas: " + deletedRows);
            editEmailRemitente.setText("");
            editAppPassword.setText("");
            editEmailsDestino.setText("");
        } else {
            Toast.makeText(this, "No hay configuración para borrar", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "clearSettings: No se encontró configuración para borrar.");
        }
    }
}
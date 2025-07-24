package com.example.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app.dao.EmailConfigDao; // Importar el DAO
import com.example.app.model.EmailConfig; // Importar el Modelo
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText editEmailRemitente;
    private TextInputEditText editAppPassword;
    private TextInputEditText editEmailsDestino;
    private MaterialButton btnSaveSettings;
    private MaterialButton btnClearSettings; // Nuevo botón para borrar/resetear la configuración

    private EmailConfigDao emailConfigDao; // Instancia del DAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 2. Inicializar el DAO
        emailConfigDao = new EmailConfigDao(this);

        // 3. Inicializar las vistas
        editEmailRemitente = findViewById(R.id.edit_email_remitente);
        editAppPassword = findViewById(R.id.edit_app_password);
        editEmailsDestino = findViewById(R.id.edit_emails_destino);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        // btnClearSettings = findViewById(R.id.btn_clear_settings); // Descomentar cuando agregues el botón en XML

        // 4. Cargar la configuración actual desde la base de datos
        loadSettings();

        // 5. Configurar los listeners
        btnSaveSettings.setOnClickListener(v -> saveSettings());
        // btnClearSettings.setOnClickListener(v -> clearSettings()); // Descomentar y añadir el método
    }

    // Este método maneja el clic en el botón de retroceso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Métodos para el menú de opciones (igual que antes)
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
            // Lógica para ir a la pantalla de Viajes
            // Intent intent = new Intent(this, TripsActivity.class);
            // startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Ya estás en Configuraciones", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Métodos de interacción con la base de datos ---

    private void loadSettings() {
        EmailConfig config = emailConfigDao.getConfig();
        if (config != null) {
            editEmailRemitente.setText(config.getRemitenteEmail());
            editAppPassword.setText(config.getAppPassword());
            editEmailsDestino.setText(config.getDestinatariosEmails());
        } else {
            // Establecer valores por defecto si no hay configuración guardada
            editEmailRemitente.setText("alvaradoandy097@gmail.com"); // Valor por defecto del remitente
            editAppPassword.setText(""); // Clave siempre vacía al cargar si no está en DB
            editEmailsDestino.setText("andy.alvarado@pbs.group"); // Valor por defecto de destinatario
        }
    }

    private void saveSettings() {
        String remitenteEmail = editEmailRemitente.getText().toString().trim();
        String appPassword = editAppPassword.getText().toString().trim();
        String destinatariosEmails = editEmailsDestino.getText().toString().trim();

        if (TextUtils.isEmpty(remitenteEmail) || TextUtils.isEmpty(appPassword) || TextUtils.isEmpty(destinatariosEmails)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        EmailConfig config = new EmailConfig(remitenteEmail, appPassword, destinatariosEmails);
        long result = emailConfigDao.saveOrUpdateConfig(config); // Guardar/actualizar en la DB

        if (result != -1) {
            Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
            // finish(); // Considera si quieres cerrar la actividad automáticamente o no
        } else {
            Toast.makeText(this, "Error al guardar la configuración", Toast.LENGTH_SHORT).show();
        }
    }

    // Opcional: Método para borrar la configuración (añade un botón en el XML si lo usas)
    private void clearSettings() {
        int deletedRows = emailConfigDao.deleteConfig();
        if (deletedRows > 0) {
            Toast.makeText(this, "Configuración borrada", Toast.LENGTH_SHORT).show();
            editEmailRemitente.setText("");
            editAppPassword.setText("");
            editEmailsDestino.setText("");
        } else {
            Toast.makeText(this, "No hay configuración para borrar", Toast.LENGTH_SHORT).show();
        }
    }
}
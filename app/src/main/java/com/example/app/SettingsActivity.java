package com.example.app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        // Habilitar el botón de retroceso en la barra de acción
        // Esto muestra la flecha de retroceso automáticamente.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 2. Aquí iría la lógica de inicialización de vistas, pero la omitimos por ahora.
        // Por ejemplo:
        // TextInputEditText editEmailRemitente = findViewById(R.id.edit_email_remitente);
        // ... etc
    }

    // Método para inflar el menú en la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Método para manejar los clics en los ítems del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // En esta pantalla, el menú de opciones también tendrá los ítems "Viajes" y "Configuraciones".
        // Sin embargo, si ya estamos en Configuraciones, no debería hacer nada o mostrar un Toast.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Maneja el clic en el botón de retroceso de la Toolbar
            onBackPressed();
            return true;
        } else if (id == R.id.action_trips) {
            Toast.makeText(this, "Navegando a Viajes...", Toast.LENGTH_SHORT).show();
            // Lógica para ir a la pantalla de Viajes (ej: Intent)
            // Intent intent = new Intent(this, TripsActivity.class);
            // startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Ya estamos en la pantalla de configuraciones.
            Toast.makeText(this, "Ya estás en Configuraciones", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
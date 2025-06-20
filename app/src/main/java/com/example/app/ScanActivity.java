package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ScanActivity extends AppCompatActivity {

    private TextInputEditText editCodigoViaje, editCodigoDespachador, editCodigoTransportista;
    private MaterialButton btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        editCodigoViaje = findViewById(R.id.editCodigoViaje);
        editCodigoDespachador = findViewById(R.id.editCodigoDespachador);
        editCodigoTransportista = findViewById(R.id.editCodigoTransportista);
        btnComenzar = findViewById(R.id.btnComenzar);

        btnComenzar.setOnClickListener(v -> {
            String codigoViaje = editCodigoViaje.getText().toString().trim();
            String codigoDespachador = editCodigoDespachador.getText().toString().trim();
            String codigoTransportista = editCodigoTransportista.getText().toString().trim();

            if (codigoViaje.isEmpty() || codigoDespachador.isEmpty() || codigoTransportista.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ir a ScanActivity con los datos
            Intent intent = new Intent(ScanActivity.this, MainActivity.class);
            intent.putExtra("viaje", codigoViaje);
            intent.putExtra("despachador", codigoDespachador);
            intent.putExtra("transportista", codigoTransportista);
            startActivity(intent);
        });
    }
}

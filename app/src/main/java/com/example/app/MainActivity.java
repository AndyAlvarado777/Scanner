package com.example.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.honeywell.aidc.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class MainActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private static final String TAG = "HoneywellScanner";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private LinearLayout scanContainer;
    private Button btnExportar;
    private EditText editCodigoViaje;
    private EditText editCodigoDespachador;
    private EditText editCodigoTransportista;
    private String codigoDespachador = "";
    private String codigoTransportista = "";

    private AidcManager manager;
    private BarcodeReader barcodeReader;

    private List<Escaneo> listaEscaneos = new ArrayList<>();

    private static class Escaneo {
        String codigoBarra;
        String cantidad;

        Escaneo(String codigoBarra, String cantidad) {
            this.codigoBarra = codigoBarra;
            this.cantidad = cantidad;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanContainer = findViewById(R.id.scanContainer);
        btnExportar = findViewById(R.id.btnExportar);
        editCodigoViaje = findViewById(R.id.editCodigoViaje);
        editCodigoDespachador = findViewById(R.id.editCodigoDespachador);
        editCodigoTransportista = findViewById(R.id.editCodigoTransportista);


        btnExportar.setOnClickListener(v -> {
            if (checkPermission()) {
                exportarExcel();
            } else {
                requestPermission();
            }
        });

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                } catch (InvalidScannerNameException e) {
                    throw new RuntimeException(e);
                }

                if (barcodeReader != null) {
                    try {
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        barcodeReader.addBarcodeListener(barcodeListener);
                        barcodeReader.claim();
                    } catch (Exception e) {
                        Log.e(TAG, "Error inicializando lector: " + e.getMessage());
                    }
                }
            }
        });
    }

    private final BarcodeReader.BarcodeListener barcodeListener = new BarcodeReader.BarcodeListener() {
        @Override
        public void onBarcodeEvent(final BarcodeReadEvent event) {
            runOnUiThread(() -> agregarBloqueEscaneo(event.getBarcodeData()));
        }

        @Override
        public void onFailureEvent(BarcodeFailureEvent event) {
            Log.e(TAG, "Falló el escaneo");
        }
    };


    private void agregarBloqueEscaneo(String data) {
        // 1. Obtener el LayoutInflater para poder "inflar" el layout XML en un objeto View.
        LayoutInflater inflater = LayoutInflater.from(this);

        // 2. Inflar el layout. El último parámetro 'false' es importante para que
        //    el sistema no lo agregue al 'scanContainer' todavía. Lo haremos manualmente.
        View itemView = inflater.inflate(R.layout.item_scan, scanContainer, false);

        // 3. Encontrar las vistas DENTRO del layout que acabamos de inflar.
        TextView codigoTexto = itemView.findViewById(R.id.tv_scan_data);
        TextInputEditText cantidadInput = itemView.findViewById(R.id.et_quantity);

        // 4. Configurar los datos y los listeners en las vistas.
        codigoTexto.setText(data);

        cantidadInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Ocultar el teclado cuando el usuario presiona "Hecho"
                cantidadInput.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(cantidadInput.getWindowToken(), 0);
                }
                return true; // Indica que hemos manejado el evento.
            }
            return false; // No hemos manejado otros eventos.
        });

        // 5. Agregar la vista completamente configurada al contenedor principal.
        scanContainer.addView(itemView); // El '0' agrega el nuevo elemento al principio

    }

    private void exportarExcel() {
        // Obtener datos de los campos
        String codigoViaje = editCodigoViaje.getText().toString().trim();
        codigoDespachador = editCodigoDespachador.getText().toString().trim();
        codigoTransportista = editCodigoTransportista.getText().toString().trim();

        // Validación
        if (codigoViaje.isEmpty() || codigoDespachador.isEmpty() || codigoTransportista.isEmpty()) {
            Toast.makeText(this, "Ingrese todos los datos: viaje, despachador y transportista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpiar lista y recopilar datos de la UI
        listaEscaneos.clear();
        for (int i = 0; i < scanContainer.getChildCount(); i++) {
            View fila = scanContainer.getChildAt(i);

            TextView codigoView = fila.findViewById(R.id.tv_scan_data);
            EditText cantidadView = fila.findViewById(R.id.et_quantity);

            if (codigoView != null && cantidadView != null) {
                String codigoBarra = codigoView.getText().toString().trim();
                String cantidad = cantidadView.getText().toString().trim();

                if (!cantidad.isEmpty()) {
                    listaEscaneos.add(new Escaneo(codigoBarra, cantidad));
                }
            }
        }

        if (listaEscaneos.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setUseTemporaryFileDuringWrite(true);

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/Download/MisExcel");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "Escaneos_" + codigoViaje + "_" + System.currentTimeMillis() + ".xls";
            File file = new File(dir, fileName);

            WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("Escaneos", 0);

            // Encabezados
            sheet.addCell(new Label(0, 0, "Viaje"));
            sheet.addCell(new Label(1, 0, "Codigo Item"));
            sheet.addCell(new Label(2, 0, "Serie (default:00000000000000000000)"));
            sheet.addCell(new Label(3, 0, "Codigo despachador"));
            sheet.addCell(new Label(4, 0, "Codigo transportista"));
            sheet.addCell(new Label(5, 0, "Cantidad"));

            // Datos
            for (int i = 0; i < listaEscaneos.size(); i++) {
                Escaneo escaneo = listaEscaneos.get(i);
                sheet.addCell(new Label(0, i + 1, codigoViaje));
                sheet.addCell(new Label(1, i + 1, escaneo.codigoBarra));
                sheet.addCell(new Label(2, i + 1, "00000000000000000000")); // Valor fijo
                sheet.addCell(new Label(3, i + 1, codigoDespachador));
                sheet.addCell(new Label(4, i + 1, codigoTransportista));
                sheet.addCell(new Label(5, i + 1, escaneo.cantidad));
            }

            workbook.write();
            workbook.close();

            Toast.makeText(this, "Archivo guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Error exportando Excel: " + e.getMessage());
            Toast.makeText(this, "Error al exportar Excel", Toast.LENGTH_LONG).show();
        }
    }


    // Permisos para Android 6+
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportarExcel();
            } else {
                Toast.makeText(this, "Permiso denegado para escribir archivos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (barcodeReader != null) {
            barcodeReader.removeBarcodeListener(barcodeListener);
            barcodeReader.release();
        }
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }
}

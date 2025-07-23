package com.example.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.honeywell.aidc.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap; // Para mantener el orden de inserción de escaneos únicos

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HoneywellScanner";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private LinearLayout scanContainer;
    private Button btnExportar;
    private Button btnClearFields; // Declaración del nuevo botón
    private TextInputEditText editCodigoViaje; // Usar TextInputEditText para consistencia
    private TextInputEditText editCodigoDespachador; // Usar TextInputEditText para consistencia
    private TextInputEditText editCodigoTransportista; // Usar TextInputEditText para consistencia

    private TextView tvProductosCount; // Declaración del TextView para el contador de productos
    private TextView tvCantidadTotal;  // Declaración del TextView para el contador de cantidad total

    private AidcManager manager;
    private BarcodeReader barcodeReader;

    // Usaremos un Map para gestionar los escaneos. Si un código se escanea de nuevo,
    // actualizamos su cantidad en lugar de añadir un nuevo ítem en la UI.
    // La clave será el código de barras, el valor será la vista del ítem escaneado.
    private Map<String, View> scannedItemViews = new LinkedHashMap<>();

    private static class Escaneo {
        String codigoBarra;
        int cantidad; // Cambiado a int para facilitar cálculos

        Escaneo(String codigoBarra, int cantidad) {
            this.codigoBarra = codigoBarra;
            this.cantidad = cantidad;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar vistas
        scanContainer = findViewById(R.id.scanContainer);
        btnExportar = findViewById(R.id.btnExportar);
        btnClearFields = findViewById(R.id.btnClearFields); // ¡IMPORTANTE: Inicializar el nuevo botón!

        // Usar TextInputEditText directamente para los EditTexts
        editCodigoViaje = findViewById(R.id.editCodigoViaje);
        editCodigoDespachador = findViewById(R.id.editCodigoDespachador);
        editCodigoTransportista = findViewById(R.id.editCodigoTransportista);

        // ¡IMPORTANTE: Inicializar los TextViews de los contadores!
        tvProductosCount = findViewById(R.id.tv_productos_count);
        tvCantidadTotal = findViewById(R.id.tv_cantidad_total);

        // Establecer listeners
        btnExportar.setOnClickListener(v -> {
            if (checkPermission()) {
                exportarExcel();
            } else {
                requestPermission();
            }
        });

        // Listener para el nuevo botón de limpiar campos
        btnClearFields.setOnClickListener(v -> {
            limpiarCampos();
        });


        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                } catch (InvalidScannerNameException e) {
                    Log.e(TAG, "Error al crear BarcodeReader: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error al inicializar el escáner.", Toast.LENGTH_SHORT).show();
                    return; // Importante para evitar NullPointerException si barcodeReader es null
                }

                if (barcodeReader != null) {
                    try {
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        barcodeReader.addBarcodeListener(barcodeListener);
                        barcodeReader.claim();
                    } catch (UnsupportedPropertyException | ScannerUnavailableException e) {
                        Log.e(TAG, "Error inicializando lector: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error al configurar el escáner.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Actualizar contadores al inicio (deberían ser 0)
        updateCounters();
    }

    private final BarcodeReader.BarcodeListener barcodeListener = new BarcodeReader.BarcodeListener() {
        @Override
        public void onBarcodeEvent(final BarcodeReadEvent event) {
            runOnUiThread(() -> agregarBloqueEscaneo(event.getBarcodeData()));
        }

        @Override
        public void onFailureEvent(BarcodeFailureEvent event) {
            Log.e(TAG, "Falló el escaneo");
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de escaneo", Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú; esto agrega ítems a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Maneja los clics en los ítems del menú.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Navegar a SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void agregarBloqueEscaneo(String data) {
        // Implementación de manejo de escaneos duplicados (mejora de UX)
        if (scannedItemViews.containsKey(data)) {
            // El código ya existe, actualiza su cantidad
            View existingItemView = scannedItemViews.get(data);
            TextInputEditText cantidadInput = existingItemView.findViewById(R.id.et_quantity);
            if (cantidadInput != null) {
                try {
                    int currentQuantity = Integer.parseInt(cantidadInput.getText().toString());
                    cantidadInput.setText(String.valueOf(currentQuantity + 1)); // Incrementa en 1
                    cantidadInput.requestFocus(); // Pone el foco en el campo actualizado
                    // Opcional: Vibrar o mostrar un Toast para indicar que se actualizó
                    Toast.makeText(this, "Cantidad actualizada para: " + data, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear cantidad existente: " + cantidadInput.getText().toString());
                    cantidadInput.setText("1"); // Si hay un error, resetea a 1
                }
            }
        } else {
            // El código no existe, crea un nuevo ítem
            LayoutInflater inflater = LayoutInflater.from(this);
            View newItemView = inflater.inflate(R.layout.item_scan, scanContainer, false);

            TextView tvItemNumber = newItemView.findViewById(R.id.tv_item_number);
            TextView codigoTexto = newItemView.findViewById(R.id.tv_scan_data);
            TextInputEditText cantidadInput = newItemView.findViewById(R.id.et_quantity);

            codigoTexto.setText(data);
            cantidadInput.setText("1"); // Valor por defecto 1

            // Almacena la vista del nuevo ítem en el mapa
            scannedItemViews.put(data, newItemView);

            cantidadInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    cantidadInput.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(cantidadInput.getWindowToken(), 0);
                    }
                    updateCounters(); // Actualizar contadores al finalizar la edición de cantidad
                    return true;
                }
                return false;
            });

            // Listener para cuando el foco cambia y se actualiza la cantidad
            cantidadInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateCounters(); // Actualiza contadores cuando el campo pierde el foco
                }
            });

            scanContainer.addView(newItemView); // Añade el nuevo ítem al final de la lista.
        }

        // Después de agregar o actualizar, asegurar que el scroll se vaya al final
        scanContainer.post(() -> {
            if (scanContainer.getParent() instanceof ScrollView) {
                ScrollView parentScrollView = (ScrollView) scanContainer.getParent();
                parentScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        updateCounters(); // Siempre actualiza los contadores después de cualquier operación de escaneo
    }


    private static final String REMITENTE_EMAIL = "alvaradoandy097@gmail.com";
    // ¡ADVERTENCIA DE SEGURIDAD! Esto no debería estar aquí. Mover a un lugar más seguro.
    private static final String REMITENTE_APP_PASSWORD = "wkzp zdrp wnxh gtdq";

    private void exportarExcel() {
        String codigoViaje = editCodigoViaje.getText().toString().trim();
        String codigoDespachador = editCodigoDespachador.getText().toString().trim();
        String codigoTransportista = editCodigoTransportista.getText().toString().trim();

        if (codigoViaje.isEmpty() || codigoDespachador.isEmpty() || codigoTransportista.isEmpty()) {
            Toast.makeText(this, "Ingrese todos los datos: viaje, despachador y transportista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recopila los datos de la UI en una lista temporal primero.
        List<Escaneo> datosParaExportar = new ArrayList<>();
        // Ahora iteramos sobre las vistas almacenadas en scannedItemViews para asegurar que reflejamos la UI
        for (Map.Entry<String, View> entry : scannedItemViews.entrySet()) {
            View fila = entry.getValue(); // Obtenemos la vista del ítem
            TextView codigoView = fila.findViewById(R.id.tv_scan_data);
            TextInputEditText cantidadView = fila.findViewById(R.id.et_quantity);

            if (codigoView != null && cantidadView != null) {
                String codigoBarra = codigoView.getText().toString().trim();
                String cantidadStr = cantidadView.getText().toString().trim();
                int cantidad = 0;
                try {
                    cantidad = Integer.parseInt(cantidadStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Cantidad inválida para " + codigoBarra + ": " + cantidadStr);
                    Toast.makeText(this, "Cantidad inválida en un ítem (" + codigoBarra + "). Revise los datos.", Toast.LENGTH_LONG).show();
                    return; // Detener exportación si hay datos inválidos
                }

                if (cantidad > 0) { // Solo exportar ítems con cantidad > 0
                    datosParaExportar.add(new Escaneo(codigoBarra, cantidad));
                }
            }
        }

        if (datosParaExportar.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        File archivoExcel = null;
        try {
            File dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "MisExcel");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "Escaneos_" + codigoViaje + "_" + System.currentTimeMillis() + ".xls";
            archivoExcel = new File(dir, fileName);

            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setUseTemporaryFileDuringWrite(true);

            WritableWorkbook workbook = Workbook.createWorkbook(archivoExcel, wbSettings);
            WritableSheet sheet = workbook.createSheet("Escaneos", 0);

            // Encabezados
            sheet.addCell(new Label(0, 0, "Viaje"));
            sheet.addCell(new Label(1, 0, "Codigo Item"));
            sheet.addCell(new Label(2, 0, "Serie (default:00000000000000000000)"));
            sheet.addCell(new Label(3, 0, "Codigo despachador"));
            sheet.addCell(new Label(4, 0, "Codigo transportista"));
            sheet.addCell(new Label(5, 0, "Cantidad"));

            // Datos
            for (int i = 0; i < datosParaExportar.size(); i++) {
                Escaneo escaneo = datosParaExportar.get(i);
                sheet.addCell(new Label(0, i + 1, codigoViaje));
                sheet.addCell(new Label(1, i + 1, escaneo.codigoBarra));
                sheet.addCell(new Label(2, i + 1, "00000000000000000000"));
                sheet.addCell(new Label(3, i + 1, codigoDespachador));
                sheet.addCell(new Label(4, i + 1, codigoTransportista));
                sheet.addCell(new Label(5, i + 1, String.valueOf(escaneo.cantidad))); // Convertir int a String
            }

            workbook.write();
            workbook.close();

            Toast.makeText(this, "Archivo guardado en: " + archivoExcel.getAbsolutePath(), Toast.LENGTH_LONG).show();

            String destinatario = "andy.alvarado@pbs.group";
            String asunto = "Archivo de escaneos para el viaje: " + codigoViaje;
            String cuerpo = "Adjunto archivo Excel generado por la app. \n\n" +
                    "Codigo Viaje: " + codigoViaje + "\n" +
                    "Codigo Despachador: " + codigoDespachador + "\n" +
                    "Codigo Transportista: " + codigoTransportista;

            MailSender.sendMailWithAttachment(
                    this,
                    REMITENTE_EMAIL,
                    REMITENTE_APP_PASSWORD,
                    destinatario,
                    asunto,
                    cuerpo,
                    archivoExcel
            );

            Toast.makeText(this, "Datos exportados y enviados. Limpia los campos si lo deseas.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("ExportarExcel", "Error exportando o enviando Excel", e);
            Toast.makeText(this, "Error al generar o enviar el archivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Nuevo método para limpiar los campos de texto y la lista de escaneos.
     */
    private void limpiarCampos() {
        // Limpiar campos de información del viaje
        editCodigoViaje.setText("");
        editCodigoDespachador.setText("");
        editCodigoTransportista.setText("");

        // Limpiar la lista de escaneos en memoria y en la UI
        scannedItemViews.clear(); // Limpiar el mapa de vistas
        scanContainer.removeAllViews(); // Eliminar todas las vistas de escaneo del LinearLayout

        // Actualizar los contadores a cero
        updateCounters();

        Toast.makeText(this, "Campos limpiados correctamente", Toast.LENGTH_SHORT).show();
    }

    /**
     * Actualiza los TextViews de productos y cantidad total.
     * Recalcula los valores leyendo directamente de las vistas en scanContainer.
     * Esto asegura que los contadores reflejen el estado actual de la UI,
     * incluso si la cantidad es modificada manualmente por el usuario.
     */
    private void updateCounters() {
        int totalProductos = scannedItemViews.size(); // Número de ítems únicos
        int totalCantidad = 0;

        // Iterar sobre las vistas en el scanContainer para obtener las cantidades actuales
        for (int i = 0; i < scanContainer.getChildCount(); i++) {
            View itemView = scanContainer.getChildAt(i);
            TextInputEditText etQuantity = itemView.findViewById(R.id.et_quantity);
            if (etQuantity != null && !etQuantity.getText().toString().isEmpty()) {
                try {
                    totalCantidad += Integer.parseInt(etQuantity.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear cantidad: " + etQuantity.getText().toString());
                    // Si hay un error de formato, podemos considerar la cantidad como 0 o ignorarla
                    // Podrías mostrar un Toast aquí si prefieres alertar al usuario
                }
            }

            // También actualiza el número de orden visible en el ítem (solo si el tvItemNumber existe)
            TextView tvItemNumber = itemView.findViewById(R.id.tv_item_number);
            if (tvItemNumber != null) {
                tvItemNumber.setText(String.valueOf(i + 1));
            }
        }

        // Asegúrate de que tvProductosCount y tvCantidadTotal no sean null antes de usarlos
        if (tvProductosCount != null) {
            tvProductosCount.setText(String.valueOf(totalProductos));
        }
        if (tvCantidadTotal != null) {
            tvCantidadTotal.setText(String.valueOf(totalCantidad));
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
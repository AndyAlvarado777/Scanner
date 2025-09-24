package com.example.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils; // Importar TextUtils
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app.dao.DespachadorDao;
import com.example.app.DespachadorActivity;
import com.example.app.dao.TransportistaDao;
import com.example.app.model.Despachador;
import com.example.app.model.Transportista;
import com.example.app.dao.ProductoDao; // <-- AÑADE ESTE IMPORT
import com.example.app.model.Producto;   // <-- AÑADE ESTE IMPORT
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

// Importaciones de las nuevas clases SQLite
import com.example.app.dao.EmailConfigDao;
import com.example.app.model.EmailConfig;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HoneywellScanner";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private static final int GESTIONAR_DESPACHADORES_REQUEST = 1;
    private static final int GESTIONAR_TRANSPORTISTAS_REQUEST = 2;

    private LinearLayout scanContainer;
    private Button btnExportar;
    private Button btnClearFields;
    private TextInputEditText editCodigoViaje;
    private AutoCompleteTextView autoCompleteDespachador;
    private DespachadorDao despachadorDao;
    private List<Despachador> despachadorList;
    private String selectedDespachadorCode;
    private AutoCompleteTextView autoCompleteTransportista;
    private TransportistaDao transportistaDao; // Necesitas el DAO para acceder a los datos

    private List<Transportista> transportistaList; // Para almacenar la lista de transportistas

    private String selectedTransportistaCode;
    private TextView tvProductosCount;
    private TextView tvCantidadTotal;

    private AidcManager manager;
    private BarcodeReader barcodeReader;

    private ProductoDao productoDao;

    private Map<String, View> scannedItemViews = new LinkedHashMap<>(); // Mantener el orden

    // Clase interna para representar un escaneo
    private static class Escaneo {
        String codigoBarra;
        int cantidad;

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
        btnClearFields = findViewById(R.id.btnClearFields);

        editCodigoViaje = findViewById(R.id.editCodigoViaje);
        autoCompleteDespachador = findViewById(R.id.autoCompleteDespachador);
        autoCompleteTransportista = findViewById(R.id.autoCompleteTransportista);

        despachadorDao = new DespachadorDao(this);
        loadDespachadores();

        transportistaDao = new TransportistaDao(this);
        loadTransportistas();

        productoDao = new ProductoDao(this);

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

        btnClearFields.setOnClickListener(v -> {
            limpiarCampos();
        });


        // Configuración del escáner Honeywell
        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                } catch (InvalidScannerNameException e) {
                    Log.e(TAG, "Error al crear BarcodeReader: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error al inicializar el escáner.", Toast.LENGTH_SHORT).show();
                    return;
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

        updateCounters(); // Actualizar contadores al inicio (deberían ser 0)
    }

    // Listener para los eventos del lector de códigos de barras
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

    // Métodos para el menú de la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_transportistas) { // <-- ¡Aquí está la nueva lógica!
            // Creamos un Intent para navegar a la nueva actividad
            Intent intent = new Intent(this, TransportistasActivity.class); // Asumo que se llama así
            startActivityForResult(intent, GESTIONAR_TRANSPORTISTAS_REQUEST);
            return true;
        }
        else if (id == R.id.action_despachadores) {
            // Creamos un Intent para navegar a la nueva actividad
            Intent intent = new Intent(this, DespachadorActivity.class);
            startActivityForResult(intent, GESTIONAR_DESPACHADORES_REQUEST);
            return true;
        }
        // Si tuvieras más ítems en main_menu.xml, los manejarías aquí.
        // Por ejemplo, R.id.action_trips para la Fase 2

        return super.onOptionsItemSelected(item);
    }

    // Método para añadir o actualizar un elemento escaneado en la UI
    // 3. REEMPLAZA TU MÉTODO agregarBloqueEscaneo CON ESTA NUEVA VERSIÓN MEJORADA
    private void agregarBloqueEscaneo(String scannedData) {
        // Lógica de traducción de código
        Producto productoMapeado = productoDao.findProductoByProveedorCodigo(scannedData);

        final String codigoFinal; // El código que se usará para la lógica y el guardado
        final String textoAMostrar; // El texto que verá el usuario en la UI

        if (productoMapeado != null) {
            // Se encontró una correspondencia
            codigoFinal = productoMapeado.getCodigoInterno();
            // Para mejor UX, mostramos ambos códigos
            textoAMostrar = codigoFinal + " (scan: " + scannedData + ")";
            Toast.makeText(this, "Código traducido", Toast.LENGTH_SHORT).show();
        } else {
            // No se encontró correspondencia, usamos el código original
            codigoFinal = scannedData;
            textoAMostrar = scannedData;
        }

        // Usamos el 'codigoFinal' como clave para evitar duplicados del producto ya traducido
        if (scannedItemViews.containsKey(codigoFinal)) {
            // El producto ya existe en la lista, actualiza su cantidad
            View existingItemView = scannedItemViews.get(codigoFinal);
            TextInputEditText cantidadInput = existingItemView.findViewById(R.id.et_quantity);
            if (cantidadInput != null) {
                try {
                    int currentQuantity = Integer.parseInt(cantidadInput.getText().toString());
                    cantidadInput.setText(String.valueOf(currentQuantity + 1));
                    cantidadInput.requestFocus();
                } catch (NumberFormatException e) {
                    cantidadInput.setText("1");
                }
            }
        } else {
            // El producto no existe, crea un nuevo ítem
            LayoutInflater inflater = LayoutInflater.from(this);
            View newItemView = inflater.inflate(R.layout.item_scan, scanContainer, false);

            TextView codigoTexto = newItemView.findViewById(R.id.tv_scan_data);
            TextInputEditText cantidadInput = newItemView.findViewById(R.id.et_quantity);

            codigoTexto.setText(textoAMostrar); // Mostramos el texto formateado
            cantidadInput.setText("1");

            // IMPORTANTE: Guardamos el 'codigoFinal' en el tag para poder borrarlo correctamente
            newItemView.setTag(codigoFinal);
            scannedItemViews.put(codigoFinal, newItemView);

            // --- NUEVA LÓGICA DE LISTENERS DE PRESIÓN ---
// Listener para una pulsación normal (editar)
            newItemView.setOnClickListener(v -> showEditDialog(codigoFinal, codigoTexto, cantidadInput));

// Listener para una pulsación larga (eliminar)
            newItemView.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(newItemView, codigoFinal);
                return true; // Retorna true para consumir el evento y evitar el click normal
            });

            // ... (tus otros listeners para cantidadInput no cambian)
            cantidadInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    cantidadInput.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(cantidadInput.getWindowToken(), 0);
                    }
                    updateCounters();
                    return true;
                }
                return false;
            });

            cantidadInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateCounters();
                }
            });


            scanContainer.addView(newItemView);
        }

        // ... (tu código para hacer scroll y actualizar contadores no cambia)
        scanContainer.post(() -> {
            View parent = (View) scanContainer.getParent();
            if (parent instanceof ScrollView) {
                ScrollView parentScrollView = (ScrollView) parent;
                parentScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        updateCounters();
    }

    // Método para exportar a Excel y enviar correo
    private void exportarExcel() {
        // Inicializar el DAO
        EmailConfigDao emailConfigDao = new EmailConfigDao(this);
        // Obtener la configuración de correo de la base de datos
        EmailConfig emailConfig = emailConfigDao.getConfig();

        // Validar si la configuración de correo existe y está completa
        if (emailConfig == null || TextUtils.isEmpty(emailConfig.getRemitenteEmail()) ||
                TextUtils.isEmpty(emailConfig.getAppPassword()) ||
                TextUtils.isEmpty(emailConfig.getDestinatariosEmails())) {
            Toast.makeText(this, "Error: Por favor, configure el correo en la sección de Configuraciones antes de exportar.", Toast.LENGTH_LONG).show();
            return; // Detener el proceso si la configuración no existe o está incompleta
        }

        String codigoViaje = editCodigoViaje.getText().toString().trim();
        String codigoDespachador = selectedDespachadorCode;
        String codigoTransportista = selectedTransportistaCode;

        if (TextUtils.isEmpty(codigoDespachador)) {
            Toast.makeText(this, "Por favor, seleccione un despachador de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(codigoTransportista)) {
            Toast.makeText(this, "Por favor, seleccione un transportista de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (codigoViaje.isEmpty() || codigoDespachador.isEmpty() || codigoTransportista.isEmpty()) {
            Toast.makeText(this, "Ingrese todos los datos: viaje, despachador y transportista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recopila los datos de la UI en una lista temporal para la exportación.
        List<Escaneo> datosParaExportar = new ArrayList<>();
        for (Map.Entry<String, View> entry : scannedItemViews.entrySet()) {
            // La clave del mapa ('entry.getKey()') es ahora nuestro 'codigoFinal' limpio.
            String codigoBarra = entry.getKey();
            View fila = entry.getValue();
            TextInputEditText cantidadView = fila.findViewById(R.id.et_quantity);

            if (cantidadView != null) {
                String cantidadStr = cantidadView.getText().toString().trim();
                int cantidad = 0;
                try {
                    cantidad = Integer.parseInt(cantidadStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Cantidad inválida para " + codigoBarra + ": " + cantidadStr);
                    Toast.makeText(this, "Cantidad inválida en un ítem (" + codigoBarra + "). Revise los datos.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (cantidad > 0) {
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
                sheet.addCell(new Label(5, i + 1, String.valueOf(escaneo.cantidad)));
            }

            workbook.write();
            workbook.close();

            Toast.makeText(this, "Archivo guardado en: " + archivoExcel.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Usar los valores obtenidos de la base de datos
            String remitenteEmail = emailConfig.getRemitenteEmail();
            String appPassword = emailConfig.getAppPassword();
            String destinatarios = emailConfig.getDestinatariosEmails(); // Esto puede ser una cadena con múltiples correos

            String asunto = "Archivo de escaneos para el viaje: " + codigoViaje;
            String cuerpo = "Adjunto archivo Excel generado por la app. \n\n" +
                    "Codigo Viaje: " + codigoViaje + "\n" +
                    "Codigo Despachador: " + codigoDespachador + "\n" +
                    "Codigo Transportista: " + codigoTransportista;

            MailSender.sendMailWithAttachment(
                    this,
                    remitenteEmail,
                    appPassword,
                    destinatarios, // Se pasa el String completo, MailSender debe parsearlo si hay múltiples
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

    private void loadTransportistas() {
        transportistaList = transportistaDao.getAllTransportistas();

        // Crear una lista de solo nombres para mostrar en el AutoCompleteTextView
        List<String> nombresTransportistas = new ArrayList<>();
        for (Transportista t : transportistaList) {
            nombresTransportistas.add(t.getNombreTransportista());
        }

        // Configurar el ArrayAdapter para el AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombresTransportistas);
        autoCompleteTransportista.setAdapter(adapter);

        // Manejar la selección del usuario
        autoCompleteTransportista.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            // Buscar el código del transportista seleccionado
            for (Transportista t : transportistaList) {
                if (t.getNombreTransportista().equals(selectedName)) {
                    selectedTransportistaCode = t.getCodigoTransportista();
                    Log.d("MainActivity", "Transportista seleccionado: " + selectedName + ", Código: " + selectedTransportistaCode);
                    break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Solo nos interesa si el resultado fue exitoso (RESULT_OK)
        if (resultCode != RESULT_OK) {
            return;
        }

        // Usamos un switch para manejar las diferentes respuestas
        switch (requestCode) {
            case GESTIONAR_DESPACHADORES_REQUEST:
                // El usuario modificó los despachadores, recargamos su lista
                Toast.makeText(this, "Actualizando lista de despachadores...", Toast.LENGTH_SHORT).show();
                loadDespachadores();
                break;

            case GESTIONAR_TRANSPORTISTAS_REQUEST:
                // ✅ El usuario modificó los transportistas, recargamos su lista
                Toast.makeText(this, "Actualizando lista de transportistas...", Toast.LENGTH_SHORT).show();
                loadTransportistas(); // Llama a tu método para cargar transportistas
                break;

            // Aquí podrías añadir más 'case' en el futuro
        }
    }
    private void loadDespachadores() {
        despachadorList = despachadorDao.getAllDespachador();

        List<String> nombresDespachadores = new ArrayList<>();
        for (Despachador d : despachadorList) {
            nombresDespachadores.add(d.getNombreDespachador());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombresDespachadores);
        autoCompleteDespachador.setAdapter(adapter);

        autoCompleteDespachador.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (Despachador d : despachadorList) {
                if (d.getNombreDespachador().equals(selectedName)) {
                    selectedDespachadorCode = d.getCodigoDespachador();
                    Log.d("MainActivity", "Despachador seleccionado: " + selectedName + ", Código: " + selectedDespachadorCode);
                    break;
                }
            }
        });
    }

    /**
     * Nuevo método para limpiar los campos de texto y la lista de escaneos.
     */
    // Código en com.example.app/MainActivity.java

    /**
     * Muestra un diálogo de confirmación antes de limpiar los campos.
     */
    private void limpiarCampos() {
        new AlertDialog.Builder(this)
                .setTitle("Limpiar Campos")
                .setMessage("¿Estás seguro de que quieres borrar todos los datos de la pantalla?")
                .setPositiveButton("Sí, limpiar", (dialog, which) -> {
                    // El usuario hizo clic en "Sí, limpiar". Procedemos a la limpieza.
                    realizarLimpiezaDeCampos();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // El usuario canceló la acción. No hacemos nada.
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Nuevo método privado que contiene la lógica real de limpieza.
     * Se llama solo después de la confirmación del usuario.
     */
    private void realizarLimpiezaDeCampos() {
        // Aquí se mueve la lógica de limpieza que ya tenías
        editCodigoViaje.setText("");
        // Limpia el campo del despachador
        autoCompleteDespachador.setText("");
        selectedDespachadorCode = null;

        autoCompleteTransportista.setText(""); // Limpia el campo
        selectedTransportistaCode = null; // Reinicia la variable de código

        scannedItemViews.clear();
        scanContainer.removeAllViews();

        updateCounters();

        Toast.makeText(this, "Campos limpiados correctamente", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(View itemView, String codeToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar elemento")
                .setMessage("¿Estás seguro de que quieres eliminar el código " + codeToDelete + "?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    scannedItemViews.remove(codeToDelete);
                    scanContainer.removeView(itemView);
                    updateCounters();
                    Toast.makeText(this, "Elemento eliminado: " + codeToDelete, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEditDialog(String oldCode, TextView tvScanData, TextInputEditText etQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_code, null);
        builder.setView(dialogView);

        final TextInputEditText etEditCode = dialogView.findViewById(R.id.et_edit_code);
        etEditCode.setText(oldCode); // Muestra el código actual

        builder.setPositiveButton("Editar", (dialog, which) -> {
            String newCode = etEditCode.getText().toString().trim();
            if (!newCode.isEmpty() && !newCode.equals(oldCode)) {
                // Eliminar el viejo ítem
                scannedItemViews.remove(oldCode);
                // Actualizar la vista con el nuevo código
                tvScanData.setText(newCode);

                // Re-mapear el nuevo código en scannedItemViews
                View cardView = (View) tvScanData.getParent().getParent(); // Obtiene la MaterialCardView
                cardView.setTag(newCode); // Actualiza el tag
                scannedItemViews.put(newCode, cardView); // Re-agrega con la nueva clave

                // Opcional: enfocar el input de cantidad
                etQuantity.requestFocus();
                // Mostrar un Toast o SnackBar
                Toast.makeText(this, "Código actualizado a: " + newCode, Toast.LENGTH_SHORT).show();
            } else if (newCode.equals(oldCode)) {
                Toast.makeText(this, "El código no ha cambiado.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "El código no puede estar vacío.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Actualiza los TextViews de productos y cantidad total.
     * Recalcula los valores leyendo directamente de las vistas en scanContainer.
     */
    private void updateCounters() {
        int totalProductos = scannedItemViews.size();
        int totalCantidad = 0;

        for (int i = 0; i < scanContainer.getChildCount(); i++) {
            View itemView = scanContainer.getChildAt(i);
            TextInputEditText etQuantity = itemView.findViewById(R.id.et_quantity);
            if (etQuantity != null && !etQuantity.getText().toString().isEmpty()) {
                try {
                    totalCantidad += Integer.parseInt(etQuantity.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear cantidad: " + etQuantity.getText().toString());
                    // Puedes manejar este error como prefieras, por ejemplo, ignorar esa cantidad
                }
            }

            // Actualiza el número de orden visible en el ítem
            TextView tvItemNumber = itemView.findViewById(R.id.tv_item_number);
            if (tvItemNumber != null) {
                tvItemNumber.setText(String.valueOf(i + 1));
            }
        }

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

    // Puedes añadir onResume() y onPause() si es necesario para el ciclo de vida del escáner Honeywell
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim(); // Reclamar el escáner cuando la actividad vuelve a estar activa
            } catch (ScannerUnavailableException e) {
                Log.e(TAG, "Error al reclamar el escáner en onResume: " + e.getMessage());
                Toast.makeText(this, "Escáner no disponible.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            barcodeReader.release(); // Liberar el escáner cuando la actividad se pausa
        }
    }
}
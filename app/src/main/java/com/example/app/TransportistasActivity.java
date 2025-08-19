package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.TransportistaAdapter;
import com.example.app.dao.TransportistaDao;
import com.example.app.TransportistaDialogFragment;
import com.example.app.model.Transportista;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TransportistasActivity extends AppCompatActivity
        implements TransportistaDialogFragment.OnTransportistaDialogListener,
        TransportistaAdapter.OnTransportistaInteractionListener {

    private static final String TAG = "TransportistasActivity";

    private RecyclerView recyclerView;
    private TransportistaAdapter adapter;
    private TransportistaDao transportistaDao;
    private List<Transportista> transportistaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transportistas);

        // Configuración del Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_transportistas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Inicializar DAO
        transportistaDao = new TransportistaDao(this);

        // Configuración de RecyclerView
        recyclerView = findViewById(R.id.recycler_view_transportistas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransportistaAdapter(transportistaList, this);
        recyclerView.setAdapter(adapter);

        // Cargar los datos iniciales
        loadTransportistas();

        // Configuración del Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab_add_transportista);
        fab.setOnClickListener(view -> showAddTransportistaDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadTransportistas() {
        transportistaList = transportistaDao.getAllTransportistas();
        adapter.updateList(transportistaList);
        Log.d(TAG, "Transportistas cargados: " + transportistaList.size());
    }

    private void showAddTransportistaDialog() {
        TransportistaDialogFragment dialog = TransportistaDialogFragment.newInstance(null);
        dialog.show(getSupportFragmentManager(), "TransportistaDialogFragment");
    }

    // Método para manejar la adición desde el diálogo
    @Override
    public void onSaveTransportista(Transportista transportista) {
        long result = transportistaDao.insertTransportista(transportista);
        if (result != -1) {
            Toast.makeText(this, "Transportista agregado", Toast.LENGTH_SHORT).show();
            loadTransportistas(); // Recargar la lista
        } else {
            Toast.makeText(this, "Error al agregar. El código ya existe.", Toast.LENGTH_LONG).show();
        }
    }

    // Método para manejar la actualización desde el diálogo
    @Override
    public void onUpdateTransportista(Transportista transportista) {
        int rowsAffected = transportistaDao.updateTransportista(transportista);
        if (rowsAffected > 0) {
            Toast.makeText(this, "Transportista actualizado", Toast.LENGTH_SHORT).show();
            loadTransportistas(); // Recargar la lista
        } else {
            Toast.makeText(this, "Error al actualizar. Transportista no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para manejar el clic en un ítem de la lista (Editar)
    @Override
    public void onEditTransportista(Transportista transportista) {
        TransportistaDialogFragment dialog = TransportistaDialogFragment.newInstance(transportista);
        dialog.show(getSupportFragmentManager(), "TransportistaDialogFragment");
    }

    // Método para manejar el clic largo en un ítem de la lista (Borrar)
    @Override
    public void onDeleteTransportista(Transportista transportista) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Transportista")
                .setMessage("¿Estás seguro de que quieres eliminar a " + transportista.getNombreTransportista() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int rowsAffected = transportistaDao.deleteTransportista(transportista.getCodigoTransportista());
                    if (rowsAffected > 0) {
                        Toast.makeText(this, "Transportista eliminado", Toast.LENGTH_SHORT).show();
                        loadTransportistas();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
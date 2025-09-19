package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.adapter.DespachadorAdapter;
import com.example.app.dao.DespachadorDao;
import com.example.app.dialog.DespachadorDialogFragment;
import com.example.app.model.Despachador;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DespachadorActivity extends AppCompatActivity
        implements DespachadorDialogFragment.OnDespachadorDialogListener,
        DespachadorAdapter.OnDespachadorInteractionListener {

    private static final String TAG = "DespachadorActivity";
    private boolean dataChanged = false;

    private RecyclerView recyclerView;
    private DespachadorAdapter adapter;
    private DespachadorDao despachadorDao;
    private List<Despachador> despachadorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despachador);

        // Configuración del Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_despachadores);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Inicializar DAO
        despachadorDao = new DespachadorDao(this);

        // Configuración de RecyclerView
        recyclerView = findViewById(R.id.recycler_view_despachadores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DespachadorAdapter(despachadorList, this);
        recyclerView.setAdapter(adapter);

        // Cargar los datos iniciales
        loadDespachadores();

        // Configuración del Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab_add_despachador);
        fab.setOnClickListener(view -> showAddDespachadorDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Llama a finish() para que se active el setResult
        return true;
    }

    private void loadDespachadores() {
        // La línea corregida para que el método coincida con el DAO
        despachadorList = despachadorDao.getAllDespachador();
        adapter.updateList(despachadorList);
        Log.d(TAG, "Despachadores cargados: " + despachadorList.size());
    }

    private void showAddDespachadorDialog() {
        DespachadorDialogFragment dialog = DespachadorDialogFragment.newInstance(null);
        dialog.show(getSupportFragmentManager(), "DespachadorDialogFragment");
    }

    @Override
    public void onSaveDespachador(Despachador despachador) {
        long result = despachadorDao.insertDespachador(despachador);
        if (result != -1) {
            Toast.makeText(this, "Despachador agregado", Toast.LENGTH_SHORT).show();
            loadDespachadores();
            dataChanged = true;
        } else {
            Toast.makeText(this, "Error al agregar. El código ya existe.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpdateDespachador(Despachador despachador) {
        int rowsAffected = despachadorDao.updateDespachador(despachador);
        if (rowsAffected > 0) {
            Toast.makeText(this, "Despachador actualizado", Toast.LENGTH_SHORT).show();
            loadDespachadores();
            dataChanged = true;
        } else {
            Toast.makeText(this, "Error al actualizar. Despachador no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditDespachador(Despachador despachador) {
        DespachadorDialogFragment dialog = DespachadorDialogFragment.newInstance(despachador);
        dialog.show(getSupportFragmentManager(), "DespachadorDialogFragment");
    }

    @Override
    public void onDeleteDespachador(Despachador despachador) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Despachador")
                .setMessage("¿Estás seguro de que quieres eliminar a " + despachador.getNombreDespachador() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int rowsAffected = despachadorDao.deleteDespachador(despachador.getCodigoDespachador());
                    if (rowsAffected > 0) {
                        Toast.makeText(this, "Despachador eliminado", Toast.LENGTH_SHORT).show();
                        loadDespachadores();
                        dataChanged = true;
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void finish() {
        if (dataChanged) {
            // Si hubo cambios, envía un resultado OK
            setResult(RESULT_OK);
        } else {
            // Si no hubo cambios, envía un resultado de cancelación
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }
}
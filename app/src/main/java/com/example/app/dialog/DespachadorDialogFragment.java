package com.example.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.app.R;
import com.example.app.model.Despachador;
import com.google.android.material.textfield.TextInputEditText;

public class DespachadorDialogFragment extends DialogFragment {

    public interface OnDespachadorDialogListener {
        void onSaveDespachador(Despachador despachador);
        void onUpdateDespachador(Despachador despachador);
    }

    private OnDespachadorDialogListener listener;
    private Despachador despachadorToEdit;
    private TextInputEditText editCodigo, editNombre, editEmpresa;

    public static DespachadorDialogFragment newInstance(Despachador despachador) {
        DespachadorDialogFragment fragment = new DespachadorDialogFragment();
        if (despachador != null) {
            Bundle args = new Bundle();
            args.putSerializable("despachador", despachador);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // El listener debe ser la actividad o fragmento padre
            if (context instanceof OnDespachadorDialogListener) {
                listener = (OnDespachadorDialogListener) context;
            } else if (getParentFragment() instanceof OnDespachadorDialogListener) {
                listener = (OnDespachadorDialogListener) getParentFragment();
            } else {
                throw new ClassCastException(context.toString() + " must implement OnDespachadorDialogListener");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDespachadorDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_despachador, null);

        editCodigo = view.findViewById(R.id.edit_codigo_despachador);
        editNombre = view.findViewById(R.id.edit_nombre_despachador);
        // Despachador no tiene campo 'empresa', lo eliminamos
        editEmpresa = view.findViewById(R.id.edit_empresa);

        if (getArguments() != null && getArguments().containsKey("despachador")) {
            despachadorToEdit = (Despachador) getArguments().getSerializable("despachador");
            editCodigo.setText(despachadorToEdit.getCodigoDespachador());
            editNombre.setText(despachadorToEdit.getNombreDespachador());
            editEmpresa.setText(despachadorToEdit.getCodigoEmpresa());
            editCodigo.setEnabled(false); // Deshabilitamos el campo de código para edición
            builder.setTitle("Editar Despachador");
        } else {
            builder.setTitle("Agregar Despachador");
        }

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, id) -> {})
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        final AlertDialog dialog = builder.create();

        // Sobrescribimos el botón positivo para manejar la validación
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String codigo = editCodigo.getText().toString().trim();
                String nombre = editNombre.getText().toString().trim();
                String empresa = "sal";

                if (TextUtils.isEmpty(codigo) || TextUtils.isEmpty(nombre)) {
                    Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Despachador solo tiene código y nombre
                Despachador newDespachador = new Despachador(codigo,empresa, nombre);

                if (despachadorToEdit == null) {
                    listener.onSaveDespachador(newDespachador); // Usamos el método correcto
                } else {
                    // Para actualizar, aseguramos que el código de la instancia original se mantenga si fuera necesario.
                    // Aunque la lógica del DAO maneja esto, es una buena práctica.
                    newDespachador.setCodigoDespachador(despachadorToEdit.getCodigoDespachador());
                    listener.onUpdateDespachador(newDespachador);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }
}
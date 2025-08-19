package com.example.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.app.R;
import com.example.app.model.Transportista;
import com.google.android.material.textfield.TextInputEditText;

public class TransportistaDialogFragment extends DialogFragment {

    public interface OnTransportistaDialogListener {
        void onSaveTransportista(Transportista transportista);
        void onUpdateTransportista(Transportista transportista);
    }

    private OnTransportistaDialogListener listener;
    private Transportista transportistaToEdit;
    private TextInputEditText editCodigo, editNombre, editEmpresa;

    public static TransportistaDialogFragment newInstance(Transportista transportista) {
        TransportistaDialogFragment fragment = new TransportistaDialogFragment();
        if (transportista != null) {
            Bundle args = new Bundle();
            args.putSerializable("transportista", transportista);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnTransportistaDialogListener) getParentFragment();
            if (listener == null) {
                listener = (OnTransportistaDialogListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTransportistaDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_transportista, null);

        editCodigo = view.findViewById(R.id.edit_codigo);
        editNombre = view.findViewById(R.id.edit_nombre);
        editEmpresa = view.findViewById(R.id.edit_empresa);

        if (getArguments() != null && getArguments().containsKey("transportista")) {
            transportistaToEdit = (Transportista) getArguments().getSerializable("transportista");
            editCodigo.setText(transportistaToEdit.getCodigoTransportista());
            editNombre.setText(transportistaToEdit.getNombreTransportista());
            editEmpresa.setText(transportistaToEdit.getCodigoEmpresa());
            editCodigo.setEnabled(false); // Deshabilitamos el campo de código
            builder.setTitle("Editar Transportista");
        } else {
            builder.setTitle("Agregar Transportista");
        }

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, id) -> {}) // Dejamos el listener vacío
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        final AlertDialog dialog = builder.create();

        // Sobrescribimos el botón positivo para manejar la validación
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String codigo = editCodigo.getText().toString().trim();
                String nombre = editNombre.getText().toString().trim();
                String empresa = editEmpresa.getText().toString().trim();

                if (TextUtils.isEmpty(codigo) || TextUtils.isEmpty(nombre) || TextUtils.isEmpty(empresa)) {
                    Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                Transportista newTransportista = new Transportista(codigo, empresa, nombre);

                if (transportistaToEdit == null) {
                    listener.onSaveTransportista(newTransportista);
                } else {
                    listener.onUpdateTransportista(newTransportista);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }
}
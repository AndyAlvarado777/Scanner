package com.example.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.model.Despachador;
import java.util.List;

public class DespachadorAdapter extends RecyclerView.Adapter<DespachadorAdapter.ViewHolder> {

    private List<Despachador> despachadores;
    private OnDespachadorInteractionListener listener;

    // Interfaz para manejar las interacciones de clic y clic largo
    public interface OnDespachadorInteractionListener {
        void onEditDespachador(Despachador despachador);
        void onDeleteDespachador(Despachador despachador);
    }

    public DespachadorAdapter(List<Despachador> despachadores, OnDespachadorInteractionListener listener) {
        this.despachadores = despachadores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que este layout sea específico para despachadores, por ejemplo, `item_despachador.xml`
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_despachador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Despachador despachador = despachadores.get(position);
        holder.tvNombre.setText(despachador.getNombreDespachador());
        holder.tvCodigo.setText("Código: " + despachador.getCodigoDespachador());

        // Manejar clics para editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditDespachador(despachador);
            }
        });

        // Manejar clics largos para borrar
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteDespachador(despachador);
            }
            return true; // Devolver true para indicar que el evento ha sido consumido
        });
    }

    @Override
    public int getItemCount() {
        return despachadores.size();
    }

    public void updateList(List<Despachador> newList) {
        this.despachadores = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCodigo;

        public ViewHolder(View itemView) {
            super(itemView);
            // Asegúrate de que los IDs de las vistas correspondan a `item_despachador.xml`
            tvNombre = itemView.findViewById(R.id.tv_nombre_despachador);
            tvCodigo = itemView.findViewById(R.id.tv_codigo_despachador);
        }
    }
}
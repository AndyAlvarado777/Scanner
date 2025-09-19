package com.example.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.model.Transportista;
import java.util.List;

public class TransportistaAdapter extends RecyclerView.Adapter<TransportistaAdapter.ViewHolder> {

    private List<Transportista> transportistas;
    private OnTransportistaInteractionListener listener;

    public interface OnTransportistaInteractionListener {
        void onEditTransportista(Transportista transportista);
        void onDeleteTransportista(Transportista transportista);
    }

    public TransportistaAdapter(List<Transportista> transportistas, OnTransportistaInteractionListener listener) {
        this.transportistas = transportistas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transportista, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transportista transportista = transportistas.get(position);
        holder.tvNombre.setText(transportista.getNombreTransportista());
        holder.tvCodigo.setText("Código: " + transportista.getCodigoTransportista());

        // Manejar clics para editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTransportista(transportista);
            }
        });

        // Manejar clics largos para borrar
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTransportista(transportista);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transportistas.size();
    }

    public void updateList(List<Transportista> newList) {
        this.transportistas = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCodigo;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_transportista);
            tvCodigo = itemView.findViewById(R.id.tv_codigo_transportista);
        }
    }
}
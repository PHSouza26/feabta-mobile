package com.example.feabtainformatica;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.ViewHolder> {

    private List<MainActivity.Agendamento> lista;
    private Context context;

    public AgendamentoAdapter(List<MainActivity.Agendamento> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agendamento, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.Agendamento ag = lista.get(position);
        holder.txtInfo.setText(ag.data + " " + ag.hora + "\n" + ag.tipoServico + ": " + ag.descricao);

        // Clique curto: editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAgendamentoActivity.class);
            intent.putExtra("data", ag.data);
            intent.putExtra("hora", ag.hora);
            intent.putExtra("tipo", ag.tipoServico);
            intent.putExtra("descricao", ag.descricao);
            context.startActivity(intent);
        });

        // Clique longo: excluir
        holder.itemView.setOnLongClickListener(v -> {
            String chave = ag.data + "_" + ag.hora;
            FirebaseDatabase.getInstance().getReference("agendamentos").child(chave).removeValue()
                    .addOnSuccessListener(unused -> Toast.makeText(context, "Excluído.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.txtInfo);
        }
    }
}
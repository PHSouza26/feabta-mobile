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

        // Monta o texto a ser exibido
        String texto = "Nome: " + ag.nome +
                "\nData: " + ag.data +
                "\nHora: " + ag.hora +
                "\nServiço: " + ag.tipoServico +
                "\nEndereço: " + ag.endereco;

        holder.txtInfo.setText(texto);

        // Clique curto: editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAgendamentoActivity.class);
            intent.putExtra("nome", ag.nome);
            intent.putExtra("endereco", ag.endereco);
            intent.putExtra("data", ag.data);
            intent.putExtra("hora", ag.hora);
            intent.putExtra("tipo", ag.tipoServico);
            intent.putExtra("descricao", ag.descricao); // Se quiser manter, senão pode remover
            context.startActivity(intent);
        });

        // Clique longo: excluir com confirmação
        holder.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Excluir Agendamento")
                    .setMessage("Deseja realmente excluir este agendamento?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        String chave = ag.data + "_" + ag.hora;
                        FirebaseDatabase.getInstance().getReference("agendamentos").child(chave).removeValue()
                                .addOnSuccessListener(unused -> Toast.makeText(context, "Excluído.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
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
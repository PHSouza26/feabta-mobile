package com.example.feabtainformatica;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AgendamentoAdapter adapter;
    private List<Agendamento> listaAgendamentos = new ArrayList<>();
    private DatabaseReference databaseRef;
    private EditText filtroData;
    private Button btnFiltrar, btnNovo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerAgendamentos);
        filtroData = findViewById(R.id.filtroData);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnNovo = findViewById(R.id.btnNovo);

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendamentoAdapter(listaAgendamentos, this);
        recyclerView.setAdapter(adapter);

        carregarAgendamentos();

        btnFiltrar.setOnClickListener(v -> filtrarPorData());
        btnNovo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, addagenda.class)));
    }

    private void carregarAgendamentos() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAgendamentos.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Agendamento a = snap.getValue(Agendamento.class);
                    listaAgendamentos.add(a);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erro ao carregar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarPorData() {
        String data = filtroData.getText().toString().trim();
        if (TextUtils.isEmpty(data)) {
            carregarAgendamentos();
            return;
        }

        databaseRef.orderByChild("data").equalTo(data).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAgendamentos.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Agendamento a = snap.getValue(Agendamento.class);
                    listaAgendamentos.add(a);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erro ao filtrar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Agendamento {
        public String data, hora, tipoServico, descricao;

        public Agendamento() { }

        public Agendamento(String data, String hora, String tipoServico, String descricao) {
            this.data = data;
            this.hora = hora;
            this.tipoServico = tipoServico;
            this.descricao = descricao;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarAgendamentos();
    }
}
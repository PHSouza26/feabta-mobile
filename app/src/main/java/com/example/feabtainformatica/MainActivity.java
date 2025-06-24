package com.example.feabtainformatica;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

    private Button btnFiltrar, btnNovo, btnSelecionarData;
    private TextView txtDataSelecionada;

    private String dataSelecionada = "";

    private TextView txtMensagem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerAgendamentos);
        txtDataSelecionada = findViewById(R.id.filtroData);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnNovo = findViewById(R.id.btnNovo);
        txtMensagem = findViewById(R.id.txtMensagem);

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendamentoAdapter(listaAgendamentos, this);
        recyclerView.setAdapter(adapter);

        carregarAgendamentos();

        txtDataSelecionada.setOnClickListener(v -> abrirDatePicker());
        btnFiltrar.setOnClickListener(v -> filtrarPorData());
        btnNovo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, addagenda.class)));
    }

    private void abrirDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            month++; // O mês no DatePicker começa em 0
            dataSelecionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, dayOfMonth);
            txtDataSelecionada.setText(dataSelecionada);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
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

        if (listaAgendamentos.isEmpty()) {
            txtMensagem.setVisibility(View.VISIBLE);
        } else {
            txtMensagem.setVisibility(View.GONE);
        }
    }

    private void filtrarPorData() {
        if (TextUtils.isEmpty(dataSelecionada)) {
            carregarAgendamentos();
            return;
        }

        databaseRef.orderByChild("data").equalTo(dataSelecionada).addListenerForSingleValueEvent(new ValueEventListener() {
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

        if (listaAgendamentos.isEmpty()) {
            txtMensagem.setVisibility(View.VISIBLE);
        } else {
            txtMensagem.setVisibility(View.GONE);
        }
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
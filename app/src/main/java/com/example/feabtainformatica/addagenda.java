package com.example.feabtainformatica;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class addagenda extends AppCompatActivity {

    private EditText edtTipo, edtDescricao;
    private TextView txtData, txtHora;
    private Button btnAgendar;

    private DatabaseReference databaseRef;

    private String dataSelecionada = "";
    private String horaSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addagenda);

        edtTipo = findViewById(R.id.edtTipo);
        edtDescricao = findViewById(R.id.edtDescricao);
        txtData = findViewById(R.id.txtData);
        txtHora = findViewById(R.id.txtHora);
        btnAgendar = findViewById(R.id.btnAgendar);

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");

        txtData.setOnClickListener(view -> escolherData());
        txtHora.setOnClickListener(view -> escolherHora());

        btnAgendar.setOnClickListener(view -> verificarEAgendar());
    }

    private void escolherData() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            month += 1;
            dataSelecionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, dayOfMonth);
            txtData.setText(dataSelecionada);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void escolherHora() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            horaSelecionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            txtHora.setText(horaSelecionada);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void verificarEAgendar() {
        String tipo = edtTipo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (TextUtils.isEmpty(tipo) || TextUtils.isEmpty(descricao)
                || TextUtils.isEmpty(dataSelecionada) || TextUtils.isEmpty(horaSelecionada)) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String chave = dataSelecionada + "_" + horaSelecionada;

        databaseRef.child(chave).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(addagenda.this, "Conflito: horário já agendado!", Toast.LENGTH_SHORT).show();
                } else {
                    Agendamento agendamento = new Agendamento(dataSelecionada, horaSelecionada, tipo, descricao);
                    databaseRef.child(chave).setValue(agendamento)
                            .addOnSuccessListener(unused -> Toast.makeText(addagenda.this, "Agendado com sucesso!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(addagenda.this, "Erro ao agendar.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(addagenda.this, "Erro ao verificar conflito.", Toast.LENGTH_SHORT).show();
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
}

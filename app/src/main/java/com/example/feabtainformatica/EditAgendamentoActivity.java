package com.example.feabtainformatica;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class EditAgendamentoActivity extends AppCompatActivity {

    private EditText edtTipo, edtDescricao;
    private TextView txtData, txtHora;
    private Button btnSalvar;

    private DatabaseReference databaseRef;
    private String chaveOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addagenda);

        edtTipo = findViewById(R.id.edtTipo);
        edtDescricao = findViewById(R.id.edtDescricao);
        txtData = findViewById(R.id.txtData);
        txtHora = findViewById(R.id.txtHora);
        btnSalvar = findViewById(R.id.btnAgendar);
        btnSalvar.setText("Salvar");

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");

        // Recupera dados enviados por intent
        String data = getIntent().getStringExtra("data");
        String hora = getIntent().getStringExtra("hora");
        String tipo = getIntent().getStringExtra("tipo");
        String desc = getIntent().getStringExtra("descricao");
        chaveOriginal = data + "_" + hora;

        txtData.setText(data);
        txtHora.setText(hora);
        edtTipo.setText(tipo);
        edtDescricao.setText(desc);

        txtData.setOnClickListener(view -> escolherData());
        txtHora.setOnClickListener(view -> escolherHora());
        btnSalvar.setOnClickListener(view -> salvarAlteracoes());
    }

    private void escolherData() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            month++;
            String data = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, dayOfMonth);
            txtData.setText(data);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void escolherHora() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            txtHora.setText(hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void salvarAlteracoes() {
        String tipo = edtTipo.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String data = txtData.getText().toString().trim();
        String hora = txtHora.getText().toString().trim();

        if (TextUtils.isEmpty(tipo) || TextUtils.isEmpty(descricao)
                || TextUtils.isEmpty(data) || TextUtils.isEmpty(hora)) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String novaChave = data + "_" + hora;
        MainActivity.Agendamento novo = new MainActivity.Agendamento(data, hora, tipo, descricao);

        databaseRef.child(chaveOriginal).removeValue();
        databaseRef.child(novaChave).setValue(novo)
                .addOnSuccessListener(unused -> finish())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show());
    }
}


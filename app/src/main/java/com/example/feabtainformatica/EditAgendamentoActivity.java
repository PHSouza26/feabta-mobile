package com.example.feabtainformatica;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class EditAgendamentoActivity extends AppCompatActivity {

    private EditText edtNome, edtEndereco, edtDescricao;
    private Spinner spinnerTipo;
    private TextView txtData, txtHora;
    private Button btnSalvar, btnExcluir;
    private String chaveOriginal;

    private DatabaseReference databaseRef;
    private String[] tipos = {"Checagem", "Manutenção de Hardware", "Troca de peça", "Manutenção de Software"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_agenda);

        edtNome = findViewById(R.id.edtNome);
        edtEndereco = findViewById(R.id.edtEndereco);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        edtDescricao = findViewById(R.id.edtDescricao);
        txtData = findViewById(R.id.txtData);
        txtHora = findViewById(R.id.txtHora);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnExcluir = findViewById(R.id.btnExcluir);

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterSpinner);

        // Recupera os dados enviados pela Intent
        String nome = getIntent().getStringExtra("nome");
        String endereco = getIntent().getStringExtra("endereco");
        String data = getIntent().getStringExtra("data");
        String hora = getIntent().getStringExtra("hora");
        String tipo = getIntent().getStringExtra("tipo");
        String desc = getIntent().getStringExtra("descricao");

        if (nome == null || endereco == null || data == null || hora == null || tipo == null || desc == null) {
            Toast.makeText(this, "Dados inválidos.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chaveOriginal = data + "_" + hora;

        // Preencher os campos
        edtNome.setText(nome);
        edtEndereco.setText(endereco);
        txtData.setText(data);
        txtHora.setText(hora);
        edtDescricao.setText(desc);

        int indexTipo = Arrays.asList(tipos).indexOf(tipo);
        if (indexTipo >= 0) {
            spinnerTipo.setSelection(indexTipo);
        }

        txtData.setOnClickListener(view -> escolherData());
        txtHora.setOnClickListener(view -> escolherHora());
        btnSalvar.setOnClickListener(view -> salvarAlteracoes());
        btnExcluir.setOnClickListener(view -> excluirAgendamento());
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
        String nome = edtNome.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String descricao = edtDescricao.getText().toString().trim();
        String data = txtData.getText().toString().trim();
        String hora = txtHora.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(endereco) || TextUtils.isEmpty(descricao)
                || TextUtils.isEmpty(data) || TextUtils.isEmpty(hora)) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar agora = Calendar.getInstance();
        Calendar dataHoraSelecionada = Calendar.getInstance();

        try {
            String[] dataSplit = data.split("-");
            String[] horaSplit = hora.split(":");

            dataHoraSelecionada.set(Calendar.YEAR, Integer.parseInt(dataSplit[0]));
            dataHoraSelecionada.set(Calendar.MONTH, Integer.parseInt(dataSplit[1]) - 1);
            dataHoraSelecionada.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataSplit[2]));
            dataHoraSelecionada.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaSplit[0]));
            dataHoraSelecionada.set(Calendar.MINUTE, Integer.parseInt(horaSplit[1]));
        } catch (Exception e) {
            Toast.makeText(this, "Data ou hora inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataHoraSelecionada.before(agora)) {
            Toast.makeText(this, "Data e horário devem ser futuros.", Toast.LENGTH_SHORT).show();
            return;
        }

        String novaChave = data + "_" + hora;

        MainActivity.Agendamento novo = new MainActivity.Agendamento(nome, endereco, data, hora, tipo, descricao);

        databaseRef.child(chaveOriginal).removeValue();
        databaseRef.child(novaChave).setValue(novo)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Agendamento atualizado.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show());
    }

    private void excluirAgendamento() {
        databaseRef.child(chaveOriginal).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Agendamento excluído.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
    }
}

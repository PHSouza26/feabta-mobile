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

public class addagenda extends AppCompatActivity {

    private Spinner spinnerTipo;
    private EditText edtDescricao, edtNome, edtEndereco;;
    private TextView txtData, txtHora;
    private Button btnAgendar;
    private DatabaseReference databaseRef;
    private String dataSelecionada = "";
    private String horaSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addagenda);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtNome = findViewById(R.id.edtNome);
        edtEndereco = findViewById(R.id.edtEndereco);
        txtData = findViewById(R.id.txtData);
        txtHora = findViewById(R.id.txtHora);
        btnAgendar = findViewById(R.id.btnAgendar);

        databaseRef = FirebaseDatabase.getInstance().getReference("agendamentos");
        String[] tipos = {"Checagem", "Manutenção de Hardware", "Troca de peça", "Manutenção de Software"};
        
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterSpinner);

        txtData.setOnClickListener(view -> escolherData());
        txtHora.setOnClickListener(view -> escolherHora());

        btnAgendar.setOnClickListener(view -> verificarAgendamento());

        Button btnLimparCampos = findViewById(R.id.btnLimparCampos);

        btnLimparCampos.setOnClickListener(v -> {
            edtNome.setText("");
            edtEndereco.setText("");
            spinnerTipo.setSelection(0);
            edtDescricao.setText("");
            txtData.setText("Selecionar Data");
            txtHora.setText("Selecionar Hora");
            dataSelecionada = "";
            horaSelecionada = "";
        });
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

    private void verificarAgendamento() {
        String tipo = spinnerTipo.getSelectedItem().toString();
        String descricao = edtDescricao.getText().toString().trim();
        String nome = edtNome.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(endereco) ||
                TextUtils.isEmpty(descricao) || TextUtils.isEmpty(dataSelecionada) || TextUtils.isEmpty(horaSelecionada)) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar agora = Calendar.getInstance();

        Calendar dataHoraSelecionada = Calendar.getInstance();
        String[] dataSplit = dataSelecionada.split("-");
        String[] horaSplit = horaSelecionada.split(":");

        dataHoraSelecionada.set(Calendar.YEAR, Integer.parseInt(dataSplit[0]));
        dataHoraSelecionada.set(Calendar.MONTH, Integer.parseInt(dataSplit[1]) - 1); // mês começa em 0
        dataHoraSelecionada.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataSplit[2]));
        dataHoraSelecionada.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaSplit[0]));
        dataHoraSelecionada.set(Calendar.MINUTE, Integer.parseInt(horaSplit[1]));

        if (dataHoraSelecionada.before(agora)) {
            Toast.makeText(this, "Data e horário devem ser futuros.", Toast.LENGTH_SHORT).show();
            return;
        }



        String chave = dataSelecionada + "_" + horaSelecionada;

        databaseRef.child(chave).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(addagenda.this, "Conflito: horário já agendado!", Toast.LENGTH_SHORT).show();
                } else {
                    Agendamento agendamento = new Agendamento(nome, endereco, dataSelecionada, horaSelecionada, tipo, descricao);
                    databaseRef.child(chave).setValue(agendamento)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(addagenda.this, "Agendado com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
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
        public String nome, endereco, data, hora, tipoServico, descricao;

        public Agendamento() { }

        public Agendamento(String nome, String endereco, String data, String hora, String tipoServico, String descricao) {
            this.nome = nome;
            this.endereco = endereco;
            this.data = data;
            this.hora = hora;
            this.tipoServico = tipoServico;
            this.descricao = descricao;
        }
    }


}
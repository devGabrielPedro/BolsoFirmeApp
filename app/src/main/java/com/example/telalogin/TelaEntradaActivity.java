package com.example.telalogin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TelaEntradaActivity extends AppCompatActivity {

    private TextInputEditText etValorEntrada, etDescricaoEntrada;
    private Button btnSalvarEntrada, btnSelecionarDataEntrada;
    private TextView tvDataSelecionadaEntrada;
    private Calendar dataSelecionadaCalendar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocumentRef;

    private static final String TAG = "TelaEntradaActivity";
    private SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_entrada);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Usuário nulo, finalizando activity.");
            finish();
            return;
        }
        userDocumentRef = db.collection("usuarios").document(currentUser.getUid());

        etValorEntrada = findViewById(R.id.etValorEntrada);
        etDescricaoEntrada = findViewById(R.id.etDescricaoEntrada);
        btnSalvarEntrada = findViewById(R.id.btnSalvarEntrada);
        btnSelecionarDataEntrada = findViewById(R.id.btnSelecionarDataEntrada);
        tvDataSelecionadaEntrada = findViewById(R.id.tvDataSelecionadaEntrada);

        dataSelecionadaCalendar = Calendar.getInstance();
        atualizarLabelDataSelecionada(); // Mostra a data atual no TextView

        btnSelecionarDataEntrada.setOnClickListener(v -> mostrarDatePickerDialog());
        btnSalvarEntrada.setOnClickListener(v -> registrarNovaEntrada());
    }

    private void atualizarLabelDataSelecionada() {
        tvDataSelecionadaEntrada.setText(sdfDisplay.format(dataSelecionadaCalendar.getTime()));
    }

    private void mostrarDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dataSelecionadaCalendar.set(Calendar.YEAR, year);
                    dataSelecionadaCalendar.set(Calendar.MONTH, month);
                    dataSelecionadaCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    atualizarLabelDataSelecionada();
                },
                dataSelecionadaCalendar.get(Calendar.YEAR),
                dataSelecionadaCalendar.get(Calendar.MONTH),
                dataSelecionadaCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void registrarNovaEntrada() {
        String valorStr = etValorEntrada.getText().toString().trim();
        String descricao = etDescricaoEntrada.getText().toString().trim();

        if (TextUtils.isEmpty(valorStr)) {
            etValorEntrada.setError("Valor é obrigatório");
            etValorEntrada.requestFocus();
            return;
        }
        double valor;
        try {
            valor = Double.parseDouble(valorStr);
            if (valor <= 0) {
                etValorEntrada.setError("Valor deve ser positivo");
                etValorEntrada.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etValorEntrada.setError("Valor inválido");
            etValorEntrada.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(descricao)) {
            etDescricaoEntrada.setError("Descrição é obrigatória");
            etDescricaoEntrada.requestFocus();
            return;
        }

        Map<String, Object> novaTransacao = new HashMap<>();
        novaTransacao.put("tipo", "entrada");
        novaTransacao.put("valor", valor);
        novaTransacao.put("descricao", descricao);
        // Pega o dia/mês/ano do DatePicker
        Calendar calendarioParaTimestamp = (Calendar) dataSelecionadaCalendar.clone();

        // Pega a hora/minuto/segundo atuais
        Calendar calendarioHoraAtual = Calendar.getInstance();
        calendarioParaTimestamp.set(Calendar.HOUR_OF_DAY, calendarioHoraAtual.get(Calendar.HOUR_OF_DAY));
        calendarioParaTimestamp.set(Calendar.MINUTE, calendarioHoraAtual.get(Calendar.MINUTE));
        calendarioParaTimestamp.set(Calendar.SECOND, calendarioHoraAtual.get(Calendar.SECOND));
        calendarioParaTimestamp.set(Calendar.MILLISECOND, calendarioHoraAtual.get(Calendar.MILLISECOND));

        novaTransacao.put("data", new Timestamp(calendarioParaTimestamp.getTime()));

        WriteBatch batch = db.batch();
        DocumentReference transacaoRef = userDocumentRef.collection("transacoes").document();
        batch.set(transacaoRef, novaTransacao);
        batch.update(userDocumentRef, "saldo", FieldValue.increment(valor));

        btnSalvarEntrada.setEnabled(false);
        Toast.makeText(this, "Salvando entrada...", Toast.LENGTH_SHORT).show();

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaEntradaActivity.this, "Entrada salva!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaEntradaActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSalvarEntrada.setEnabled(true);
                });
    }
}
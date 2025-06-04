package com.example.telalogin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID; // Para IDs de grupo

public class TelaSaidaActivity extends AppCompatActivity {

    private TextInputEditText etValorSaida, etDescricaoSaida, etNumeroParcelas, etMesesRecorrencia;
    private Button btnSalvarSaida, btnSelecionarDataSaida;
    private TextView tvDataSelecionadaSaida;
    private Spinner spinnerFormaPagamento;
    private CheckBox cbDespesaRecorrente;
    private LinearLayout layoutParcelamento, layoutConfigRecorrencia;
    private Calendar dataSelecionadaCalendar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocumentRef;

    private static final String TAG = "TelaSaidaActivity";
    private SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final String FORMA_PAGAMENTO_CREDITO = "Cartão de Crédito";
    private Spinner spinnerCategoriaSaida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_saida);

        // ... (Inicialização do Firebase Auth e Firestore como antes) ...
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // ... (Tratamento de usuário nulo como antes) ...
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        userDocumentRef = db.collection("usuarios").document(currentUser.getUid());

        // Referenciar Views
        etValorSaida = findViewById(R.id.etValorSaida);
        etDescricaoSaida = findViewById(R.id.etDescricaoSaida);
        btnSalvarSaida = findViewById(R.id.btnSalvarSaida);
        btnSelecionarDataSaida = findViewById(R.id.btnSelecionarDataSaida);
        tvDataSelecionadaSaida = findViewById(R.id.tvDataSelecionadaSaida);
        spinnerFormaPagamento = findViewById(R.id.spinnerFormaPagamento);
        layoutParcelamento = findViewById(R.id.layoutParcelamento);
        etNumeroParcelas = findViewById(R.id.etNumeroParcelas);
        cbDespesaRecorrente = findViewById(R.id.cbDespesaRecorrente);
        layoutConfigRecorrencia = findViewById(R.id.layoutConfigRecorrencia);
        etMesesRecorrencia = findViewById(R.id.etMesesRecorrencia);
        spinnerCategoriaSaida = findViewById(R.id.spinnerCategoriaSaida);
        ArrayAdapter<CharSequence> categoriaAdapter = ArrayAdapter.createFromResource(this,
                R.array.categorias_despesa_array, android.R.layout.simple_spinner_item);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaSaida.setAdapter(categoriaAdapter);

        dataSelecionadaCalendar = Calendar.getInstance();
        atualizarLabelDataSelecionada();

        // Configurar Spinner Forma de Pagamento
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.formas_pagamento_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormaPagamento.setAdapter(adapter);
        spinnerFormaPagamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String formaSelecionada = parent.getItemAtPosition(position).toString();
                if (FORMA_PAGAMENTO_CREDITO.equals(formaSelecionada)) {
                    layoutParcelamento.setVisibility(View.VISIBLE);
                    cbDespesaRecorrente.setChecked(false); // Não pode ser recorrente e parcelado ao mesmo tempo (simplificação)
                    layoutConfigRecorrencia.setVisibility(View.GONE);
                } else {
                    layoutParcelamento.setVisibility(View.GONE);
                    etNumeroParcelas.setText(""); // Limpa o campo
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutParcelamento.setVisibility(View.GONE);
            }
        });

        // Listener para CheckBox de Despesa Recorrente
        cbDespesaRecorrente.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutConfigRecorrencia.setVisibility(View.VISIBLE);
                // Se for recorrente, desabilitar parcelamento (simplificação)
                if (FORMA_PAGAMENTO_CREDITO.equals(spinnerFormaPagamento.getSelectedItem().toString())) {
                    spinnerFormaPagamento.setSelection(0); // Volta para a primeira opção
                    layoutParcelamento.setVisibility(View.GONE);
                }
            } else {
                layoutConfigRecorrencia.setVisibility(View.GONE);
                etMesesRecorrencia.setText(""); // Limpa o campo
            }
        });

        btnSelecionarDataSaida.setOnClickListener(v -> mostrarDatePickerDialog());
        btnSalvarSaida.setOnClickListener(v -> validarESalvarSaida());
    }

    private void atualizarLabelDataSelecionada() {
        tvDataSelecionadaSaida.setText(sdfDisplay.format(dataSelecionadaCalendar.getTime()));
    }

    private void mostrarDatePickerDialog() {
        // ... (Mesma lógica do DatePickerDialog da TelaEntradaActivity) ...
        DatePickerDialog datePickerDialog = new DatePickerDialog( this,
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

    private void validarESalvarSaida() {
        String valorTotalStr = etValorSaida.getText().toString().trim();
        String descricao = etDescricaoSaida.getText().toString().trim();
        String formaPagamento = spinnerFormaPagamento.getSelectedItem().toString();
        boolean isParcelado = FORMA_PAGAMENTO_CREDITO.equals(formaPagamento) && layoutParcelamento.getVisibility() == View.VISIBLE;
        boolean isRecorrente = cbDespesaRecorrente.isChecked();
        Calendar calendarioBaseParaTimestamp = (Calendar) dataSelecionadaCalendar.clone();
        String categoriaSelecionada = spinnerCategoriaSaida.getSelectedItem().toString();

        // Validações básicas
        if (TextUtils.isEmpty(valorTotalStr)) { /* ... error ... */ etValorSaida.setError("Valor obrigatório"); return; }
        double valorTotal;
        try {
            valorTotal = Double.parseDouble(valorTotalStr);
            if (valorTotal <= 0) { /* ... error ... */ etValorSaida.setError("Valor deve ser positivo"); return; }
        } catch (NumberFormatException e) { /* ... error ... */ etValorSaida.setError("Valor inválido"); return; }
        if (TextUtils.isEmpty(descricao)) { /* ... error ... */ etDescricaoSaida.setError("Descrição obrigatória"); return; }

        List<Map<String, Object>> transacoesParaSalvar = new ArrayList<>();
        String idGrupo = UUID.randomUUID().toString(); // ID único para o grupo de parcelas/recorrências

        // Lógica de Parcelamento
        if (isParcelado) {
            String numParcelasStr = etNumeroParcelas.getText().toString().trim();
            if (TextUtils.isEmpty(numParcelasStr)) { /* ... error ... */ etNumeroParcelas.setError("Nº de parcelas obrigatório"); return; }
            int numParcelas;
            try {
                numParcelas = Integer.parseInt(numParcelasStr);
                if (numParcelas <= 0) { /* ... error ... */ etNumeroParcelas.setError("Inválido"); return; }
            } catch (NumberFormatException e) { /* ... error ... */ etNumeroParcelas.setError("Inválido"); return; }

            if (numParcelas == 1) { // Se for 1 parcela, trata como transação normal com CC
                Map<String, Object> transacao = criarMapaTransacao(
                        valorTotal, descricao + " (1/1)", new Timestamp(dataSelecionadaCalendar.getTime()),
                        formaPagamento, categoriaSelecionada, true, 1, 1, idGrupo,
                        false, null);
                transacoesParaSalvar.add(transacao);
            } else {
                double valorParcela = Math.round((valorTotal / numParcelas) * 100.0) / 100.0; // Arredonda para 2 casas decimais
                Calendar dataParcelaCal = (Calendar) calendarioBaseParaTimestamp.clone();
                for (int i = 1; i <= numParcelas; i++) {
                    Calendar dataParcelaComHora = (Calendar) dataParcelaCal.clone();
                    if (i == 1) { // Apenas para a primeira parcela, usar a hora atual do registro
                        Calendar calendarioHoraAtual = Calendar.getInstance();
                        dataParcelaComHora.set(Calendar.HOUR_OF_DAY, calendarioHoraAtual.get(Calendar.HOUR_OF_DAY));
                        dataParcelaComHora.set(Calendar.MINUTE, calendarioHoraAtual.get(Calendar.MINUTE));
                        dataParcelaComHora.set(Calendar.SECOND, calendarioHoraAtual.get(Calendar.SECOND));
                        dataParcelaComHora.set(Calendar.MILLISECOND, calendarioHoraAtual.get(Calendar.MILLISECOND));
                    }
                    Map<String, Object> parcela = criarMapaTransacao(
                            valorParcela, descricao + " (" + i + "/" + numParcelas + ")",
                            new Timestamp(dataParcelaCal.getTime()), formaPagamento,
                            categoriaSelecionada, true, i, numParcelas, idGrupo, false, null
                    );
                    transacoesParaSalvar.add(parcela);
                    dataParcelaCal.add(Calendar.MONTH, 1); // Próxima parcela no mês seguinte
                }
            }
        }
        // Lógica de Recorrência
        else if (isRecorrente) {
            String mesesRecorrenciaStr = etMesesRecorrencia.getText().toString().trim();
            if (TextUtils.isEmpty(mesesRecorrenciaStr)) { etMesesRecorrencia.setError("Nº de meses obrigatório"); return; }
            int numMesesRecorrencia;
            try {
                numMesesRecorrencia = Integer.parseInt(mesesRecorrenciaStr);
                if (numMesesRecorrencia <= 0) { etMesesRecorrencia.setError("Inválido"); return; }
            } catch (NumberFormatException e) { etMesesRecorrencia.setError("Inválido"); return; }

            Calendar dataRecorrenciaCal = (Calendar) calendarioBaseParaTimestamp.clone();
            for (int i = 0; i < numMesesRecorrencia; i++) {
                Calendar dataRecorrenciaComHora = (Calendar) dataRecorrenciaCal.clone();
                if (i == 0) { // Apenas para a primeira recorrência, usar a hora atual do registro
                    Calendar calendarioHoraAtual = Calendar.getInstance();
                    dataRecorrenciaComHora.set(Calendar.HOUR_OF_DAY, calendarioHoraAtual.get(Calendar.HOUR_OF_DAY));
                    dataRecorrenciaComHora.set(Calendar.MINUTE, calendarioHoraAtual.get(Calendar.MINUTE));
                    dataRecorrenciaComHora.set(Calendar.SECOND, calendarioHoraAtual.get(Calendar.SECOND));
                    dataRecorrenciaComHora.set(Calendar.MILLISECOND, calendarioHoraAtual.get(Calendar.MILLISECOND));
                }
                Map<String, Object> recorrencia = criarMapaTransacao(
                        valorTotal, // Valor fixo
                        descricao + " (Recorrente)", new Timestamp(dataRecorrenciaComHora.getTime()),
                        formaPagamento, categoriaSelecionada, false, 0,0,null,
                        true, idGrupo // Usando o mesmo idGrupo para agrupar
                );
                transacoesParaSalvar.add(recorrencia);
                dataRecorrenciaCal.add(Calendar.MONTH, 1);
            }
        }
        // Transação Única (não parcelada, não recorrente)
        else {
            Calendar calendarioTimestampUnico = (Calendar) calendarioBaseParaTimestamp.clone();
            Calendar calendarioHoraAtual = Calendar.getInstance();
            calendarioTimestampUnico.set(Calendar.HOUR_OF_DAY, calendarioHoraAtual.get(Calendar.HOUR_OF_DAY));
            calendarioTimestampUnico.set(Calendar.MINUTE, calendarioHoraAtual.get(Calendar.MINUTE));
            calendarioTimestampUnico.set(Calendar.SECOND, calendarioHoraAtual.get(Calendar.SECOND));
            calendarioTimestampUnico.set(Calendar.MILLISECOND, calendarioHoraAtual.get(Calendar.MILLISECOND));

            Map<String, Object> transacao = criarMapaTransacao(
                    valorTotal, descricao, new Timestamp(calendarioTimestampUnico.getTime()),
                    formaPagamento, categoriaSelecionada, false,0,0,null,
                    false, null);
            transacoesParaSalvar.add(transacao);
        }

        // Salvar no Firestore
        salvarTransacoesBatch(transacoesParaSalvar, !(isParcelado || isRecorrente), valorTotal);
    }

    private Map<String, Object> criarMapaTransacao(double valor, String desc, Timestamp data, String formaPag, String categoria,
                                                   boolean parcelado, int parcelaAtual, int totalParcelas, String idGrupoParc,
                                                   boolean recorrente, String idGrupoRec) {
        Map<String, Object> transacao = new HashMap<>();
        transacao.put("tipo", "saida");
        transacao.put("valor", valor);
        transacao.put("descricao", desc);
        transacao.put("data", data);
        transacao.put("formaPagamento", formaPag);
        transacao.put("categoria", categoria);

        if (parcelado) {
            transacao.put("parcelado", true);
            transacao.put("parcelaAtual", parcelaAtual);
            transacao.put("totalParcelas", totalParcelas);
            transacao.put("idGrupoParcelamento", idGrupoParc);
        }
        if (recorrente) {
            transacao.put("recorrente", true);
            transacao.put("idGrupoRecorrencia", idGrupoRec);
        }
        return transacao;
    }


    private void salvarTransacoesBatch(List<Map<String, Object>> transacoes, boolean atualizarSaldoGlobal, double valorTotalParaSaldo) {
        WriteBatch batch = db.batch();

        for (Map<String, Object> transacao : transacoes) {
            DocumentReference transacaoRef = userDocumentRef.collection("transacoes").document();
            batch.set(transacaoRef, transacao);
        }

        // ATENÇÃO: O saldo global só é atualizado para despesas únicas e imediatas.
        // Parcelamentos e recorrências futuras NÃO afetam o saldo global AGORA.
        // Elas serão consideradas no cálculo do saldo do mês quando a data delas chegar.
        if (atualizarSaldoGlobal) {
            batch.update(userDocumentRef, "saldo", FieldValue.increment(-valorTotalParaSaldo));
        }

        btnSalvarSaida.setEnabled(false);
        Toast.makeText(this, "Salvando saída(s)...", Toast.LENGTH_SHORT).show();

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaSaidaActivity.this, "Saída(s) salva(s) com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaSaidaActivity.this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSalvarSaida.setEnabled(true);
                });
    }
}
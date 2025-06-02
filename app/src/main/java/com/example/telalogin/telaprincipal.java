package com.example.telalogin; // Ou seu pacote principal

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot; // Adicionado para addSnapshotListener

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class telaprincipal extends AppCompatActivity {

    // Componentes de UI
    private TextView tvSaldo, tvTituloExtrato;
    private Button btnSaida, btnGraficos, btnEntrada, btnLogout;
    private LinearLayout llMesesContainer; // Este é o LinearLayout dentro do HorizontalScrollView
    private RecyclerView rvExtrato;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocumentRef;

    // Extrato
    private TransacaoAdapter transacaoAdapter;
    private List<Transacao> listaDeTransacoesGlobais; // Mantém a lista para o adapter

    // Controle de Mês/Ano
    private int anoSelecionadoGlobal;
    private int mesSelecionadoGlobal; // 0 = Janeiro, 1 = Fevereiro, ...
    private Button ultimoBotaoMesSelecionado = null; // Para estilização

    private static final String TAG = "TelaPrincipal";
    // Nomes dos meses para os botões/textos
    private final String[] NOMES_MESES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    private ListenerRegistration saldoListenerRegistration;
    private ListenerRegistration extratoListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telaprincipal); // Seu XML principal

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Verificar se usuário está logado
        if (currentUser == null) {
            irParaTelaLogin();
            return; // Impede a execução do resto do onCreate
        }
        // Referência ao documento do usuário no Firestore
        userDocumentRef = db.collection("usuarios").document(currentUser.getUid());

        // Referenciar componentes de UI (usando os IDs do seu XML)
        tvSaldo = findViewById(R.id.tvSaldo);
        tvTituloExtrato = findViewById(R.id.tvTituloExtrato); // TextView para "Extrato de..."
        llMesesContainer = findViewById(R.id.llMesesContainer); // LinearLayout dentro do HorizontalScrollView
        rvExtrato = findViewById(R.id.rvExtrato); // Seu RecyclerView
        btnEntrada = findViewById(R.id.btnEntrada);
        btnSaida = findViewById(R.id.btnSaida);
        btnGraficos = findViewById(R.id.btnGraficos);
        btnLogout = findViewById(R.id.btnLogout); // Botão de Logout

        // Configurar RecyclerView para o extrato
        listaDeTransacoesGlobais = new ArrayList<>();
        transacaoAdapter = new TransacaoAdapter(this, listaDeTransacoesGlobais); // Passar o contexto
        rvExtrato.setLayoutManager(new LinearLayoutManager(this));
        rvExtrato.setAdapter(transacaoAdapter);

        btnEntrada.setOnClickListener(v -> {
            Intent intent = new Intent(telaprincipal.this, TelaEntradaActivity.class);
            startActivity(intent);
        });

        btnSaida.setOnClickListener(v -> {
            Intent intent = new Intent(telaprincipal.this, TelaSaidaActivity.class);
            startActivity(intent);
        });

        btnGraficos.setOnClickListener(v -> {
            // Quando você criar a TelaGraficosActivity, a chamada será similar:
            // Intent intent = new Intent(telaprincipal.this, TelaGraficosActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Tela de Gráficos a ser implementada.", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            irParaTelaLogin();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Registrando listeners...");
        carregarSaldoTotalDoUsuario(); // Este método agora vai registrar o saldoListenerRegistration

        // Garante que a barra de meses esteja configurada e o mês/extrato atual carregado com listener
        if (llMesesContainer.getChildCount() == 0) { // Configura a barra de meses apenas uma vez ou se estiver vazia
            configurarEselecionarMesAtualNaBarra();
        } else if (ultimoBotaoMesSelecionado != null) {
            // Se a barra já existe, apenas recarrega o extrato do mês selecionado para reativar o listener
            processarSelecaoDeMes(ultimoBotaoMesSelecionado, mesSelecionadoGlobal, anoSelecionadoGlobal);
        } else {
            // Fallback para caso algo não tenha sido inicializado
            configurarEselecionarMesAtualNaBarra();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remover os listeners para evitar memory leaks e processamento desnecessário
        Log.d(TAG, "onStop: Removendo listeners...");
        if (saldoListenerRegistration != null) {
            saldoListenerRegistration.remove();
            saldoListenerRegistration = null; // Limpa a referência
        }
        if (extratoListenerRegistration != null) {
            extratoListenerRegistration.remove();
            extratoListenerRegistration = null; // Limpa a referência
        }
    }

    private void irParaTelaLogin() {
        Intent intent = new Intent(telaprincipal.this, telalogin.class);
        // Limpa a pilha de activities para que o usuário não volte para cá com o botão "back"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void carregarSaldoTotalDoUsuario() {
        if (userDocumentRef == null || currentUser == null) {
            Log.e(TAG, "userDocumentRef ou currentUser é nulo em carregarSaldoTotalDoUsuario.");
            if (currentUser == null && !isFinishing()) irParaTelaLogin();
            return;
        }

        if (saldoListenerRegistration != null) {
            saldoListenerRegistration.remove();
        }

        saldoListenerRegistration  = userDocumentRef.addSnapshotListener(this, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Falha ao ouvir o saldo.", e);
                tvSaldo.setText("R$ --,--");
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Double saldoAtual = snapshot.getDouble("saldo");
                if (saldoAtual != null) {
                    tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldoAtual));
                    tvSaldo.setTextColor(saldoAtual < 0 ? ContextCompat.getColor(this, R.color.red) : ContextCompat.getColor(this, R.color.green));
                } else {
                    tvSaldo.setText("R$ 0,00"); // Caso o campo saldo seja nulo
                    tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.green));
                    userDocumentRef.update("saldo", 0.00)
                            .addOnSuccessListener(aVoid -> Log.i(TAG, "Campo 'saldo' inicializado para usuário existente."))
                            .addOnFailureListener(err -> Log.e(TAG, "Falha ao inicializar campo 'saldo' para usuário existente.", err));
                }
            } else {
                Log.d(TAG, "Documento do usuário não encontrado para UID: " + currentUser.getUid() + ". Tentando criar...");
                tvSaldo.setText("R$ 0,00"); // Mostra 0 enquanto tenta criar
                tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.green));

                Map<String, Object> initialUserData = new HashMap<>();
                initialUserData.put("email", currentUser.getEmail());
                // Se você salvou o nome completo durante o cadastro e quer adicioná-lo aqui:
                initialUserData.put("nomeCompleto", currentUser.getDisplayName()); // Ou a forma como você armazenou
                initialUserData.put("saldo", 0.00);
                // Adicione outros campos padrão que deveriam existir no documento do usuário
                // Se você adicionou 'nomeCompleto' em telacadastro, certifique-se de que está aqui também
                // ou obtenha do FirebaseUser se disponível (displayName).

                userDocumentRef.set(initialUserData) // Use .set() para criar o documento
                        .addOnSuccessListener(aVoid -> {
                            Log.i(TAG, "Documento do usuário criado com sucesso na tela principal para UID: " + currentUser.getUid());
                            // O listener do snapshot deve ser acionado novamente agora com o documento existindo,
                            // atualizando o saldo corretamente.
                        })
                        .addOnFailureListener(e_create -> {
                            Log.e(TAG, "Falha CRÍTICA ao criar documento do usuário na tela principal: ", e_create);
                            Toast.makeText(telaprincipal.this, "Erro ao inicializar dados do usuário. Tente novamente.", Toast.LENGTH_LONG).show();
                            // Aqui, talvez seja necessário deslogar o usuário ou tomar outra ação drástica,
                            // pois sem o documento base, muitas coisas não funcionarão.
                        });
            }
        });
    }

    private void configurarEselecionarMesAtualNaBarra() {
        llMesesContainer.removeAllViews(); // Limpa botões antigos se houver reconstrução da activity

        Calendar calendario = Calendar.getInstance();
        anoSelecionadoGlobal = calendario.get(Calendar.YEAR); // Pega o ano atual
        int mesAtualSistema = calendario.get(Calendar.MONTH);  // Mês atual (0-11)

        for (int i = 0; i < NOMES_MESES.length; i++) {
            // Usar Button para melhor feedback de clique, mas pode ser TextView estilizado
            Button btnMes = new Button(this, null, android.R.attr.buttonBarButtonStyle);
            btnMes.setText(NOMES_MESES[i]);
            btnMes.setTextColor(Color.WHITE); // Cor do texto dos meses (ajuste conforme seu @color/green)
            btnMes.setTag(i); // Guarda o índice do mês (0-11) no botão

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Margens para espaçar os botões dos meses
            params.setMargins(getResources().getDimensionPixelSize(R.dimen.margem_entre_meses), 0, getResources().getDimensionPixelSize(R.dimen.margem_entre_meses), 0);
            btnMes.setLayoutParams(params);

            final int indiceMesLoop = i;
            btnMes.setOnClickListener(v -> {
                processarSelecaoDeMes((Button) v, indiceMesLoop, anoSelecionadoGlobal);
            });

            llMesesContainer.addView(btnMes);

            // Seleciona o mês atual por padrão ao carregar a tela
            if (i == mesAtualSistema) {
                processarSelecaoDeMes(btnMes, mesAtualSistema, anoSelecionadoGlobal);
            }
        }
    }
    // Crie em res/values/dimens.xml: <dimen name="margem_entre_meses">8dp</dimen> (ou o valor que desejar)


    private void processarSelecaoDeMes(Button btnMesClicado, int mesIndex, int ano) {
        if (ultimoBotaoMesSelecionado != null) {
            // Restaura o estilo do botão do mês anteriormente selecionado
            ultimoBotaoMesSelecionado.setTypeface(null, Typeface.NORMAL);
            // Pode adicionar outras alterações de estilo para "desselecionar"
        }
        // Destaca o novo botão do mês selecionado
        btnMesClicado.setTypeface(null, Typeface.BOLD_ITALIC); // Exemplo de destaque
        // Pode adicionar outras alterações de estilo para "selecionar"

        ultimoBotaoMesSelecionado = btnMesClicado;
        mesSelecionadoGlobal = mesIndex;
        // anoSelecionadoGlobal já foi definido (atualmente fixo no ano corrente)

        // Atualiza o título do extrato e carrega os dados
        String nomeMes = NOMES_MESES[mesIndex];
        tvTituloExtrato.setText("Extrato de " + nomeMes + "/" + ano);
        carregarTransacoesDoMesSelecionado(ano, mesIndex);
    }

    private void carregarTransacoesDoMesSelecionado(int ano, int mes) {
        if (userDocumentRef == null) {
            Log.e(TAG, "userDocumentRef é nulo. Não é possível carregar extrato.");
            listaDeTransacoesGlobais.clear();
            transacaoAdapter.notifyDataSetChanged();
            return;
        }

        // Definir o início e o fim do mês para a consulta no Firestore
        Calendar calInicio = Calendar.getInstance();
        calInicio.set(ano, mes, 1, 0, 0, 0); // Primeiro dia do mês, meia-noite
        calInicio.set(Calendar.MILLISECOND, 0);
        Date dataInicioMes = calInicio.getTime();

        Calendar calFim = Calendar.getInstance();
        // Vai para o próximo mês e depois volta um milissegundo para pegar o final exato do mês atual
        calFim.set(ano, mes + 1, 1, 0, 0, 0);
        calFim.set(Calendar.MILLISECOND, 0);
        calFim.add(Calendar.MILLISECOND, -1); // Último milissegundo do mês selecionado
        Date dataFimMes = calFim.getTime();

        Timestamp timestampInicio = new Timestamp(dataInicioMes);
        Timestamp timestampFim = new Timestamp(dataFimMes);

        Log.d(TAG, "Carregando extrato para Mês/Ano: " + (mes + 1) + "/" + ano);
        Log.d(TAG, "Query entre: " + dataInicioMes.toString() + " E " + dataFimMes.toString());

        if (extratoListenerRegistration != null) {
            extratoListenerRegistration.remove();
        }

        // Listener para as transações do mês (atualiza em tempo real)
        userDocumentRef.collection("transacoes")
                .whereGreaterThanOrEqualTo("data", timestampInicio)
                .whereLessThanOrEqualTo("data", timestampFim)
                .orderBy("data", Query.Direction.DESCENDING) // Transações mais recentes primeiro
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Erro ao carregar extrato do mês: ", e);
                            Toast.makeText(telaprincipal.this, "Erro ao carregar extrato.", Toast.LENGTH_SHORT).show();
                            listaDeTransacoesGlobais.clear(); // Limpa em caso de erro também
                            transacaoAdapter.atualizarLista(null); // Atualiza o adapter
                            return;
                        }

                        List<Transacao> transacoesDoMes = new ArrayList<>();
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Transacao transacao = doc.toObject(Transacao.class);
                                transacao.setId(doc.getId()); // Guarda o ID do documento do Firestore
                                transacoesDoMes.add(transacao);
                                Log.d(TAG, "Transação Encontrada: " + transacao.getDescricao() + " Data: " + (transacao.getData() != null ? transacao.getData().toDate() : "N/A"));
                            }
                        }
                        Log.d(TAG, "Extrato atualizado com " + transacoesDoMes.size() + " transações.");
                        transacaoAdapter.atualizarLista(transacoesDoMes);

                        if (transacoesDoMes.isEmpty()) {
                            Log.d(TAG, "Nenhuma transação encontrada para o mês selecionado.");
                            // Poderia mostrar uma mensagem no lugar do RecyclerView ou um item "vazio"
                        }
                    }
                });
    }
}
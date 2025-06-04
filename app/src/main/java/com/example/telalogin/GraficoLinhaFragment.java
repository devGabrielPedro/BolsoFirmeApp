package com.example.telalogin; // Ou seu pacote

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry; // Cuidado! Não confundir com Map.Entry
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraficoLinhaFragment extends Fragment {

    private LineChart lineChart;
    private TextView tvTituloGraficoLinha, tvSemDadosGraficoLinha;
    private Spinner spinnerCategoriaLinha;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<String> listaNomesCategorias;

    private static final String TAG = "GraficoLinhaFragment";
    private final int NUMERO_MESES_PARA_EVOLUCAO = 6; // Analisar os últimos 6 meses
    private SimpleDateFormat sdfMesAnoLabel = new SimpleDateFormat("MMM/yy", new Locale("pt", "BR"));
    private String categoriaSelecionada = null;

    public GraficoLinhaFragment() {
        // Construtor público vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentLifecycle", "onCreateView: GraficoLinhaFragment");
        View view = inflater.inflate(R.layout.layout_fragment_grafico_linha, container, false);
        spinnerCategoriaLinha = view.findViewById(R.id.spinnerCategoriaLinha);
        if (spinnerCategoriaLinha == null) {
            Log.e("GraficoLinhaFragment", "ERRO FATAL: spinnerCategoriaLinha NÃO FOI ENCONTRADO NO LAYOUT!");
        }

        lineChart = view.findViewById(R.id.lineChartEvolucaoDespesas);
        tvTituloGraficoLinha = view.findViewById(R.id.tvTituloGraficoLinha);
        tvSemDadosGraficoLinha = view.findViewById(R.id.tvSemDadosGraficoLinha);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            if(getContext() != null) Toast.makeText(getContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return view;
        }

        configurarLineChart();
        configurarSpinnerCategorias();

        return view;
    }

    private void configurarLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Intervalo mínimo

        lineChart.getAxisRight().setEnabled(false); // Desabilita eixo Y da direita
        lineChart.getAxisLeft().setAxisMinimum(0f); // Começa do zero
        lineChart.getAxisLeft().setDrawGridLines(true);
    }

    private void configurarSpinnerCategorias() {
        listaNomesCategorias = new ArrayList<>();
        // Adiciona "Todas as Categorias" como primeira opção
        listaNomesCategorias.add(getString(R.string.todas_as_categorias));

        // Carrega as categorias do array de strings (o mesmo usado na TelaSaidaActivity)
        String[] categoriasApp = getResources().getStringArray(R.array.categorias_despesa_array);
        Collections.addAll(listaNomesCategorias, categoriasApp);

        if (getContext() == null) return; // Segurança
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, listaNomesCategorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaLinha.setAdapter(adapter);

        spinnerCategoriaLinha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "Todas as Categorias"
                    categoriaSelecionada = null; // Sem filtro de categoria
                } else {
                    categoriaSelecionada = parent.getItemAtPosition(position).toString();
                }
                Log.d(TAG, "Categoria selecionada para gráfico de linha: " + categoriaSelecionada);
                carregarDadosEvolucaoDespesas(); // Recarrega o gráfico com o novo filtro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                categoriaSelecionada = null;
                carregarDadosEvolucaoDespesas();
            }
        });
        // Define "Todas as Categorias" como padrão e carrega os dados iniciais
        spinnerCategoriaLinha.setSelection(0, false); // false para não disparar o listener na configuração inicial
        categoriaSelecionada = null; // Garante que o estado inicial é "Todas"
        // carregarDadosEvolucaoDespesas(); // Já será chamado pelo setOnItemSelectedListener ou o primeiro carregamento é feito no onStart do fragment
    }


    private void carregarDadosEvolucaoDespesas() {
        String titulo = "Evolução Despesas Totais";
        if (categoriaSelecionada != null) {
            titulo = "Evolução: " + categoriaSelecionada;
        }
        tvTituloGraficoLinha.setText(titulo + " (Últimos " + NUMERO_MESES_PARA_EVOLUCAO + " Meses)");

        // ... (Lógica para obter listaNomesMeses e LinkedHashMap despesasMensais como antes) ...
        Map<String, Double> despesasMensais = new LinkedHashMap<>();
        List<String> labelsMeses = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < NUMERO_MESES_PARA_EVOLUCAO; i++) {
            if (i > 0) cal.add(Calendar.MONTH, -1);
            Calendar mesCorrenteIteracao = (Calendar) cal.clone();
            String labelMes = sdfMesAnoLabel.format(mesCorrenteIteracao.getTime());
            labelsMeses.add(0, labelMes);
            // despesasMensais.put(labelMes, 0.0); // Será preenchido após as tasks

            Calendar inicioMesCal = (Calendar) mesCorrenteIteracao.clone(); /* ... */ Timestamp tsInicio = new Timestamp(inicioMesCal.getTime());
            inicioMesCal.set(Calendar.DAY_OF_MONTH, 1);
            configurarHorarioInicioDia(inicioMesCal);
            tsInicio = new Timestamp(inicioMesCal.getTime());

            Calendar fimMesCal = (Calendar) mesCorrenteIteracao.clone(); /* ... */ Timestamp tsFim = new Timestamp(fimMesCal.getTime());
            fimMesCal.set(Calendar.DAY_OF_MONTH, fimMesCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            configurarHorarioFimDia(fimMesCal);
            tsFim = new Timestamp(fimMesCal.getTime());

            // MODIFICAÇÃO NA QUERY
            Query queryDespesas = db.collection("usuarios").document(currentUser.getUid()).collection("transacoes")
                    .whereEqualTo("tipo", "saida")
                    .whereGreaterThanOrEqualTo("data", tsInicio)
                    .whereLessThanOrEqualTo("data", tsFim);

            if (categoriaSelecionada != null) { // Adiciona filtro de categoria se uma foi selecionada
                queryDespesas = queryDespesas.whereEqualTo("categoria", categoriaSelecionada);
                Log.d(TAG, "Filtrando despesas pela categoria: " + categoriaSelecionada);
            } else {
                Log.d(TAG, "Carregando todas as categorias de despesa.");
            }

            tasks.add(queryDespesas.get()); // Adiciona a task à lista
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            if (getActivity() == null || !isAdded()) return;

            Map<String, Double> despesasMensaisOrdenadas = new LinkedHashMap<>();
            boolean houveErro = false;

            for(String label : labelsMeses){ // Inicializa o mapa ordenado
                despesasMensaisOrdenadas.put(label, 0.0);
            }

            // Processar os resultados das tasks
            // A ordem das tasks corresponde à ordem inversa dos labelsMeses (do mais recente ao mais antigo)
            for (int i = 0; i < tasks.size(); i++) {
                Task<QuerySnapshot> task = tasks.get(i);
                // O label correspondente a tasks.get(i) é labelsMeses.get(labelsMeses.size() - 1 - i)
                String labelMesTask = labelsMeses.get(labelsMeses.size() - 1 - i);

                if (task.isSuccessful() && task.getResult() != null) {
                    double totalDespesasMes = 0;
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Transacao t = doc.toObject(Transacao.class);
                        totalDespesasMes += t.getValor();
                    }
                    despesasMensaisOrdenadas.put(labelMesTask, totalDespesasMes);
                } else {
                    Log.e(TAG, "Erro ao buscar despesas para o mês " + labelMesTask + ": ", task.getException());
                    houveErro = true;
                }
            }
            // labelsMeses já está na ordem cronológica correta (mais antigo -> mais recente)
            // devido ao add(0, labelMes).

            if (houveErro) {
                // ... (tratar erro)
                if(getContext() != null) Toast.makeText(getContext(), "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                lineChart.clear();
                lineChart.setVisibility(View.GONE);
                tvSemDadosGraficoLinha.setVisibility(View.VISIBLE);
                tvSemDadosGraficoLinha.setText("Erro ao carregar dados.");
            } else {
                popularLineChart(despesasMensaisOrdenadas, labelsMeses);
            }
        });
    }

    private void popularLineChart(Map<String, Double> despesasMensais, List<String> labelsMeses) {
        if (despesasMensais.isEmpty() || labelsMeses.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvSemDadosGraficoLinha.setVisibility(View.VISIBLE);
            return;
        }
        lineChart.setVisibility(View.VISIBLE);
        tvSemDadosGraficoLinha.setVisibility(View.GONE);

        boolean dadosEncontrados = false;
        for(Double valor : despesasMensais.values()){
            if(valor > 0) {
                dadosEncontrados = true;
                break;
            }
        }

        if (!dadosEncontrados) {
            lineChart.clear(); // Limpa dados anteriores
            lineChart.setVisibility(View.GONE);
            tvSemDadosGraficoLinha.setVisibility(View.VISIBLE);
            tvSemDadosGraficoLinha.setText("Nenhuma despesa encontrada para '" + (categoriaSelecionada != null ? categoriaSelecionada : "Todas as Categorias") + "' neste período.");
            return;
        }
        lineChart.setVisibility(View.VISIBLE);
        tvSemDadosGraficoLinha.setVisibility(View.GONE);

        ArrayList<Entry> entries = new ArrayList<>();
        int i = 0;
        for (String mesLabel : labelsMeses) { // labelsMeses deve estar em ordem cronológica
            Double valorDespesa = despesasMensais.get(mesLabel);
            if (valorDespesa != null) {
                entries.add(new Entry(i, valorDespesa.floatValue()));
            } else {
                entries.add(new Entry(i, 0f)); // Se não houver dado para o mês
            }
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, categoriaSelecionada != null ? categoriaSelecionada : "Despesas Totais");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.red)); // Sua cor de saída
        if (getContext() != null) {
            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.red)); // Ou R.color.cor_saida
            dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.red)); // Ou R.color.cor_saida
            dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.green)); // Ou alguma cor para o preenchimento, talvez com alfa
        } else {
            dataSet.setColor(Color.RED); // Fallback
            dataSet.setCircleColor(Color.RED);
            dataSet.setFillColor(Color.GREEN); // Fallback
        }
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true); // Preenchimento abaixo da linha
        dataSet.setFillAlpha(100); // Transparência do preenchimento
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Linha suavizada

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsMeses));
        xAxis.setLabelCount(labelsMeses.size(), false); // Ajusta o número de labels

        lineChart.invalidate(); // Atualiza o gráfico
        lineChart.animateX(1500); // Animação no eixo X
    }


    private void configurarHorarioInicioDia(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void configurarHorarioFimDia(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }
}
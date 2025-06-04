package com.example.telalogin; // Ou seu pacote

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter; // Para formatar eixo X com nomes de meses
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.HashMap;
import java.util.LinkedHashMap; // Para manter a ordem de inserção dos meses
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraficoBarrasFragment extends Fragment {

    private BarChart barChart;
    private TextView tvTituloGraficoBarras, tvSemDadosGraficoBarras;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private static final String TAG = "GraficoBarrasFragment";
    private final int NUMERO_MESES_PARA_ANALISE = 6; // Analisar os últimos 6 meses
    private SimpleDateFormat sdfMesAnoLabel = new SimpleDateFormat("MMM/yy", new Locale("pt", "BR"));


    public GraficoBarrasFragment() {
        // Construtor público vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentLifecycle", "onCreateView: GraficoBarrasFragment");
        View view = inflater.inflate(R.layout.layout_fragment_grafico_barras, container, false);

        barChart = view.findViewById(R.id.barChartReceitasDespesas);
        tvTituloGraficoBarras = view.findViewById(R.id.tvTituloGraficoBarras);
        tvSemDadosGraficoBarras = view.findViewById(R.id.tvSemDadosGraficoBarras);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            if(getContext() != null) Toast.makeText(getContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return view;
        }

        configurarBarChart();
        carregarDadosReceitasDespesas();

        return view;
    }

    private void configurarBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false); // Se usar barras agrupadas

        // Eixo X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Intervalo mínimo entre os valores do eixo X

        // Eixo Y (Esquerdo)
        barChart.getAxisLeft().setAxisMinimum(0f); // Começa do zero
        barChart.getAxisLeft().setDrawGridLines(true);

        // Eixo Y (Direito) - desabilitar
        barChart.getAxisRight().setEnabled(false);

        // Legenda
        barChart.getLegend().setEnabled(true);
    }


    private void carregarDadosReceitasDespesas() {
        tvTituloGraficoBarras.setText("Receitas vs Despesas (Últimos " + NUMERO_MESES_PARA_ANALISE + " Meses)");
        // Usaremos um LinkedHashMap para manter a ordem dos meses
        Map<String, MensalData> dadosMensais = new LinkedHashMap<>();
        List<String> labelsMeses = new ArrayList<>(); // Para o eixo X

        Calendar cal = Calendar.getInstance(); // Começa com o mês atual

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < NUMERO_MESES_PARA_ANALISE; i++) {
            // Define o mês para consulta (cal já está no mês correto na primeira iteração, depois decrementa)
            if (i > 0) { // Para os meses anteriores
                cal.add(Calendar.MONTH, -1);
            }
            Calendar mesCorrenteIteracao = (Calendar) cal.clone(); // Clona para não afetar o 'cal' principal no loop

            String labelMes = sdfMesAnoLabel.format(mesCorrenteIteracao.getTime());
            labelsMeses.add(0, labelMes); // Adiciona no início para ter a ordem cronológica correta no final
            dadosMensais.put(labelMes, new MensalData()); // Prepara a estrutura de dados

            // Definir início e fim do mês
            Calendar inicioMesCal = (Calendar) mesCorrenteIteracao.clone();
            inicioMesCal.set(Calendar.DAY_OF_MONTH, 1);
            inicioMesCal.set(Calendar.HOUR_OF_DAY, 0);
            inicioMesCal.set(Calendar.MINUTE, 0);
            inicioMesCal.set(Calendar.SECOND, 0);
            inicioMesCal.set(Calendar.MILLISECOND, 0);
            Timestamp tsInicio = new Timestamp(inicioMesCal.getTime());

            Calendar fimMesCal = (Calendar) mesCorrenteIteracao.clone();
            fimMesCal.set(Calendar.DAY_OF_MONTH, fimMesCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            fimMesCal.set(Calendar.HOUR_OF_DAY, 23);
            fimMesCal.set(Calendar.MINUTE, 59);
            fimMesCal.set(Calendar.SECOND, 59);
            fimMesCal.set(Calendar.MILLISECOND, 999);
            Timestamp tsFim = new Timestamp(fimMesCal.getTime());

            Query queryBase = db.collection("usuarios").document(currentUser.getUid()).collection("transacoes")
                    .whereGreaterThanOrEqualTo("data", tsInicio)
                    .whereLessThanOrEqualTo("data", tsFim);

            // Adiciona task para buscar entradas
            tasks.add(queryBase.whereEqualTo("tipo", "entrada").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        double totalEntradas = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Transacao t = doc.toObject(Transacao.class);
                            totalEntradas += t.getValor();
                        }
                        MensalData data = dadosMensais.get(labelMes);
                        if (data != null) data.receita = totalEntradas;
                    }));

            // Adiciona task para buscar saídas
            tasks.add(queryBase.whereEqualTo("tipo", "saida").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        double totalSaidas = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Transacao t = doc.toObject(Transacao.class);
                            totalSaidas += t.getValor();
                        }
                        MensalData data = dadosMensais.get(labelMes);
                        if (data != null) data.despesa = totalSaidas;
                    }));
        }

        // Quando todas as tasks (queries) terminarem
        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            if (getActivity() == null || !isAdded()) return; // Fragment não está mais anexado

            boolean sucessoGeral = true;
            for(Task<?> task : allTasks.getResult()) {
                if (!task.isSuccessful()) {
                    sucessoGeral = false;
                    Log.e(TAG, "Erro em uma das queries de Receita/Despesa", task.getException());
                    break;
                }
            }

            if (sucessoGeral) {
                popularBarChart(dadosMensais, labelsMeses);
            } else {
                if(getContext() != null) Toast.makeText(getContext(), "Erro ao carregar dados para o gráfico de barras.", Toast.LENGTH_SHORT).show();
                barChart.setVisibility(View.GONE);
                tvSemDadosGraficoBarras.setVisibility(View.VISIBLE);
                tvSemDadosGraficoBarras.setText("Erro ao carregar dados.");
            }
        });
    }


    private void popularBarChart(Map<String, MensalData> dadosMensais, List<String> labelsMeses) {
        if (dadosMensais.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvSemDadosGraficoBarras.setVisibility(View.VISIBLE);
            return;
        }
        barChart.setVisibility(View.VISIBLE);
        tvSemDadosGraficoBarras.setVisibility(View.GONE);

        ArrayList<BarEntry> receitasEntries = new ArrayList<>();
        ArrayList<BarEntry> despesasEntries = new ArrayList<>();

        // É importante que a ordem dos labelsMeses corresponda à ordem que os dados foram buscados e serão adicionados
        // O LinkedHashMap em dadosMensais mantém a ordem de inserção (que foi do mês mais recente para o mais antigo)
        // Mas os labelsMeses foram construídos na ordem cronológica (mais antigo para mais recente)
        // Precisamos garantir consistência. O XAxis formatter usará labelsMeses.

        int index = 0;
        for (String mesLabel : labelsMeses) { // Iterar na ordem cronológica dos labels
            MensalData data = dadosMensais.get(mesLabel); // Buscar dados pelo label
            if(data != null) {
                receitasEntries.add(new BarEntry(index, (float) data.receita));
                despesasEntries.add(new BarEntry(index, (float) data.despesa));
            } else { // Caso não haja dados para um mês (improvável com a inicialização, mas seguro)
                receitasEntries.add(new BarEntry(index, 0f));
                despesasEntries.add(new BarEntry(index, 0f));
            }
            index++;
        }

        BarDataSet setReceitas = new BarDataSet(receitasEntries, "Receitas");
        setReceitas.setColor(ContextCompat.getColor(getContext(), R.color.green)); // Sua cor de entrada

        BarDataSet setDespesas = new BarDataSet(despesasEntries, "Despesas");
        setDespesas.setColor(ContextCompat.getColor(getContext(), R.color.red));   // Sua cor de saída

        float barWidth = 0.35f; // Largura da barra
        float barSpace = 0.05f; // Espaço entre as barras de um mesmo grupo
        float groupSpace = 0.20f; // Espaço entre os grupos de barras

        BarData barData = new BarData(setReceitas, setDespesas);
        barData.setBarWidth(barWidth);

        barChart.setData(barData);
        // (barWidth + barSpace) * N + groupSpace = 1.00 -> onde N é o número de barras em um grupo
        // (0.35 + 0.05) * 2 + 0.20 = 0.40 * 2 + 0.20 = 0.80 + 0.20 = 1.00
        barChart.groupBars(0f, groupSpace, barSpace); // O primeiro parâmetro é o X de início dos grupos

        // Configurar o Eixo X para mostrar os nomes dos meses
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsMeses));
        xAxis.setCenterAxisLabels(true); // Centraliza os labels entre os grupos de barras
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        // Ajusta o máximo para mostrar todos os grupos corretamente
        xAxis.setAxisMaximum(labelsMeses.size());


        barChart.invalidate(); // Atualiza o gráfico
        barChart.animateY(1500);
    }

    // Classe auxiliar para armazenar dados mensais
    private static class MensalData {
        double receita = 0;
        double despesa = 0;
    }
}
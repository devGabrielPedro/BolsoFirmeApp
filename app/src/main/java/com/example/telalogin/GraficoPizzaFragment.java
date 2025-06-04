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
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraficoPizzaFragment extends Fragment {

    private PieChart pieChartDespesas;
    private TextView tvTituloGraficoPizza, tvSemDadosGraficoPizza;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "GraficoPizzaFragment";

    public GraficoPizzaFragment() {
        // Construtor público vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentLifecycle", "onCreateView: GraficoPizzaFragment");
        View view = inflater.inflate(R.layout.layout_fragment_grafico_pizza, container, false);

        pieChartDespesas = view.findViewById(R.id.pieChartDespesasFragment);
        tvTituloGraficoPizza = view.findViewById(R.id.tvTituloGraficoPizza);
        tvSemDadosGraficoPizza = view.findViewById(R.id.tvSemDadosGraficoPizza);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            // Lidar com a ausência do usuário, talvez fechar o fragmento ou activity pai
            return view;
        }

        configurarPieChart();
        carregarDadosDespesasDoMesAtual();

        return view;
    }

    // --- Mova os métodos configurarPieChart(), carregarDadosDespesasDoMesAtual(),
    // --- e popularPieChart() da TelaGraficosActivity original para cá,
    // --- ajustando o contexto se necessário (ex: getContext() em vez de 'this' para Toasts).

    private void configurarPieChart() {
        // ... (código idêntico ao da sua TelaGraficosActivity anterior)
        pieChartDespesas.setUsePercentValues(true);
        pieChartDespesas.getDescription().setEnabled(false);
        pieChartDespesas.setExtraOffsets(5, 10, 5, 5);
        pieChartDespesas.setDragDecelerationFrictionCoef(0.95f);
        pieChartDespesas.setDrawHoleEnabled(true);
        pieChartDespesas.setHoleColor(Color.WHITE);
        pieChartDespesas.setTransparentCircleRadius(61f);
        pieChartDespesas.setDrawCenterText(true);
        pieChartDespesas.setCenterText("Despesas\nMês Atual"); // Ajustado
        pieChartDespesas.setCenterTextSize(16f);
        Legend legend = pieChartDespesas.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        // ... (resto da configuração da legenda e gráfico)
        pieChartDespesas.setEntryLabelColor(Color.BLACK);
        pieChartDespesas.setEntryLabelTextSize(12f);
    }

    private void carregarDadosDespesasDoMesAtual() {
        // ... (código idêntico ao da sua TelaGraficosActivity anterior para buscar dados)
        // ... lembre-se de usar getContext() para Toasts se necessário
        Calendar cal = Calendar.getInstance();
        int anoAtual = cal.get(Calendar.YEAR);
        int mesAtual = cal.get(Calendar.MONTH);

        String nomeMes = new SimpleDateFormat("MMMM", new Locale("pt", "BR")).format(cal.getTime());
        tvTituloGraficoPizza.setText("Despesas de " + nomeMes.substring(0,1).toUpperCase() + nomeMes.substring(1) + "/" + anoAtual);

        Calendar calInicio = Calendar.getInstance(); /* ... set Y,M,D ... */ Date dataInicioMes = calInicio.getTime();
        calInicio.set(anoAtual, mesAtual, 1, 0, 0, 0);
        calInicio.set(Calendar.MILLISECOND, 0);
        dataInicioMes = calInicio.getTime();


        Calendar calFim = Calendar.getInstance(); /* ... set Y,M,D ... */ Date dataFimMes = calFim.getTime();
        calFim.set(anoAtual, mesAtual, calInicio.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calFim.set(Calendar.MILLISECOND, 999);
        dataFimMes = calFim.getTime();


        Timestamp timestampInicio = new Timestamp(dataInicioMes);
        Timestamp timestampFim = new Timestamp(dataFimMes);

        db.collection("usuarios").document(currentUser.getUid()).collection("transacoes")
                .whereEqualTo("tipo", "saida")
                .whereGreaterThanOrEqualTo("data", timestampInicio)
                .whereLessThanOrEqualTo("data", timestampFim)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // ... (lógica idêntica para processar e chamar popularPieChart)
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Nenhuma despesa encontrada para o mês atual.");
                        pieChartDespesas.setVisibility(View.GONE);
                        tvSemDadosGraficoPizza.setVisibility(View.VISIBLE);
                        pieChartDespesas.clear();
                        pieChartDespesas.invalidate();
                        return;
                    }
                    pieChartDespesas.setVisibility(View.VISIBLE);
                    tvSemDadosGraficoPizza.setVisibility(View.GONE);
                    Map<String, Double> despesasPorCategoria = new HashMap<>();
                    // ... (loop e agregação)
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Transacao transacao = document.toObject(Transacao.class);
                        if (transacao.getCategoria() != null && !transacao.getCategoria().isEmpty()) {
                            String categoria = transacao.getCategoria();
                            double valor = transacao.getValor();
                            despesasPorCategoria.put(categoria, despesasPorCategoria.getOrDefault(categoria, 0.0) + valor);
                        }
                    }
                    popularPieChart(despesasPorCategoria);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar despesas: ", e);
                    if(getContext() != null) Toast.makeText(getContext(), "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                    pieChartDespesas.setVisibility(View.GONE);
                    tvSemDadosGraficoPizza.setVisibility(View.VISIBLE);
                    tvSemDadosGraficoPizza.setText("Erro ao carregar dados.");
                });
    }

    private void popularPieChart(Map<String, Double> despesasPorCategoria) {
        // ... (código idêntico ao da sua TelaGraficosActivity anterior)
        if (despesasPorCategoria.isEmpty()) { /* ... tratar ... */ return; }
        ArrayList<PieEntry> entries = new ArrayList<>();
        // ... (loop para criar entries)
        for (Map.Entry<String, Double> entry : despesasPorCategoria.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        // ... (configurar dataSet com cores, etc.)
        dataSet.setSliceSpace(3f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Usar cores diferentes
        // Ou use a lista de cores que você tinha antes
        // ArrayList<Integer> colors = new ArrayList<>();
        // for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c); ...
        // dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartDespesas));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChartDespesas.setData(data);
        pieChartDespesas.invalidate();
        pieChartDespesas.animateY(1400);
    }
}
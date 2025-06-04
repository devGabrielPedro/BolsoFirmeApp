package com.example.telalogin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TelaGraficosActivity extends AppCompatActivity {

    private TabLayout tabLayoutGraficos;
    private ViewPager2 viewPagerGraficos;
    private GraficosPagerAdapter graficosPagerAdapter;

    // Títulos para as abas
    private final String[] titulosAbas = new String[]{"Categorias", "Receita vs Despesa", "Evolução Despesas"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_graficos);

        tabLayoutGraficos = findViewById(R.id.tabLayoutGraficos);
        viewPagerGraficos = findViewById(R.id.viewPagerGraficos);

        graficosPagerAdapter = new GraficosPagerAdapter(this);
        viewPagerGraficos.setAdapter(graficosPagerAdapter);

        // Conectar o TabLayout com o ViewPager2
        new TabLayoutMediator(tabLayoutGraficos, viewPagerGraficos,
                (tab, position) -> tab.setText(titulosAbas[position])
        ).attach();

        // Lógica de carregamento de dados específica para cada gráfico
        // será movida para os respectivos Fragments.
    }
}
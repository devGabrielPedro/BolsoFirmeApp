package com.example.telalogin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GraficosPagerAdapter extends FragmentStateAdapter {

    public GraficosPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new GraficoPizzaFragment(); // Fragmento para o gráfico de pizza
            case 1:
                return new GraficoBarrasFragment(); // Fragmento para o gráfico de barras
            case 2: // NOVO CASO
                return new GraficoLinhaFragment(); // Fragmento para o gráfico de linha
            default:
                return new GraficoPizzaFragment(); // Fallback
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Número de abas/gráficos
    }
}
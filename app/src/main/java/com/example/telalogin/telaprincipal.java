package com.example.telalogin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class telaprincipal extends AppCompatActivity {

    // Declaração dos componentes que serão usados
    TextView tvSaldo, tvExtrato;
    Button btnSaida, btnGraficos, btnEntrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telaprincipal); // Conecta com o XML

        // Conectando os elementos do XML com o Java
        tvSaldo = findViewById(R.id.tvSaldo);
        tvExtrato = findViewById(R.id.tvExtrato);
        btnSaida = findViewById(R.id.btnSaida);
        btnGraficos = findViewById(R.id.btnGraficos);
        btnEntrada = findViewById(R.id.btnEntrada);

        // Simulação de um valor de saldo
        double saldo = -200.50; // Aqui você colocaria o valor real do banco de dados

        // Atualiza o texto do saldo
        tvSaldo.setText("R$ " + String.format("%.2f", saldo));

        // Muda a cor dependendo do valor do saldo
        if (saldo < 0) {
            tvSaldo.setTextColor(Color.RED); // Saldo negativo
        } else {
            tvSaldo.setTextColor(Color.parseColor("#1B5E20")); // Verde escuro
        }

        // Ações dos botões
        btnSaida.setOnClickListener(v -> {
            // Aqui você colocaria a lógica para registrar uma saída
            tvExtrato.setText("Você clicou em Saída.");
        });

        btnGraficos.setOnClickListener(v -> {
            // Aqui você abriria outra tela ou gráfico
            tvExtrato.setText("Você clicou em Gráficos.");
        });

        btnEntrada.setOnClickListener(v -> {
            // Aqui você colocaria a lógica para registrar uma entrada
            tvExtrato.setText("Você clicou em Entrada.");
        });
    }
}
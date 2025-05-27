package com.example.telalogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class telalogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button buttonLogin;
    private TextView etCadastrese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telalogin);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        etCadastrese = findViewById(R.id.etCadastrese);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String senha = etPassword.getText().toString().trim();

                // Simulação de verificação
                if (email.equals("admin@email.com") && senha.equals("123456")) {
                    Toast.makeText(telalogin.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

                    // Ir para a tela principal
                    Intent intent = new Intent(telalogin.this, telaprincipal.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(telalogin.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        etCadastrese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir para tela de cadastro
                Intent intent = new Intent(telalogin.this, telacadastro.class);
                startActivity(intent);
            }
        });
    }
}
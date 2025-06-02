package com.example.telalogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class telalogin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button buttonLogin;
    private TextView etCadastrese;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telalogin);

        // Inicialização do Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        etCadastrese = findViewById(R.id.etCadastrese);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(); // Chamará o login do Firebase
            }
        });

        // Ação do TextView "Cadastre-se" (mantida, mas agora telacadastro.java será funcional)
        etCadastrese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(telalogin.this, telacadastro.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verifique se o usuário já está logado ao iniciar a activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuário já logado, redirecionar para tela principal
            Toast.makeText(this, "Usuário já conectado: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(telalogin.this, telaprincipal.class);
            startActivity(intent);
            finish(); // Finaliza a tela de login para não voltar com o botão "back"
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();

        // Validações básicas (você pode adicionar mais)
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email é obrigatório.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            etPassword.setError("Senha é obrigatória.");
            etPassword.requestFocus();
            return;
        }

        // Autenticação com Firebase
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login bem-sucedido
                            Toast.makeText(telalogin.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                            // FirebaseUser user = mAuth.getCurrentUser(); // Informações do usuário, se necessário
                            // Ir para a tela principal
                            Intent intent = new Intent(telalogin.this, telaprincipal.class);
                            startActivity(intent);
                            finish(); // Finaliza a tela de login
                        } else {
                            // Se o login falhar, exiba uma mensagem para o usuário.
                            Toast.makeText(telalogin.this, "Email ou senha incorretos. Falha: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
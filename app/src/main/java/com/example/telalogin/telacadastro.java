package com.example.telalogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Para o etLogin
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class telacadastro extends AppCompatActivity {

    // Seus atributos de UI conforme especificado
    private EditText etNomeCompleto;
    private EditText etEmailCadastro;
    private EditText etSenhaCadastro;
    private EditText etConfirmacaoSenha;
    private Button buttonCadastro;
    private TextView etLogin; // Texto "Já tem login?"

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG_CADASTRO = "TelaCadastro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Certifique-se que seu layout XML se chama 'telacadastro.xml' ou ajuste aqui
        setContentView(R.layout.telacadastro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referenciando os componentes do layout XML pelos IDs que você forneceu
        etNomeCompleto = findViewById(R.id.etNomeCompleto);
        etEmailCadastro = findViewById(R.id.etEmailCadastro);
        etSenhaCadastro = findViewById(R.id.etSenhaCadastro);
        etConfirmacaoSenha = findViewById(R.id.etConfirmacaoSenha);
        buttonCadastro = findViewById(R.id.buttonCadastro);
        etLogin = findViewById(R.id.etLogin);

        buttonCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarEProcessarCadastro();
            }
        });

        etLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Voltar para a tela de login
                Intent intent = new Intent(telacadastro.this, telalogin.class);
                // Adicionar flags para limpar a pilha se a tela de login já estiver aberta
                // ou para garantir que não haja múltiplas instâncias da tela de login.
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Finaliza a tela de cadastro ao voltar para o login
            }
        });
    }

    private void validarEProcessarCadastro() {
        String nomeCompleto = etNomeCompleto.getText().toString().trim();
        String email = etEmailCadastro.getText().toString().trim();
        String senha = etSenhaCadastro.getText().toString().trim();
        String confirmacaoSenha = etConfirmacaoSenha.getText().toString().trim();

        // Validações
        if (TextUtils.isEmpty(nomeCompleto)) {
            etNomeCompleto.setError("Nome completo é obrigatório.");
            etNomeCompleto.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmailCadastro.setError("Email é obrigatório.");
            etEmailCadastro.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailCadastro.setError("Por favor, insira um email válido.");
            etEmailCadastro.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            etSenhaCadastro.setError("Senha é obrigatória.");
            etSenhaCadastro.requestFocus();
            return;
        }

        if (senha.length() < 6) {
            etSenhaCadastro.setError("A senha deve ter no mínimo 6 caracteres.");
            etSenhaCadastro.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmacaoSenha)) {
            etConfirmacaoSenha.setError("Confirmação de senha é obrigatória.");
            etConfirmacaoSenha.requestFocus();
            return;
        }

        if (!senha.equals(confirmacaoSenha)) {
            etConfirmacaoSenha.setError("As senhas não coincidem.");
            etConfirmacaoSenha.requestFocus();
            // Limpar campos de senha para segurança e conveniência
            etSenhaCadastro.setText("");
            etConfirmacaoSenha.setText("");
            return;
        }

        // Se todas as validações passarem, prosseguir com o cadastro no Firebase
        criarNovoUsuarioFirebase(nomeCompleto, email, senha);
    }

    private void criarNovoUsuarioFirebase(String nomeCompleto, String email, String senha) {
        // Desabilitar o botão para evitar múltiplos cliques durante o processo
        buttonCadastro.setEnabled(false);
        Toast.makeText(telacadastro.this, "Processando cadastro...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(telacadastro.this, "Usuário autenticado com sucesso!", Toast.LENGTH_SHORT).show();
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Usuário criado na autenticação, agora crie o documento no Firestore
                                criarDocumentoUsuarioFirestore(firebaseUser.getUid(), nomeCompleto, email);
                            } else {
                                buttonCadastro.setEnabled(true); // Reabilitar botão
                                Toast.makeText(telacadastro.this, "Erro ao obter dados do usuário após autenticação.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            buttonCadastro.setEnabled(true); // Reabilitar botão em caso de falha
                            Log.w(TAG_CADASTRO, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(telacadastro.this, "Falha no cadastro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void criarDocumentoUsuarioFirestore(String userId, String nomeCompleto, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nomeCompleto", nomeCompleto); // Adicionando o nome completo
        userData.put("email", email);
        userData.put("saldo", 0.00); // Saldo inicial para o novo usuário

        db.collection("usuarios").document(userId)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG_CADASTRO, "Documento do usuário criado com ID: " + userId + " e nome: " + nomeCompleto);
                        Toast.makeText(telacadastro.this, "Dados do usuário salvos!", Toast.LENGTH_SHORT).show();

                        // Após cadastro e criação do doc, ir para a tela principal
                        Intent intent = new Intent(telacadastro.this, telaprincipal.class);
                        // Limpar stack de activities para não voltar para login/cadastro com o botão "back"
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Finaliza a tela de cadastro
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        buttonCadastro.setEnabled(true); // Reabilitar botão em caso de falha
                        Log.w(TAG_CADASTRO, "Erro ao criar documento do usuário no Firestore", e);
                        // O usuário foi autenticado, mas o doc não foi criado.
                        // Isso é um estado inconsistente. O ideal seria tentar remover o usuário autenticado ou
                        // fornecer uma forma de tentar criar o documento novamente.
                        // Por enquanto, apenas informamos o erro e deixamos o usuário na tela de cadastro.
                        Toast.makeText(telacadastro.this, "Usuário autenticado, mas houve um erro ao salvar seus dados: " + e.getMessage() + ". Por favor, tente logar ou contate o suporte.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
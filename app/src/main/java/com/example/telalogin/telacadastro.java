package com.example.telalogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class telacadastro extends AppCompatActivity {

    // Criando variáveis para acessar os componentes do XML
    private EditText etNomeCompleto, etEmailCadastro, etSenhaCadastro, etConfirmacaoSenha;
    private Button buttonCadastro;
    private TextView etLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Inicializa a tela
        setContentView(R.layout.telacadastro); // Carrega o XML da tela de cadastro

        // Ligando cada variável Java com os componentes do XML usando o ID
        etNomeCompleto = findViewById(R.id.etNomeCompleto); // Campo nome
        etEmailCadastro = findViewById(R.id.etEmailCadastro); // Campo e-mail
        etSenhaCadastro = findViewById(R.id.etSenhaCadastro); // Campo senha
        etConfirmacaoSenha = findViewById(R.id.etConfirmacaoSenha); // Campo confirmar senha
        buttonCadastro = findViewById(R.id.buttonCadastro); // Botão de cadastro
        etLogin = findViewById(R.id.etLogin); // Texto "Já tem login?"

        // Quando o botão de cadastro for clicado, executar a função realizarCadastro()
        buttonCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarCadastro(); // Chama a função
            }
        });

        // Quando clicar no texto "Já tem login?", ir para a tela de login
        etLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Criar uma intenção para abrir a tela de login
                Intent intent = new Intent(telacadastro.this, telalogin.class); // Troque MainActivity se o nome for outro
                startActivity(intent); // Inicia a nova tela
                finish(); // Fecha essa tela atual
            }
        });
    }

    // Função que executa o cadastro
    private void realizarCadastro() {
        // Pegando os textos digitados e removendo espaços antes/depois
        String nome = etNomeCompleto.getText().toString().trim();
        String email = etEmailCadastro.getText().toString().trim();
        String senha = etSenhaCadastro.getText().toString().trim();
        String confirmacaoSenha = etConfirmacaoSenha.getText().toString().trim();

        // Verificando se algum campo está vazio
        Toast Toast = null;
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(senha) || TextUtils.isEmpty(confirmacaoSenha)) {

            // Mostra uma mensagem rápida avisando que faltou preencher algo
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return; // Sai da função
        }

        // Verifica se a senha digitada é igual à confirmação
        if (!senha.equals(confirmacaoSenha)) {
            Toast.makeText(this, "As senhas são diferentes", Toast.LENGTH_SHORT).show();
            return; // Sai da função
        }

        // Aqui é onde você pode salvar no banco de dados, Firebase, etc.
        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

        // Depois de cadastrar, manda pra tela de login
        Intent intent = new Intent(telacadastro.this, telalogin.class); // Troque MainActivity se for outro nome
        startActivity(intent); // Vai pra tela de login
        finish(); // Fecha a tela atual
    }
}
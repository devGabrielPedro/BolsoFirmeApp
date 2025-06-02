package com.example.telalogin;

import com.google.firebase.Timestamp;

public class Transacao {
    private String id;
    private String tipo; // "entrada" ou "saida"
    private double valor;
    private String descricao;
    private Timestamp data;

    public Transacao() {}

    public Transacao(String tipo, double valor, String descricao, Timestamp data) {
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.data = data;
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public double getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public Timestamp getData() { return data; }
    public void setId(String id) { this.id = id; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setValor(double valor) { this.valor = valor; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setData(Timestamp data) { this.data = data; }
}
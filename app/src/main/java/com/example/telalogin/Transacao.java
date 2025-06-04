package com.example.telalogin;

import com.google.firebase.Timestamp;

public class Transacao {
    private String id;
    private String tipo; // "entrada" ou "saida"
    private double valor;
    private String descricao;
    private Timestamp data;
    private String formaPagamento;

    private boolean parcelado;
    private int parcelaAtual;
    private int totalParcelas;
    private String idGrupoParcelamento; // Para agrupar todas as parcelas de uma compra

    private boolean recorrente;
    private String idGrupoRecorrencia; // Para agrupar despesas recorrentes

    private String categoria;

    public Transacao() {}

    public Transacao(String id, String tipo, double valor, String descricao, Timestamp data, String formaPagamento, boolean parcelado, int parcelaAtual, int totalParcelas, String idGrupoParcelamento, boolean recorrente, String idGrupoRecorrencia, String categoria) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.data = data;
        this.formaPagamento = formaPagamento;
        this.parcelado = parcelado;
        this.parcelaAtual = parcelaAtual;
        this.totalParcelas = totalParcelas;
        this.idGrupoParcelamento = idGrupoParcelamento;
        this.recorrente = recorrente;
        this.idGrupoRecorrencia = idGrupoRecorrencia;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public boolean isParcelado() {
        return parcelado;
    }

    public void setParcelado(boolean parcelado) {
        this.parcelado = parcelado;
    }

    public int getParcelaAtual() {
        return parcelaAtual;
    }

    public void setParcelaAtual(int parcelaAtual) {
        this.parcelaAtual = parcelaAtual;
    }

    public int getTotalParcelas() {
        return totalParcelas;
    }

    public void setTotalParcelas(int totalParcelas) {
        this.totalParcelas = totalParcelas;
    }

    public String getIdGrupoParcelamento() {
        return idGrupoParcelamento;
    }

    public void setIdGrupoParcelamento(String idGrupoParcelamento) {
        this.idGrupoParcelamento = idGrupoParcelamento;
    }

    public boolean isRecorrente() {
        return recorrente;
    }

    public void setRecorrente(boolean recorrente) {
        this.recorrente = recorrente;
    }

    public String getIdGrupoRecorrencia() {
        return idGrupoRecorrencia;
    }

    public void setIdGrupoRecorrencia(String idGrupoRecorrencia) {
        this.idGrupoRecorrencia = idGrupoRecorrencia;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
package com.example.telalogin; // Ou seu pacote principal

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransacaoAdapter extends RecyclerView.Adapter<TransacaoAdapter.TransacaoViewHolder> {

    private List<Transacao> transacoesList;
    private Context context; // Necessário para pegar cores de R.color
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

    public TransacaoAdapter(Context context, List<Transacao> transacoesList) {
        this.context = context;
        this.transacoesList = transacoesList;
    }

    @NonNull
    @Override
    public TransacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transacao, parent, false);
        return new TransacaoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransacaoViewHolder holder, int position) {
        Transacao transacao = transacoesList.get(position);

        holder.tvItemDescricao.setText(transacao.getDescricao());
        holder.tvItemValor.setText(String.format(Locale.getDefault(), "R$ %.2f", transacao.getValor()));

        if (transacao.getData() != null) {
            holder.tvItemData.setText(dateFormat.format(transacao.getData().toDate()));
        } else {
            holder.tvItemData.setText(""); // Ou alguma string padrão
        }

        // Mudar a cor do valor baseado no tipo (entrada/saída)
        if ("entrada".equalsIgnoreCase(transacao.getTipo())) {
            // Use ContextCompat para compatibilidade de cores
            holder.tvItemValor.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else if ("saida".equalsIgnoreCase(transacao.getTipo())) {
            holder.tvItemValor.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.tvItemValor.setTextColor(Color.BLACK); // Cor padrão
        }

        if ("saida".equalsIgnoreCase(transacao.getTipo()) &&
                transacao.getCategoria() != null &&
                !transacao.getCategoria().isEmpty()) {
            holder.tvItemCategoria.setText(transacao.getCategoria());
            holder.tvItemCategoria.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemCategoria.setVisibility(View.GONE);
        }

        if ("saida".equalsIgnoreCase(transacao.getTipo()) &&
                transacao.getFormaPagamento() != null &&
                !transacao.getFormaPagamento().isEmpty()) {
            holder.tvItemFormaPagamento.setText(transacao.getFormaPagamento());
            holder.tvItemFormaPagamento.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemFormaPagamento.setVisibility(View.GONE); // Esconde se não for saída ou não tiver forma de pgto
        }
    }

    @Override
    public int getItemCount() {
        return transacoesList.size();
    }

    // Método para atualizar a lista de transações no adapter
    public void atualizarLista(List<Transacao> novaLista) {
        this.transacoesList.clear();
        if (novaLista != null) {
            this.transacoesList.addAll(novaLista);
        }
        notifyDataSetChanged(); // Notifica o RecyclerView que os dados mudaram
    }

    static class TransacaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemDescricao, tvItemValor, tvItemData, tvItemFormaPagamento, tvItemCategoria;

        TransacaoViewHolder(View view) {
            super(view);
            tvItemDescricao = view.findViewById(R.id.tvItemDescricao);
            tvItemValor = view.findViewById(R.id.tvItemValor);
            tvItemData = view.findViewById(R.id.tvItemData);
            tvItemFormaPagamento = view.findViewById(R.id.tvItemFormaPagamento);
            tvItemCategoria = view.findViewById(R.id.tvItemCategoria);
        }
    }
}
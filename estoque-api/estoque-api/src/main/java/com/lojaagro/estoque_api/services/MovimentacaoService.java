package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.MovimentacaoRequest;
import com.lojaagro.estoque_api.dto.ComprovanteVenda;
import com.lojaagro.estoque_api.entities.OperacaoEstoque;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.repositories.OperacaoEstoqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class MovimentacaoService {
    private final OperacaoEstoqueRepository operacoes;
    private final FluxoCaixaService caixa;
    private final ProdutoService produtos;
    private final ClienteService clientes;

    public MovimentacaoService(OperacaoEstoqueRepository operacoes, FluxoCaixaService caixa,
                               ProdutoService produtos, ClienteService clientes) {
        this.operacoes = operacoes; this.caixa = caixa; this.produtos = produtos; this.clientes = clientes;
    }

    @Transactional
    public ComprovanteVenda executar(String chave, boolean venda, Long produtoId, MovimentacaoRequest dados, Usuario usuario) {
        UUID id;
        try {
            id = UUID.fromString(chave);
            if (!id.toString().equalsIgnoreCase(chave)) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Atualize o aplicativo: Idempotency-Key deve ser um UUID válido.");
        }
        Long lojaId = usuario.getLoja().getId();
        caixa.bloquearOperacoes(lojaId);
        java.math.BigDecimal preco = dados.preco() == null ? java.math.BigDecimal.ZERO : dados.preco();
        String assinatura = usuario.getId() + "|" + venda + "|" + produtoId + "|" + dados.quantidade()
                + "|" + preco.stripTrailingZeros().toPlainString() + "|" + dados.clienteId();
        var anterior = operacoes.findByIdAndLojaId(id, lojaId);
        if (anterior.isPresent()) {
            if (!anterior.get().getAssinatura().equals(assinatura)) {
                throw new IllegalArgumentException("Chave de operação já utilizada com outros dados.");
            }
            Transacao transacaoAnterior = anterior.get().getTransacao();
            return transacaoAnterior == null ? null : ComprovanteVenda.from(transacaoAnterior);
        }
        Transacao transacao;
        if (venda) {
            var cliente = clientes.entidadeOpcional(dados.clienteId(), usuario.getLoja());
            transacao = produtos.venderComLucro(produtoId, dados.quantidade(), dados.preco(), usuario, cliente);
        } else {
            if (dados.clienteId() != null) throw new IllegalArgumentException("Cliente só pode ser informado em vendas.");
            transacao = produtos.comprarComCusto(produtoId, dados.quantidade(), dados.preco(), usuario);
        }
        operacoes.save(new OperacaoEstoque(id, assinatura, usuario.getLoja(), transacao));
        return ComprovanteVenda.from(transacao);
    }
}

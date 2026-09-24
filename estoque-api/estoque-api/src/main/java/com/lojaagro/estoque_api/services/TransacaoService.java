package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.entities.Cliente;
import com.lojaagro.estoque_api.entities.FormaPagamento;
import com.lojaagro.estoque_api.entities.Venda;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final Clock businessClock;

    public TransacaoService(TransacaoRepository repository, Clock businessClock) {
        this.repository = repository;
        this.businessClock = businessClock;
    }

    @Transactional
    public Transacao registrarTransacao(Produto produto, Usuario usuario, String tipo, 
                                       int quantidade, BigDecimal precoUnitario,
                                       BigDecimal custoUnitario, BigDecimal lucro,
                                       String descricao, Cliente cliente) {
        BigDecimal valorTotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        return registrarTransacao(produto, usuario, tipo, quantidade, precoUnitario,
                valorTotal, custoUnitario, lucro, descricao, cliente, null, null);
    }

    @Transactional
    public Transacao registrarTransacao(Produto produto, Usuario usuario, String tipo,
                                       int quantidade, BigDecimal precoUnitario, BigDecimal valorTotal,
                                       BigDecimal custoUnitario, BigDecimal lucro, String descricao,
                                       Cliente cliente, Venda venda, FormaPagamento formaPagamento) {
        Transacao transacao = new Transacao();
        transacao.setProduto(produto);
        transacao.setLoja(usuario.getLoja());
        transacao.setUsuario(usuario);
        transacao.setTipo(tipo);
        transacao.setQuantidade(quantidade);
        transacao.setPrecoUnitario(precoUnitario);
        transacao.setValorTotal(valorTotal);
        transacao.setCustoUnitario(custoUnitario);
        transacao.setLucro(lucro);
        transacao.setData(LocalDateTime.now(businessClock));
        transacao.setDescricao(descricao);
        transacao.setCliente(cliente);
        transacao.setVenda(venda);
        transacao.setFormaPagamento(formaPagamento);
        
        return repository.save(transacao);
    }

    public List<Transacao> listarTodas(Long lojaId) {
        return repository.findByLojaIdOrderByDataDesc(lojaId);
    }

    public List<Transacao> listarUltimas10(Long lojaId) {
        return repository.findTop10ByLojaIdOrderByDataDesc(lojaId);
    }

    public List<Transacao> listarPorProduto(Long lojaId, Long produtoId) {
        return repository.findByLojaIdAndProdutoId(lojaId, produtoId);
    }

    public List<Transacao> listarPorTipo(Long lojaId, String tipo) {
        return repository.findByLojaIdAndTipo(lojaId, tipo);
    }
    
    public BigDecimal somarPorTipo(Long lojaId, String tipo) {
        BigDecimal soma = repository.sumValorTotalByTipo(lojaId, tipo);
        return soma != null ? soma : BigDecimal.ZERO;
    }

    public BigDecimal somarLucroReal(Long lojaId) {
        BigDecimal soma = repository.sumLucroVendas(lojaId);
        return soma != null ? soma : BigDecimal.ZERO;
    }
}

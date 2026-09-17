package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Transacao;
import com.lojaagro.estoque_api.entities.Usuario;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Transacao registrarTransacao(Produto produto, Usuario usuario, String tipo, 
                                       int quantidade, BigDecimal precoUnitario,
                                       BigDecimal custoUnitario, BigDecimal lucro,
                                       String descricao) {
        BigDecimal valorTotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        
        Transacao transacao = new Transacao();
        transacao.setProduto(produto);
        transacao.setUsuario(usuario);
        transacao.setTipo(tipo);
        transacao.setQuantidade(quantidade);
        transacao.setPrecoUnitario(precoUnitario);
        transacao.setValorTotal(valorTotal);
        transacao.setCustoUnitario(custoUnitario);
        transacao.setLucro(lucro);
        transacao.setData(LocalDateTime.now());
        transacao.setDescricao(descricao);
        
        return repository.save(transacao);
    }

    public List<Transacao> listarTodas() {
        return repository.findAll();
    }

    public List<Transacao> listarUltimas10() {
        return repository.findTop10ByOrderByDataDesc();
    }

    public List<Transacao> listarPorProduto(Long produtoId) {
        return repository.findByProdutoId(produtoId);
    }

    public List<Transacao> listarPorTipo(String tipo) {
        return repository.findByTipo(tipo);
    }
    
    public BigDecimal somarPorTipo(String tipo) {
        BigDecimal soma = repository.sumValorTotalByTipo(tipo);
        return soma != null ? soma : BigDecimal.ZERO;
    }

    public BigDecimal somarLucroReal() {
        BigDecimal soma = repository.sumLucroVendas();
        return soma != null ? soma : BigDecimal.ZERO;
    }
}

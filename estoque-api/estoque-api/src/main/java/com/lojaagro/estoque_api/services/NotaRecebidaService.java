package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.NotaRecebidaRequest;
import com.lojaagro.estoque_api.dto.NotaRecebidaResponse;
import com.lojaagro.estoque_api.entities.*;
import com.lojaagro.estoque_api.repositories.NotaRecebidaRepository;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotaRecebidaService {
    private final NotaRecebidaRepository notas;
    private final ProdutoRepository produtos;
    private final ProdutoService produtoService;
    private final Clock clock;

    public NotaRecebidaService(NotaRecebidaRepository notas, ProdutoRepository produtos,
                               ProdutoService produtoService, Clock clock) {
        this.notas = notas; this.produtos = produtos; this.produtoService = produtoService; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<NotaRecebidaResponse> listar(Loja loja) {
        exigirModulo(loja);
        return notas.findByLojaIdOrderByDataRecebimentoDescIdDesc(loja.getId()).stream()
                .map(this::resposta).toList();
    }

    @Transactional
    public NotaRecebidaResponse criar(NotaRecebidaRequest request, Usuario usuario) {
        Loja loja = usuario.getLoja();
        exigirModulo(loja);
        String documento = somenteDigitos(request.documentoFornecedor());
        if (documento != null && documento.length() != 11 && documento.length() != 14) {
            throw new IllegalArgumentException("CPF/CNPJ do fornecedor deve ter 11 ou 14 dígitos.");
        }
        String chave = somenteDigitos(request.chaveAcesso());
        if (chave != null && chave.length() != 44) {
            throw new IllegalArgumentException("Chave de acesso deve ter 44 dígitos.");
        }
        if (chave != null && notas.existsByLojaIdAndChaveAcesso(loja.getId(), chave)) {
            throw new IllegalArgumentException("Esta nota já foi cadastrada nesta loja.");
        }
        if (request.atualizarEstoque() && chave == null) {
            throw new IllegalArgumentException("Informe a chave de acesso para atualizar o estoque com segurança.");
        }
        List<NotaRecebidaRequest.Item> itens = request.itens() == null ? List.of() : request.itens();
        if (request.atualizarEstoque() && itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione os itens para atualizar o estoque pela nota.");
        }

        NotaRecebida nota = new NotaRecebida(loja, usuario, request.fornecedor(), documento,
                request.numero(), request.serie(), chave, request.dataEmissao(), request.dataRecebimento(),
                request.valorTotal(), request.conferida(), request.atualizarEstoque(), request.observacoes(),
                LocalDateTime.now(clock));
        for (NotaRecebidaRequest.Item item : itens) {
            Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(item.produtoId(), loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto da nota não encontrado nesta loja."));
            BigDecimal custo = loja.isFinanceiroAtivo() ? item.custoUnitario() : BigDecimal.ZERO;
            if (loja.isFinanceiroAtivo() && custo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Custo dos itens deve ser maior que zero.");
            }
            nota.adicionarItem(new NotaRecebidaItem(nota, produto, item.quantidade(), custo));
            if (request.atualizarEstoque()) {
                produtoService.comprarComCusto(produto.getId(), item.quantidade(), custo, usuario);
            }
        }
        return resposta(notas.save(nota));
    }

    @Transactional
    public NotaRecebidaResponse alterarConferencia(Long id, boolean conferida, Loja loja) {
        exigirModulo(loja);
        NotaRecebida nota = notas.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nota não encontrada nesta loja."));
        nota.setConferida(conferida);
        return resposta(nota);
    }

    private void exigirModulo(Loja loja) {
        if (!loja.isNotasFiscaisAtivas()) throw new AccessDeniedException("Módulo de notas não ativo para esta loja.");
    }

    private String somenteDigitos(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.replaceAll("\\D", "");
    }

    private NotaRecebidaResponse resposta(NotaRecebida nota) {
        return new NotaRecebidaResponse(nota.getId(), nota.getFornecedor(), nota.getDocumentoFornecedor(),
                nota.getNumero(), nota.getSerie(), nota.getChaveAcesso(), nota.getDataEmissao(),
                nota.getDataRecebimento(), nota.getValorTotal(), nota.isConferida(),
                nota.isEstoqueAtualizado(), nota.getObservacoes(), nota.getCriadaEm(),
                nota.getUsuario().getEmail(), nota.getItens().stream().map(item ->
                    new NotaRecebidaResponse.Item(item.getProduto().getId(), item.getProduto().getNome(),
                            item.getQuantidade(), item.getCustoUnitario())).toList());
    }
}

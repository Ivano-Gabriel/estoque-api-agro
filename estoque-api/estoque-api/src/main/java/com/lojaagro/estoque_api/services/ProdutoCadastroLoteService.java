package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.CadastroProdutosLoteRequest;
import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaErro;
import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaResultado;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProdutoCadastroLoteService {

    static final int MAXIMO_PRODUTOS = 200;

    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;
    private final Validator validator;

    public ProdutoCadastroLoteService(ProdutoService produtoService,
                                      ProdutoRepository produtoRepository,
                                      Validator validator) {
        this.produtoService = produtoService;
        this.produtoRepository = produtoRepository;
        this.validator = validator;
    }

    @Transactional
    public ImportacaoPlanilhaResultado cadastrar(CadastroProdutosLoteRequest lote, Loja loja) {
        List<ProdutoRequest> requisicoes = lote == null ? null : lote.produtos();
        if (requisicoes == null || requisicoes.isEmpty()) {
            return resultadoComErro(0, 0, "lote", "Adicione pelo menos um produto.");
        }
        if (requisicoes.size() > MAXIMO_PRODUTOS) {
            return resultadoComErro(
                    0, requisicoes.size(), "lote",
                    "Envie no máximo " + MAXIMO_PRODUTOS + " produtos por vez.");
        }

        List<ImportacaoPlanilhaErro> erros = new ArrayList<>();
        Set<String> chaves = new HashSet<>();
        produtoRepository.findByLojaId(loja.getId()).stream()
                .map(this::chaveProduto)
                .forEach(chaves::add);

        for (int indice = 0; indice < requisicoes.size(); indice++) {
            ProdutoRequest request = requisicoes.get(indice);
            int linha = indice + 1;
            if (request == null) {
                erros.add(new ImportacaoPlanilhaErro(linha, "produto", "Linha vazia ou inválida."));
                continue;
            }

            int errosAntes = erros.size();
            for (ConstraintViolation<ProdutoRequest> violacao : validator.validate(request)) {
                String campo = violacao.getPropertyPath().toString();
                erros.add(new ImportacaoPlanilhaErro(linha, campo, violacao.getMessage()));
            }
            validarFinanceiro(request, loja, linha, erros);
            validarImagem(request, loja, linha, erros);

            if (erros.size() == errosAntes) {
                String chave = chaveProduto(request.nome(), request.categoria().nome());
                if (!chaves.add(chave)) {
                    erros.add(new ImportacaoPlanilhaErro(
                            linha,
                            "nome",
                            "Produto duplicado no lote, no estoque ou na lixeira. Restaure o cadastro existente."));
                }
            }
        }

        if (!erros.isEmpty()) {
            return new ImportacaoPlanilhaResultado(0, requisicoes.size(), List.copyOf(erros));
        }

        requisicoes.forEach(request -> produtoService.criar(request, loja));
        return new ImportacaoPlanilhaResultado(requisicoes.size(), requisicoes.size(), List.of());
    }

    private void validarFinanceiro(ProdutoRequest request, Loja loja, int linha,
                                   List<ImportacaoPlanilhaErro> erros) {
        if (!loja.isFinanceiroAtivo()) return;
        if (!positivo(request.preco())) {
            erros.add(new ImportacaoPlanilhaErro(
                    linha, "preco", "O preço de venda deve ser maior que zero."));
        }
        if (request.quantidadeEstoque() > 0 && !positivo(request.custoUnitario())) {
            erros.add(new ImportacaoPlanilhaErro(
                    linha, "custoUnitario",
                    "Informe quanto foi pago por unidade quando existe estoque inicial."));
        }
    }

    private void validarImagem(ProdutoRequest request, Loja loja, int linha,
                               List<ImportacaoPlanilhaErro> erros) {
        if (request.imagemUrl() != null && !request.imagemUrl().isBlank() && !loja.isFotosAtivas()) {
            erros.add(new ImportacaoPlanilhaErro(
                    linha, "imagemUrl", "O módulo de fotos não está ativo para esta loja."));
        }
    }

    private boolean positivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    private String chaveProduto(Produto produto) {
        return chaveProduto(produto.getNome(), produto.getCategoria().getNome());
    }

    private String chaveProduto(String nome, String categoria) {
        return normalizar(nome) + "|" + normalizar(categoria);
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    private ImportacaoPlanilhaResultado resultadoComErro(
            int totalImportado, int totalLinhas, String campo, String mensagem) {
        return new ImportacaoPlanilhaResultado(
                totalImportado,
                totalLinhas,
                List.of(new ImportacaoPlanilhaErro(0, campo, mensagem)));
    }
}

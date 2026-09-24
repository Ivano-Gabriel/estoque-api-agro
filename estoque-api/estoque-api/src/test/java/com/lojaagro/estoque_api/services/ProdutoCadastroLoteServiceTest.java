package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.CadastroProdutosLoteRequest;
import com.lojaagro.estoque_api.dto.CategoriaRequest;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.entities.Categoria;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoCadastroLoteServiceTest {

    @Test
    void deveCadastrarTodoLoteValido() {
        ProdutoService produtos = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        Loja loja = loja(true, true);
        when(repository.findByLojaId(1L)).thenReturn(List.of());
        ProdutoCadastroLoteService service = service(produtos, repository);

        var resultado = service.cadastrar(new CadastroProdutosLoteRequest(List.of(
                request("Camisa azul", "Roupas"),
                request("Calça preta", "Roupas")
        )), loja);

        assertEquals(2, resultado.totalImportado());
        assertTrue(resultado.erros().isEmpty());
        verify(produtos, times(2)).criar(any(ProdutoRequest.class), eq(loja));
    }

    @Test
    void deveRejeitarLoteInteiroEIndicarLinhaDoDuplicado() {
        ProdutoService produtos = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        Loja loja = loja(true, false);
        when(repository.findByLojaId(1L)).thenReturn(List.of());
        ProdutoCadastroLoteService service = service(produtos, repository);

        var resultado = service.cadastrar(new CadastroProdutosLoteRequest(List.of(
                request("Ração Premium", "Rações"),
                request("racao premium", "racoes")
        )), loja);

        assertEquals(0, resultado.totalImportado());
        assertTrue(resultado.erros().stream().anyMatch(erro -> erro.linha() == 2));
        verify(produtos, never()).criar(any(), any());
    }

    @Test
    void deveDetectarProdutoJaExistenteInclusiveNaLixeira() {
        ProdutoService produtos = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        Loja loja = loja(true, false);
        Categoria categoria = mock(Categoria.class);
        Produto existente = mock(Produto.class);
        when(categoria.getNome()).thenReturn("Bebidas");
        when(existente.getNome()).thenReturn("Água mineral");
        when(existente.getCategoria()).thenReturn(categoria);
        when(repository.findByLojaId(1L)).thenReturn(List.of(existente));
        ProdutoCadastroLoteService service = service(produtos, repository);

        var resultado = service.cadastrar(new CadastroProdutosLoteRequest(List.of(
                request("Agua mineral", "Bebidas")
        )), loja);

        assertEquals(0, resultado.totalImportado());
        assertTrue(resultado.erros().stream().anyMatch(erro -> erro.campo().equals("nome")));
        verify(produtos, never()).criar(any(), any());
    }

    @Test
    void deveLimitarQuantidadeEValidarValoresAntesDeGravar() {
        ProdutoService produtos = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        Loja loja = loja(true, false);
        when(repository.findByLojaId(1L)).thenReturn(List.of());
        ProdutoCadastroLoteService service = service(produtos, repository);

        ProdutoRequest invalido = new ProdutoRequest(
                "Produto", "UNIDADE", BigDecimal.ZERO, BigDecimal.ZERO,
                null, 5, new CategoriaRequest("Geral"), null, null);
        var resultadoInvalido = service.cadastrar(
                new CadastroProdutosLoteRequest(List.of(invalido)), loja);
        assertTrue(resultadoInvalido.erros().stream()
                .anyMatch(erro -> erro.campo().equals("preco")));
        assertTrue(resultadoInvalido.erros().stream()
                .anyMatch(erro -> erro.campo().equals("custoUnitario")));

        List<ProdutoRequest> excesso = new ArrayList<>();
        for (int indice = 0; indice <= ProdutoCadastroLoteService.MAXIMO_PRODUTOS; indice++) {
            excesso.add(request("Produto " + indice, "Geral"));
        }
        var resultadoExcesso = service.cadastrar(new CadastroProdutosLoteRequest(excesso), loja);
        assertTrue(resultadoExcesso.erros().stream()
                .anyMatch(erro -> erro.mensagem().contains("no máximo 200")));
        verify(produtos, never()).criar(any(), any());
    }

    private ProdutoCadastroLoteService service(ProdutoService produtos, ProdutoRepository repository) {
        return new ProdutoCadastroLoteService(
                produtos,
                repository,
                Validation.buildDefaultValidatorFactory().getValidator());
    }

    private Loja loja(boolean financeiro, boolean fotos) {
        Loja loja = mock(Loja.class);
        when(loja.getId()).thenReturn(1L);
        when(loja.isFinanceiroAtivo()).thenReturn(financeiro);
        when(loja.isFotosAtivas()).thenReturn(fotos);
        return loja;
    }

    private ProdutoRequest request(String nome, String categoria) {
        return new ProdutoRequest(
                nome, "UNIDADE", new BigDecimal("20.00"), new BigDecimal("10.00"),
                null, 5, new CategoriaRequest(categoria), null, null);
    }
}

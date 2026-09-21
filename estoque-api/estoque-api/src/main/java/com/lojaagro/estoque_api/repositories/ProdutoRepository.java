package com.lojaagro.estoque_api.repositories; 

import com.lojaagro.estoque_api.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();
    java.util.Optional<Produto> findByIdAndAtivoTrue(Long id);

    @Query("SELECT COUNT(p) FROM Produto p WHERE LOWER(TRIM(p.nome)) = LOWER(TRIM(:nome)) "
            + "AND LOWER(TRIM(p.categoria.nome)) = LOWER(TRIM(:categoria)) "
            + "AND (:ignorarId IS NULL OR p.id <> :ignorarId)")
    long contarDuplicados(String nome, String categoria, Long ignorarId);

    // Busca apenas os inativos (Lixeira) ignorando o filtro padrão
    @Query(value = "SELECT * FROM produto WHERE ativo = false", nativeQuery = true)
    List<Produto> buscarLixeira();

    // Método para restaurar o produto
    @Modifying
    @Transactional
    @Query(value = "UPDATE produto SET ativo = true WHERE id = ?", nativeQuery = true)
    void restaurarProduto(Long id);

    /*
     * Ferramentas destrutivas preservadas para testes.
     * Só são chamadas pelo controller do profile "ferramentas-teste",
     * que não existe no ambiente normal de produção.
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM transacao WHERE produto_id = ?", nativeQuery = true)
    void apagarTransacoesDoProduto(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM produto WHERE id = ?", nativeQuery = true)
    void apagarPermanente(Long id);

    // 1. Conta quantos produtos estão ativos
    long countByAtivoTrue();

    // 2. Conta quantos produtos estão com 5 ou menos no estoque
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= 5")
    long contarEstoqueCritico();

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= 5 "
            + "ORDER BY p.quantidadeEstoque ASC, p.nome ASC")
    List<Produto> buscarEstoqueCritico();

    // 3. Calcula o patrimônio (Preço x Quantidade de todos os ativos)
    // O COALESCE garante que, se a loja estiver vazia, ele retorne 0 em vez de dar erro null
    @Query("SELECT COALESCE(SUM(p.quantidadeEstoque * p.preco), 0) FROM Produto p WHERE p.ativo = true")
    BigDecimal calcularPatrimonioTotal();
}

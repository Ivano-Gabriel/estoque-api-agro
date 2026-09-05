package com.lojaagro.estoque_api.repositories; 

import com.lojaagro.estoque_api.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca apenas os inativos (Lixeira) ignorando o filtro padrão
    @Query(value = "SELECT * FROM produto WHERE ativo = false", nativeQuery = true)
    List<Produto> buscarLixeira();

    // Método para restaurar o produto
    @Modifying
    @Transactional
    @Query(value = "UPDATE produto SET ativo = true WHERE id = ?", nativeQuery = true)
    void restaurarProduto(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM transacao WHERE produto_id = ?", nativeQuery = true)
    void apagarTransacoesDoProduto(Long id);
    
    // Método para apagar de vez (agora dentro da interface)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM produto WHERE id = ?", nativeQuery = true)
    void apagarPermanente(Long id);
    // 1. Conta quantos produtos estão ativos
    long countByAtivoTrue();

    // 2. Conta quantos produtos estão com 5 ou menos no estoque
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= 5")
    long contarEstoqueCritico();

    // 3. Calcula o patrimônio (Preço x Quantidade de todos os ativos)
    // O COALESCE garante que, se a loja estiver vazia, ele retorne 0 em vez de dar erro null
    @Query("SELECT COALESCE(SUM(p.quantidadeEstoque * p.preco), 0) FROM Produto p WHERE p.ativo = true")
    double calcularPatrimonioTotal();
}


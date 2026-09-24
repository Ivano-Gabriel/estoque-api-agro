package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.ClienteRequest;
import com.lojaagro.estoque_api.dto.ClienteResponse;
import com.lojaagro.estoque_api.entities.Cliente;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.repositories.ClienteRepository;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import com.lojaagro.estoque_api.repositories.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final TransacaoRepository transacoes;
    private final Clock clock;

    public ClienteService(ClienteRepository clientes, ProdutoRepository produtos,
                          TransacaoRepository transacoes, Clock clock) {
        this.clientes = clientes;
        this.produtos = produtos;
        this.transacoes = transacoes;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Loja loja) {
        return clientes.findByLojaIdAndAtivoTrueOrderByNomeAsc(loja.getId()).stream()
                .map(cliente -> resposta(cliente, false)).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, Loja loja) {
        return resposta(entidade(id, loja), true);
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request, Loja loja) {
        Cliente cliente = new Cliente(loja, request.nome(), request.telefone(), request.email(),
                request.observacoes(), LocalDateTime.now(clock));
        cliente.definirFavoritos(favoritos(request.produtoFavoritoIds(), loja));
        return resposta(clientes.save(cliente), true);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request, Loja loja) {
        Cliente cliente = entidade(id, loja);
        cliente.atualizar(request.nome(), request.telefone(), request.email(), request.observacoes());
        cliente.definirFavoritos(favoritos(request.produtoFavoritoIds(), loja));
        return resposta(cliente, true);
    }

    @Transactional
    public void arquivar(Long id, Loja loja) {
        Cliente cliente = entidade(id, loja);
        cliente.setAtivo(false);
    }

    @Transactional(readOnly = true)
    public Cliente entidadeOpcional(Long id, Loja loja) {
        if (id == null) return null;
        return entidade(id, loja);
    }

    private Cliente entidade(Long id, Loja loja) {
        return clientes.findByIdAndLojaIdAndAtivoTrue(id, loja.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado nesta loja."));
    }

    private Set<Produto> favoritos(Set<Long> ids, Loja loja) {
        if (ids == null || ids.isEmpty()) return Set.of();
        Set<Produto> resultado = new LinkedHashSet<>();
        for (Long id : ids) {
            Produto produto = produtos.findByIdAndLojaIdAndAtivoTrue(id, loja.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto favorito não encontrado nesta loja."));
            resultado.add(produto);
        }
        return resultado;
    }

    private ClienteResponse resposta(Cliente cliente, boolean incluirHistorico) {
        List<ClienteResponse.ProdutoResumo> favoritos = cliente.getProdutosFavoritos().stream()
                .map(produto -> new ClienteResponse.ProdutoResumo(
                        produto.getId(), produto.getNome(), produto.getCategoria().getNome()))
                .toList();
        List<ClienteResponse.ProdutoComprado> maisComprados = incluirHistorico
                ? transacoes.buscarMaisCompradosCliente(cliente.getLoja().getId(), cliente.getId()).stream()
                    .limit(5)
                    .map(linha -> new ClienteResponse.ProdutoComprado(
                            (Long) linha[0], (String) linha[1], ((Number) linha[2]).longValue()))
                    .toList()
                : List.of();
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone(),
                cliente.getEmail(), cliente.getObservacoes(), cliente.getCriadoEm(), favoritos, maisComprados);
    }
}

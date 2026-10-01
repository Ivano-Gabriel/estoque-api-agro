package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.*;
import com.lojaagro.estoque_api.entities.*;
import com.lojaagro.estoque_api.repositories.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CaixaOperacionalService {
    private final CaixaSessaoRepository sessoes;
    private final MovimentoCaixaRepository movimentos;
    private final AuditoriaService auditoria;
    private final Clock clock;
    public CaixaOperacionalService(CaixaSessaoRepository sessoes, MovimentoCaixaRepository movimentos,
                                   AuditoriaService auditoria, Clock clock) {
        this.sessoes=sessoes; this.movimentos=movimentos; this.auditoria=auditoria; this.clock=clock;
    }

    @Transactional
    public CaixaSessaoResponse abrir(Usuario usuario, BigDecimal saldoInicial) {
        exigirModulo(usuario.getLoja());
        if (sessoes.findByLojaIdAndOperadorIdAndStatus(usuario.getLoja().getId(), usuario.getId(), StatusCaixa.ABERTO).isPresent())
            throw new IllegalArgumentException("Você já possui um caixa aberto.");
        LocalDateTime agora=LocalDateTime.now(clock);
        CaixaSessao sessao=sessoes.save(new CaixaSessao(usuario.getLoja(),usuario,saldoInicial,agora));
        movimentos.save(new MovimentoCaixa(sessao,usuario,null,TipoMovimentoCaixa.ABERTURA,
                saldoInicial==null?BigDecimal.ZERO:saldoInicial,"Abertura do caixa",agora));
        auditoria.registrar(usuario,"ABRIR","CAIXA",sessao.getId(),"Saldo inicial: "+sessao.getSaldoInicial());
        return CaixaSessaoResponse.de(sessao);
    }

    @Transactional(readOnly=true)
    public Optional<CaixaSessaoResponse> atual(Usuario usuario) {
        if (!usuario.getLoja().isCaixaOperacionalAtivo()) return Optional.empty();
        return sessoes.findByLojaIdAndOperadorIdAndStatus(usuario.getLoja().getId(),usuario.getId(),StatusCaixa.ABERTO)
                .map(CaixaSessaoResponse::de);
    }

    @Transactional
    public CaixaSessaoResponse movimentar(Usuario usuario, TipoMovimentoCaixa tipo, BigDecimal valor, String descricao) {
        if (tipo!=TipoMovimentoCaixa.SUPRIMENTO && tipo!=TipoMovimentoCaixa.SANGRIA)
            throw new IllegalArgumentException("Movimento manual inválido.");
        CaixaSessao sessao=abertoBloqueado(usuario);
        if(tipo==TipoMovimentoCaixa.SUPRIMENTO) sessao.registrarSuprimento(valor); else sessao.registrarSangria(valor);
        movimentos.save(new MovimentoCaixa(sessao,usuario,null,tipo,valor,limitar(descricao),LocalDateTime.now(clock)));
        auditoria.registrar(usuario,tipo.name(),"CAIXA",sessao.getId(),"Valor: "+valor+" • "+limitar(descricao));
        return CaixaSessaoResponse.de(sessao);
    }

    @Transactional
    public CaixaSessaoResponse fechar(Usuario usuario, BigDecimal saldoInformado, String observacoes) {
        CaixaSessao sessao=abertoBloqueado(usuario);
        sessao.fechar(saldoInformado,observacoes,LocalDateTime.now(clock));
        auditoria.registrar(usuario,"FECHAR","CAIXA",sessao.getId(),
                "Esperado: "+sessao.getSaldoEsperado()+" • Informado: "+sessao.getSaldoInformado()+" • Diferença: "+sessao.getDiferenca());
        return CaixaSessaoResponse.de(sessao);
    }

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void registrarVenda(Usuario usuario, Venda venda, BigDecimal total, BigDecimal dinheiro) {
        if (!usuario.getLoja().isCaixaOperacionalAtivo()) return;
        CaixaSessao sessao=abertoBloqueado(usuario); sessao.registrarVenda(total,dinheiro);
        movimentos.save(new MovimentoCaixa(sessao,usuario,venda,TipoMovimentoCaixa.VENDA,total,
                "Venda "+venda.getId()+" • dinheiro "+dinheiro,LocalDateTime.now(clock)));
    }

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void registrarEstorno(Usuario usuario, Venda venda, BigDecimal total, BigDecimal dinheiro) {
        if (!usuario.getLoja().isCaixaOperacionalAtivo()) return;
        CaixaSessao sessao=abertoBloqueado(usuario); sessao.registrarEstorno(total,dinheiro);
        movimentos.save(new MovimentoCaixa(sessao,usuario,venda,TipoMovimentoCaixa.ESTORNO,total,
                "Estorno da venda "+venda.getId()+" • dinheiro "+dinheiro,LocalDateTime.now(clock)));
    }

    @Transactional(readOnly=true)
    public Page<CaixaSessaoResponse> historico(Loja loja,int pagina,int tamanho) {
        exigirModulo(loja);
        return sessoes.findByLojaId(loja.getId(),PageRequest.of(Math.max(0,pagina),Math.min(100,Math.max(1,tamanho)),
                Sort.by(Sort.Direction.DESC,"abertaEm"))).map(CaixaSessaoResponse::de);
    }
    @Transactional(readOnly=true)
    public List<MovimentoCaixaResponse> movimentos(Usuario usuario,UUID sessaoId) {
        CaixaSessao sessao=sessoes.findById(sessaoId).filter(c->c.getLoja().getId().equals(usuario.getLoja().getId()))
                .orElseThrow(()->new IllegalArgumentException("Caixa não encontrado nesta loja."));
        if(usuario.getRole()!=UsuarioRole.ADMIN && !sessao.getOperador().getId().equals(usuario.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Caixa de outro operador.");
        return movimentos.findBySessaoIdOrderByCriadoEmAsc(sessaoId).stream().map(MovimentoCaixaResponse::de).toList();
    }
    private CaixaSessao abertoBloqueado(Usuario u){exigirModulo(u.getLoja()); return sessoes.bloquearAberto(u.getLoja().getId(),u.getId(),StatusCaixa.ABERTO)
            .orElseThrow(()->new IllegalArgumentException("Abra seu caixa antes de registrar esta operação."));}
    private void exigirModulo(Loja l){if(!l.isFinanceiroAtivo()||!l.isCaixaOperacionalAtivo()) throw new IllegalArgumentException("Caixa operacional não está ativo para esta loja.");}
    private String limitar(String v){if(v==null||v.isBlank())return null; String s=v.trim(); return s.length()>300?s.substring(0,300):s;}
}

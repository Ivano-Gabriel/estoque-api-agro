package com.lojaagro.estoque_api.controllers;

import com.lojaagro.estoque_api.dto.*;
import com.lojaagro.estoque_api.entities.TipoMovimentoCaixa;
import com.lojaagro.estoque_api.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/caixas")
public class CaixaOperacionalController {
    private final CaixaOperacionalService caixas; private final UsuarioService usuarios;
    public CaixaOperacionalController(CaixaOperacionalService caixas,UsuarioService usuarios){this.caixas=caixas;this.usuarios=usuarios;}
    public record Abertura(@DecimalMin("0.00") BigDecimal saldoInicial){}
    public record Movimento(@NotNull TipoMovimentoCaixa tipo,@NotNull @DecimalMin("0.01") BigDecimal valor,@Size(max=300) String descricao){}
    public record Fechamento(@NotNull @DecimalMin("0.00") BigDecimal saldoInformado,@Size(max=500) String observacoes){}
    @PostMapping("/abertura") public CaixaSessaoResponse abrir(@Valid @RequestBody Abertura r,Authentication a){return caixas.abrir(usuarios.atual(a),r.saldoInicial());}
    @GetMapping("/atual") public Optional<CaixaSessaoResponse> atual(Authentication a){return caixas.atual(usuarios.atual(a));}
    @PostMapping("/movimentos") @PreAuthorize("hasRole('ADMIN')") public CaixaSessaoResponse movimento(@Valid @RequestBody Movimento r,Authentication a){return caixas.movimentar(usuarios.atual(a),r.tipo(),r.valor(),r.descricao());}
    @PostMapping("/fechamento") public CaixaSessaoResponse fechar(@Valid @RequestBody Fechamento r,Authentication a){return caixas.fechar(usuarios.atual(a),r.saldoInformado(),r.observacoes());}
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public Page<CaixaSessaoResponse> historico(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho,Authentication a){return caixas.historico(usuarios.lojaAtual(a),pagina,tamanho);}
    @GetMapping("/{id}/movimentos") public List<MovimentoCaixaResponse> movimentos(@PathVariable UUID id,Authentication a){return caixas.movimentos(usuarios.atual(a),id);}
}

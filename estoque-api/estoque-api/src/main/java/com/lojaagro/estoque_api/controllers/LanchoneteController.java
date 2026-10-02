package com.lojaagro.estoque_api.controllers;
import com.lojaagro.estoque_api.dto.LanchoneteDtos.*;import com.lojaagro.estoque_api.services.*;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController@RequestMapping("/lanchonete")
public class LanchoneteController{
 private final LanchoneteService service;private final UsuarioService usuarios;public LanchoneteController(LanchoneteService service,UsuarioService usuarios){this.service=service;this.usuarios=usuarios;}
 @GetMapping("/configuracao")public Configuracao configuracao(Authentication a){return service.configuracao(usuarios.lojaAtual(a));}
 @GetMapping("/cardapio")public List<ItemCardapioResponse>cardapio(Authentication a){return service.cardapio(usuarios.lojaAtual(a));}
 @PostMapping("/cardapio")@ResponseStatus(HttpStatus.CREATED)public ItemCardapioResponse criarItem(@Valid@RequestBody ItemCardapioRequest r,Authentication a){return service.salvarItem(null,r,usuarios.atual(a));}
 @PutMapping("/cardapio/{id}")public ItemCardapioResponse atualizarItem(@PathVariable Long id,@Valid@RequestBody ItemCardapioRequest r,Authentication a){return service.salvarItem(id,r,usuarios.atual(a));}
 @PostMapping("/grupos")@ResponseStatus(HttpStatus.CREATED)public Grupo criarGrupo(@Valid@RequestBody GrupoRequest r,Authentication a){return service.salvarGrupo(null,r,usuarios.atual(a));}
 @PutMapping("/grupos/{id}")public Grupo atualizarGrupo(@PathVariable Long id,@Valid@RequestBody GrupoRequest r,Authentication a){return service.salvarGrupo(id,r,usuarios.atual(a));}
 @PostMapping("/mesas")@ResponseStatus(HttpStatus.CREATED)public Mesa criarMesa(@Valid@RequestBody MesaRequest r,Authentication a){return service.salvarMesa(null,r,usuarios.atual(a));}
 @PutMapping("/mesas/{id}")public Mesa atualizarMesa(@PathVariable Long id,@Valid@RequestBody MesaRequest r,Authentication a){return service.salvarMesa(id,r,usuarios.atual(a));}
 @PostMapping("/pedidos")@ResponseStatus(HttpStatus.CREATED)public PedidoResponse criarPedido(@RequestHeader(value="Idempotency-Key",required=false)String chave,@Valid@RequestBody PedidoRequest r,Authentication a){if(chave==null||chave.isBlank())throw new IllegalArgumentException("Idempotency-Key é obrigatório.");UUID id;try{id=UUID.fromString(chave);}catch(IllegalArgumentException e){throw new IllegalArgumentException("Idempotency-Key inválido.");}return service.criar(id,r,usuarios.atual(a));}
 @GetMapping("/pedidos")public List<PedidoResponse>fila(@RequestParam(defaultValue="false")boolean historico,Authentication a){return service.fila(usuarios.lojaAtual(a),historico);}
 @PutMapping("/pedidos/{id}/status")public PedidoResponse status(@PathVariable UUID id,@Valid@RequestBody StatusRequest r,Authentication a){return service.status(id,r.status(),usuarios.atual(a));}
 @PostMapping("/pedidos/{id}/pagamento")public PedidoResponse pagar(@PathVariable UUID id,@Valid@RequestBody PagamentoRequest r,Authentication a){return service.pagar(id,r,usuarios.atual(a));}
 @PutMapping("/pedidos/{id}/cancelamento")public PedidoResponse cancelar(@PathVariable UUID id,@Valid@RequestBody CancelamentoRequest r,Authentication a){return service.cancelar(id,r.motivo(),usuarios.atual(a));}
}

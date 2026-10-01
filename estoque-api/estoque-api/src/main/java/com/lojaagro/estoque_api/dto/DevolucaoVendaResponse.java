package com.lojaagro.estoque_api.dto;
import com.lojaagro.estoque_api.entities.*; import java.math.*; import java.time.*; import java.util.*;
public record DevolucaoVendaResponse(UUID id,UUID vendaId,FormaPagamento formaReembolso,BigDecimal valor,String motivo,LocalDateTime criadaEm,List<Item> itens){
 public record Item(Long vendaItemId,String produto,int quantidade,BigDecimal valor){}
 public static DevolucaoVendaResponse de(DevolucaoVenda d){return new DevolucaoVendaResponse(d.getId(),d.getVenda().getId(),d.getFormaReembolso(),d.getValor(),d.getMotivo(),d.getCriadaEm(),d.getItens().stream().map(i->new Item(i.getVendaItem().getId(),i.getVendaItem().getNomeProduto(),i.getQuantidade(),i.getValor())).toList());}
}

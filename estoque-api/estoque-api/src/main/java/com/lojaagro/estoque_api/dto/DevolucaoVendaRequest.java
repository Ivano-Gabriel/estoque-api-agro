package com.lojaagro.estoque_api.dto;
import com.lojaagro.estoque_api.entities.FormaPagamento; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.*;
public record DevolucaoVendaRequest(@NotBlank @Size(max=300) String motivo,@NotNull FormaPagamento formaReembolso,
 @NotEmpty @Size(max=100) List<@Valid Item> itens){
 public record Item(@NotNull Long vendaItemId,@Min(1) int quantidade){}
}

package com.lojaagro.estoque_api.dto;
import org.springframework.data.domain.Page; import java.util.*;
public record PaginaResponse<T>(List<T> itens,int pagina,int tamanho,long total,int totalPaginas,boolean ultima){
 public static <T> PaginaResponse<T> de(Page<T> p){return new PaginaResponse<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.isLast());}
}

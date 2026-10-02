package com.lojaagro.estoque_api.entities;
public enum StatusPedido {
    RECEBIDO, EM_PREPARO, PRONTO, SAIU_PARA_ENTREGA, FINALIZADO, CANCELADO;
    public boolean encerrado(){ return this==FINALIZADO||this==CANCELADO; }
}

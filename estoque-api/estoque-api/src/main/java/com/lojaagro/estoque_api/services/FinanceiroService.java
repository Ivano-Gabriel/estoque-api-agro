package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.entities.Loja;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class FinanceiroService {
    public void exigirAtivo(Loja loja) {
        if (loja == null || !loja.isFinanceiroAtivo()) {
            throw new AccessDeniedException("Módulo financeiro não contratado por esta loja.");
        }
    }
}

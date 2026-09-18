package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaResultado;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoImportacaoServiceTest {

    @Test
    void deveImportarModeloValido() {
        ProdutoService produtoService = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        ProdutoImportacaoService service = new ProdutoImportacaoService(produtoService, repository);

        byte[] modelo = service.gerarModelo();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "produtos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", modelo);

        ImportacaoPlanilhaResultado resultado = service.importar(arquivo);

        assertEquals(1, resultado.totalImportado());
        assertTrue(resultado.erros().isEmpty());

        ArgumentCaptor<ProdutoRequest> captor = ArgumentCaptor.forClass(ProdutoRequest.class);
        verify(produtoService).criar(captor.capture());
        assertEquals("Ração Premium 15kg", captor.getValue().nome());
        assertEquals(10, captor.getValue().quantidadeEstoque());
        assertEquals("100.00", captor.getValue().custoUnitario().toPlainString());
    }

    @Test
    void naoDeveGravarNenhumaLinhaQuandoUmaLinhaForInvalida() throws Exception {
        ProdutoService produtoService = mock(ProdutoService.class);
        ProdutoRepository repository = mock(ProdutoRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        ProdutoImportacaoService service = new ProdutoImportacaoService(produtoService, repository);

        byte[] planilha;
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("PRODUTOS");
            Row cabecalho = sheet.createRow(0);
            String[] colunas = {
                    "nome", "tipo", "preco_venda", "custo_unitario",
                    "quantidade", "categoria", "data_validade"
            };
            for (int indice = 0; indice < colunas.length; indice++) {
                cabecalho.createCell(indice).setCellValue(colunas[indice]);
            }
            Row linha = sheet.createRow(1);
            linha.createCell(0).setCellValue("Produto inválido");
            linha.createCell(1).setCellValue("UNIDADE");
            linha.createCell(2).setCellValue(20.00);
            linha.createCell(4).setCellValue(5);
            linha.createCell(5).setCellValue("Teste");
            workbook.write(output);
            planilha = output.toByteArray();
        }

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "produtos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", planilha);

        ImportacaoPlanilhaResultado resultado = service.importar(arquivo);

        assertEquals(0, resultado.totalImportado());
        assertTrue(resultado.erros().stream()
                .anyMatch(erro -> erro.campo().equals("custo_unitario")));
        verify(produtoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }
}

package com.lojaagro.estoque_api.services;

import com.lojaagro.estoque_api.dto.CategoriaRequest;
import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaErro;
import com.lojaagro.estoque_api.dto.ImportacaoPlanilhaResultado;
import com.lojaagro.estoque_api.dto.ProdutoRequest;
import com.lojaagro.estoque_api.entities.Produto;
import com.lojaagro.estoque_api.entities.Loja;
import com.lojaagro.estoque_api.repositories.ProdutoRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ProdutoImportacaoService {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private static final int MAXIMO_PRODUTOS = 1_000;
    private static final int MAXIMO_LINHAS_FISICAS = 10_000;
    private static final DataFormatter FORMATADOR = new DataFormatter(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;

    public ProdutoImportacaoService(ProdutoService produtoService,
                                    ProdutoRepository produtoRepository) {
        this.produtoService = produtoService;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ImportacaoPlanilhaResultado importar(MultipartFile arquivo, Loja loja) {
        validarArquivo(arquivo);
        LeituraPlanilha leitura = lerPlanilha(arquivo, loja);

        if (!leitura.erros().isEmpty()) {
            return new ImportacaoPlanilhaResultado(0, leitura.totalLinhas(), leitura.erros());
        }

        leitura.produtos().forEach(produto -> produtoService.criar(produto, loja));
        return new ImportacaoPlanilhaResultado(
                leitura.produtos().size(), leitura.totalLinhas(), List.of());
    }

    public byte[] gerarModelo() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet produtos = workbook.createSheet("PRODUTOS");
            String[] cabecalhos = {
                    "Nome do produto", "Unidade de medida",
                    "Preço de venda (quanto cobrar)", "Preço de compra (quanto pagou)",
                    "Quantidade inicial", "Categoria", "Data de validade"
            };

            Font fonteCabecalho = workbook.createFont();
            fonteCabecalho.setBold(true);
            fonteCabecalho.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloCabecalho = workbook.createCellStyle();
            estiloCabecalho.setFont(fonteCabecalho);
            estiloCabecalho.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row cabecalho = produtos.createRow(0);
            for (int coluna = 0; coluna < cabecalhos.length; coluna++) {
                Cell cell = cabecalho.createCell(coluna);
                cell.setCellValue(cabecalhos[coluna]);
                cell.setCellStyle(estiloCabecalho);
            }

            Row exemplo = produtos.createRow(1);
            exemplo.createCell(0).setCellValue("Ração Premium 15kg");
            exemplo.createCell(1).setCellValue("PACOTE");
            exemplo.createCell(2).setCellValue(149.90);
            exemplo.createCell(3).setCellValue(100.00);
            exemplo.createCell(4).setCellValue(10);
            exemplo.createCell(5).setCellValue("Rações");
            exemplo.createCell(6).setCellValue("31/12/2027");

            int[] larguras = {32, 16, 18, 18, 14, 22, 18};
            for (int coluna = 0; coluna < larguras.length; coluna++) {
                produtos.setColumnWidth(coluna, larguras[coluna] * 256);
            }
            produtos.createFreezePane(0, 1);
            produtos.setAutoFilter(new CellRangeAddress(0, 1, 0, cabecalhos.length - 1));

            Sheet instrucoes = workbook.createSheet("LEIA-ME");
            String[] textos = {
                    "INSTRUÇÕES PARA IMPORTAÇÃO",
                    "1. Preencha somente a aba PRODUTOS.",
                    "2. Não altere os nomes das colunas.",
                    "3. Preço de venda é quanto o cliente pagará por uma unidade.",
                    "4. Preço de compra é quanto você pagou por uma unidade e é obrigatório quando existe estoque inicial.",
                    "5. Data de validade é opcional e deve usar DD/MM/AAAA.",
                    "6. O limite é de 1.000 produtos por importação.",
                    "7. Se alguma linha estiver errada, nenhum produto será cadastrado."
            };
            for (int linha = 0; linha < textos.length; linha++) {
                instrucoes.createRow(linha).createCell(0).setCellValue(textos[linha]);
            }
            instrucoes.setColumnWidth(0, 90 * 256);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gerar o modelo de importação.", exception);
        }
    }

    private LeituraPlanilha lerPlanilha(MultipartFile arquivo, Loja loja) {
        try (Workbook workbook = WorkbookFactory.create(arquivo.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                return erroGeral("A planilha não possui abas.");
            }

            Sheet sheet = workbook.getSheet("PRODUTOS");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAXIMO_LINHAS_FISICAS) {
                return erroGeral("A planilha possui linhas demais. Use o modelo com até 1.000 produtos.");
            }

            Row cabecalho = sheet.getRow(0);
            List<ImportacaoPlanilhaErro> erros = new ArrayList<>();
            MapaColunas colunas = mapearColunas(cabecalho, erros, loja.isFinanceiroAtivo());
            if (!erros.isEmpty()) {
                return new LeituraPlanilha(List.of(), 0, erros);
            }

            List<ProdutoRequest> produtos = new ArrayList<>();
            Set<String> chaves = new HashSet<>();
            produtoRepository.findByLojaId(loja.getId()).stream()
                    .map(this::chaveProduto)
                    .forEach(chaves::add);

            int totalLinhas = 0;
            for (int indice = 1; indice <= sheet.getLastRowNum(); indice++) {
                Row row = sheet.getRow(indice);
                if (linhaVazia(row)) continue;

                totalLinhas++;
                int numeroLinha = indice + 1;
                if (totalLinhas > MAXIMO_PRODUTOS) {
                    erros.add(new ImportacaoPlanilhaErro(
                            numeroLinha, "arquivo", "Limite de 1.000 produtos excedido."));
                    break;
                }

                int errosAntes = erros.size();
                String nome = texto(row, colunas.nome(), "nome", numeroLinha, erros, true);
                String tipo = texto(row, colunas.tipo(), "tipo", numeroLinha, erros, false);
                BigDecimal preco = decimal(row, colunas.preco(), "preco_venda", numeroLinha, erros, loja.isFinanceiroAtivo());
                BigDecimal custo = decimal(row, colunas.custo(), "custo_unitario", numeroLinha, erros, false);
                Integer quantidade = inteiro(row, colunas.quantidade(), "quantidade", numeroLinha, erros);
                String categoria = texto(row, colunas.categoria(), "categoria", numeroLinha, erros, true);
                LocalDate validade = data(row, colunas.validade(), numeroLinha, erros);

                validarTamanhos(nome, tipo, categoria, numeroLinha, erros);
                if (loja.isFinanceiroAtivo() && preco != null && preco.compareTo(BigDecimal.ZERO) <= 0) {
                    erros.add(new ImportacaoPlanilhaErro(numeroLinha, "preco_venda", "Deve ser maior que zero."));
                }
                if (custo != null && custo.compareTo(BigDecimal.ZERO) < 0) {
                    erros.add(new ImportacaoPlanilhaErro(numeroLinha, "custo_unitario", "Não pode ser negativo."));
                }
                if (quantidade != null && quantidade < 0) {
                    erros.add(new ImportacaoPlanilhaErro(numeroLinha, "quantidade", "Não pode ser negativa."));
                }
                if (loja.isFinanceiroAtivo() && quantidade != null && quantidade > 0
                        && (custo == null || custo.compareTo(BigDecimal.ZERO) <= 0)) {
                    erros.add(new ImportacaoPlanilhaErro(
                            numeroLinha, "custo_unitario",
                            "É obrigatório e deve ser maior que zero quando existe estoque inicial."));
                }

                if (erros.size() == errosAntes && nome != null && categoria != null) {
                    String chave = chaveProduto(nome, categoria);
                    if (!chaves.add(chave)) {
                        erros.add(new ImportacaoPlanilhaErro(
                                numeroLinha, "nome", "Produto duplicado na planilha, no estoque ou na lixeira. Restaure o cadastro existente."));
                    }
                }

                if (erros.size() == errosAntes) {
                    produtos.add(new ProdutoRequest(
                            nome,
                            tipo == null || tipo.isBlank() ? "UNIDADE" : tipo,
                            preco == null ? BigDecimal.ZERO : preco,
                            custo == null ? BigDecimal.ZERO : custo,
                            validade,
                            quantidade,
                            new CategoriaRequest(categoria),
                            null,
                            null));
                }
            }

            if (totalLinhas == 0) {
                erros.add(new ImportacaoPlanilhaErro(1, "arquivo", "Nenhum produto foi encontrado."));
            }

            return new LeituraPlanilha(produtos, totalLinhas, List.copyOf(erros));
        } catch (Exception exception) {
            return erroGeral("Arquivo Excel inválido, corrompido ou protegido por senha.");
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione uma planilha Excel.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A planilha deve ter no máximo 5 MB.");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !(nome.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                || nome.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
            throw new IllegalArgumentException("Envie um arquivo .xlsx ou .xls.");
        }
    }

    private MapaColunas mapearColunas(Row row, List<ImportacaoPlanilhaErro> erros, boolean financeiroAtivo) {
        if (row == null || row.getLastCellNum() > 30) {
            erros.add(new ImportacaoPlanilhaErro(1, "cabecalho", "Cabeçalho ausente ou inválido."));
            return MapaColunas.vazio();
        }

        Map<String, Integer> indices = new HashMap<>();
        for (int coluna = 0; coluna < row.getLastCellNum(); coluna++) {
            String valor = normalizar(FORMATADOR.formatCellValue(row.getCell(coluna)));
            if (!valor.isBlank()) indices.putIfAbsent(valor, coluna);
        }

        int nome = localizar(indices, "nome", "produto", "nome_do_produto");
        int tipo = localizar(indices, "tipo", "unidade", "unidade_de_medida");
        int preco = localizar(indices, "preco_venda", "preco", "valor_venda",
                "preco_de_venda", "preco_de_venda_quanto_cobrar");
        int custo = localizar(indices, "custo_unitario", "custo", "preco_custo",
                "preco_de_compra", "preco_de_compra_quanto_pagou");
        int quantidade = localizar(indices, "quantidade", "estoque", "quantidade_estoque", "quantidade_inicial");
        int categoria = localizar(indices, "categoria", "setor");
        int validade = localizar(indices, "data_validade", "validade", "data_de_validade");

        exigirColuna(nome, "nome", erros);
        if (financeiroAtivo) {
            exigirColuna(preco, "preco_venda", erros);
            exigirColuna(custo, "custo_unitario", erros);
        }
        exigirColuna(quantidade, "quantidade", erros);
        exigirColuna(categoria, "categoria", erros);
        return new MapaColunas(nome, tipo, preco, custo, quantidade, categoria, validade);
    }

    private void exigirColuna(int indice, String nome, List<ImportacaoPlanilhaErro> erros) {
        if (indice < 0) {
            erros.add(new ImportacaoPlanilhaErro(1, "cabecalho", "Coluna obrigatória ausente: " + nome));
        }
    }

    private int localizar(Map<String, Integer> indices, String... nomes) {
        for (String nome : nomes) {
            Integer indice = indices.get(nome);
            if (indice != null) return indice;
        }
        return -1;
    }

    private String texto(Row row, int coluna, String campo, int linha,
                         List<ImportacaoPlanilhaErro> erros, boolean obrigatorio) {
        if (coluna < 0) return null;
        Cell cell = row.getCell(coluna);
        if (formula(cell, campo, linha, erros)) return null;
        String valor = cell == null ? "" : FORMATADOR.formatCellValue(cell).trim();
        if (valor.isBlank()) {
            if (obrigatorio) {
                erros.add(new ImportacaoPlanilhaErro(linha, campo, "Campo obrigatório."));
            }
            return null;
        }
        return valor;
    }

    private BigDecimal decimal(Row row, int coluna, String campo, int linha,
                               List<ImportacaoPlanilhaErro> erros, boolean obrigatorio) {
        if (coluna < 0) return null;
        Cell cell = row.getCell(coluna);
        if (formula(cell, campo, linha, erros)) return null;
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            if (obrigatorio) erros.add(new ImportacaoPlanilhaErro(linha, campo, "Campo obrigatório."));
            return null;
        }

        try {
            BigDecimal valor;
            if (cell.getCellType() == CellType.NUMERIC) {
                valor = BigDecimal.valueOf(cell.getNumericCellValue());
            } else {
                String texto = FORMATADOR.formatCellValue(cell)
                        .replace("R$", "")
                        .replace("\u00A0", "")
                        .replace(" ", "")
                        .trim();
                if (texto.isBlank()) {
                    if (obrigatorio) erros.add(new ImportacaoPlanilhaErro(linha, campo, "Campo obrigatório."));
                    return null;
                }
                if (texto.contains(",")) {
                    texto = texto.replace(".", "").replace(',', '.');
                }
                valor = new BigDecimal(texto);
            }
            if (!campo.equals("quantidade") && (valor.precision() - valor.scale() > 17 || valor.stripTrailingZeros().scale() > 2)) {
                erros.add(new ImportacaoPlanilhaErro(linha, campo, "Use até 17 dígitos inteiros e 2 casas decimais."));
                return null;
            }
            return campo.equals("quantidade") ? valor : valor.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            erros.add(new ImportacaoPlanilhaErro(linha, campo, "Valor numérico inválido."));
            return null;
        }
    }

    private Integer inteiro(Row row, int coluna, String campo, int linha,
                            List<ImportacaoPlanilhaErro> erros) {
        BigDecimal valor = decimal(row, coluna, campo, linha, erros, true);
        if (valor == null) return null;
        try {
            return valor.intValueExact();
        } catch (ArithmeticException exception) {
            erros.add(new ImportacaoPlanilhaErro(linha, campo, "Use um número inteiro."));
            return null;
        }
    }

    private LocalDate data(Row row, int coluna, int linha,
                           List<ImportacaoPlanilhaErro> erros) {
        if (coluna < 0) return null;
        Cell cell = row.getCell(coluna);
        if (formula(cell, "data_validade", linha, erros)) return null;
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return DateUtil.getLocalDateTime(cell.getNumericCellValue()).toLocalDate();
            }
            String valor = FORMATADOR.formatCellValue(cell).trim();
            if (valor.isBlank()) return null;
            try {
                return LocalDate.parse(valor, DATA_BR);
            } catch (DateTimeParseException ignored) {
                return LocalDate.parse(valor);
            }
        } catch (DateTimeParseException exception) {
            erros.add(new ImportacaoPlanilhaErro(
                    linha, "data_validade", "Data inválida. Use DD/MM/AAAA."));
            return null;
        }
    }

    private boolean formula(Cell cell, String campo, int linha,
                            List<ImportacaoPlanilhaErro> erros) {
        if (cell != null && cell.getCellType() == CellType.FORMULA) {
            erros.add(new ImportacaoPlanilhaErro(
                    linha, campo, "Fórmulas não são aceitas. Cole somente o valor."));
            return true;
        }
        return false;
    }

    private void validarTamanhos(String nome, String tipo, String categoria, int linha,
                                 List<ImportacaoPlanilhaErro> erros) {
        if (nome != null && nome.length() > 120) {
            erros.add(new ImportacaoPlanilhaErro(linha, "nome", "Máximo de 120 caracteres."));
        }
        if (tipo != null && tipo.length() > 40) {
            erros.add(new ImportacaoPlanilhaErro(linha, "tipo", "Máximo de 40 caracteres."));
        }
        if (categoria != null && categoria.length() > 80) {
            erros.add(new ImportacaoPlanilhaErro(linha, "categoria", "Máximo de 80 caracteres."));
        }
    }

    private boolean linhaVazia(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (!FORMATADOR.formatCellValue(cell).trim().isBlank()) return false;
        }
        return true;
    }

    private String chaveProduto(Produto produto) {
        return chaveProduto(produto.getNome(), produto.getCategoria().getNome());
    }

    private String chaveProduto(String nome, String categoria) {
        return normalizar(nome) + "|" + normalizar(categoria);
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    private LeituraPlanilha erroGeral(String mensagem) {
        return new LeituraPlanilha(
                List.of(), 0,
                List.of(new ImportacaoPlanilhaErro(1, "arquivo", mensagem)));
    }

    private record LeituraPlanilha(
            List<ProdutoRequest> produtos,
            int totalLinhas,
            List<ImportacaoPlanilhaErro> erros) {
    }

    private record MapaColunas(
            int nome,
            int tipo,
            int preco,
            int custo,
            int quantidade,
            int categoria,
            int validade) {

        private static MapaColunas vazio() {
            return new MapaColunas(-1, -1, -1, -1, -1, -1, -1);
        }
    }
}

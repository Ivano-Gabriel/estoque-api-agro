package com.lojaagro.estoque_api;

import com.lojaagro.estoque_api.dto.*;
import com.lojaagro.estoque_api.entities.*;
import com.lojaagro.estoque_api.repositories.*;
import com.lojaagro.estoque_api.services.*;
import com.lojaagro.estoque_api.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.whatsapp.report-number=5582999999999",
                "spring.datasource.url=jdbc:h2:mem:pilot-integrity;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
                "spring.datasource.password=", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop", "app.admin.email=", "app.admin.password="})
class PilotIntegrityTest {
    @Autowired ProdutoService produtos;
    @Autowired MovimentacaoService movimentos;
    @Autowired ProdutoRepository produtoRepo;
    @Autowired UsuarioRepository usuarios;
    @Autowired TransacaoRepository transacoes;
    @Autowired OperacaoEstoqueRepository operacoes;
    @Autowired CategoriaRepository categorias;
    @Autowired FluxoCaixaRepository caixas;
    @Autowired RelatorioWhatsappService relatorios;
    @Autowired ProdutoImportacaoService importacao;
    @Autowired ProdutoCadastroLoteService cadastroLote;
    @Autowired JwtUtil jwt;
    @Autowired PasswordEncoder encoder;
    @Autowired LojaRepository lojas;
    @org.springframework.beans.factory.annotation.Value("${local.server.port}") int port;
    Usuario admin;
    Usuario funcionaria;
    Produto produto;
    Loja loja;

    @BeforeEach void preparar() {
        operacoes.deleteAll(); transacoes.deleteAll(); produtoRepo.deleteAll(); categorias.deleteAll(); usuarios.deleteAll();
        FluxoCaixa caixa = caixas.findById(1L).orElseThrow();
        caixa.setTotalEntradas(BigDecimal.ZERO); caixa.setTotalSaidas(BigDecimal.ZERO); caixas.saveAndFlush(caixa);
        loja = lojas.findAll().getFirst();
        admin = usuario("admin@teste.local", UsuarioRole.ADMIN);
        funcionaria = usuario("funcionaria@teste.local", UsuarioRole.FUNCIONARIA);
        produto = produtos.criar(request("Ração Premium 15kg", LocalDate.of(2027, 1, 10)), loja);
    }

    Usuario usuario(String email, UsuarioRole role) {
        Usuario u = new Usuario(email, encoder.encode("senha-teste-segura")); u.setRole(role); u.setLoja(loja); return usuarios.saveAndFlush(u);
    }
    ProdutoRequest request(String nome, LocalDate validade) {
        return new ProdutoRequest(nome, "UNIDADE", new BigDecimal("150.00"), new BigDecimal("100.00"), validade, 10, new CategoriaRequest("Rações"), null, null);
    }
    MovimentacaoRequest venda(int quantidade) { return new MovimentacaoRequest(quantidade, new BigDecimal("150.00")); }

    @Test void repetirVendaInclusiveSimultaneaNaoDuplicaEstoqueNemCaixa() throws Exception {
        String key = UUID.randomUUID().toString();
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch iniciar = new CountDownLatch(1);
            Callable<Void> acao = () -> { iniciar.await(); movimentos.executar(key, true, produto.getId(), venda(2), admin); return null; };
            var primeira = executor.submit(acao); var segunda = executor.submit(acao); iniciar.countDown();
            primeira.get(15, TimeUnit.SECONDS); segunda.get(15, TimeUnit.SECONDS);
        }
        movimentos.executar(key, true, produto.getId(), venda(2), admin);
        assertEquals(8, produtoRepo.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(1, transacoes.count()); assertEquals(1, operacoes.count());
        assertEquals(new BigDecimal("300.00"), caixas.findById(1L).orElseThrow().getTotalEntradas());
        assertThrows(IllegalArgumentException.class, () -> movimentos.executar(key, true, produto.getId(), venda(3), admin));
        assertThrows(IllegalArgumentException.class, () -> movimentos.executar(key, true, produto.getId(), venda(2), funcionaria));
    }

    @Test void compraRepetidaNaoDuplicaESemEstoqueFazRollback() {
        String key = UUID.randomUUID().toString();
        MovimentacaoRequest compra = new MovimentacaoRequest(3, new BigDecimal("100.00"));
        movimentos.executar(key, false, produto.getId(), compra, admin);
        movimentos.executar(key, false, produto.getId(), compra, admin);
        assertEquals(13, produtoRepo.findById(produto.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(new BigDecimal("300.00"), caixas.findById(1L).orElseThrow().getTotalSaidas());
        assertThrows(IllegalArgumentException.class, () -> movimentos.executar(UUID.randomUUID().toString(), true, produto.getId(), venda(99), admin));
        assertEquals(1, transacoes.count()); assertEquals(1, operacoes.count());
    }

    @Test void lixeiraPreservaHistoricoRelatorioEImpedeDuplicados() {
        movimentos.executar(UUID.randomUUID().toString(), true, produto.getId(), venda(2), admin);
        produtos.deletar(produto.getId(), loja);
        assertTrue(produtos.buscarTodos(loja).isEmpty()); assertTrue(produtos.buscarPorId(produto.getId(), loja).isEmpty());
        assertEquals(produto.getNome(), transacoes.findAll().getFirst().getProduto().getNome());
        assertTrue(relatorios.gerar("DIARIO", loja).mensagem().contains("Total vendido: R$ 300,00"));
        assertThrows(IllegalArgumentException.class, () -> produtos.criar(request(produto.getNome(), null), loja));
        var resultado = importacao.importar(new MockMultipartFile("arquivo", "modelo.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", importacao.gerarModelo()), loja);
        assertEquals(0, resultado.totalImportado()); assertFalse(resultado.erros().isEmpty());
        produtos.restaurar(produto.getId(), loja);
        assertEquals(1, produtos.buscarTodos(loja).size()); assertEquals(1, transacoes.count());
    }

    @Test void editarSemDataPreservaValidade() {
        produtos.atualizar(produto.getId(), request("Ração atualizada", null), loja);
        assertEquals(LocalDate.of(2027, 1, 10), produtoRepo.findById(produto.getId()).orElseThrow().getDataValidade());
    }

    @Test void lojasNaoEnxergamProdutosUmaDaOutra() {
        Loja outra = lojas.saveAndFlush(new Loja("Outra loja", "outra-loja", false, false, "5582999999999"));
        caixas.saveAndFlush(new FluxoCaixa(outra.getId(), outra));
        Produto exclusivo = produtos.criar(new ProdutoRequest("Camisa", "UNIDADE", null, null, null, 2,
                new CategoriaRequest("Roupas"), "Camisa azul", null), outra);
        assertEquals(1, produtos.buscarTodos(loja).size());
        assertEquals(1, produtos.buscarTodos(outra).size());
        assertTrue(produtos.buscarPorId(exclusivo.getId(), loja).isEmpty());
        assertTrue(produtos.buscarPorId(produto.getId(), outra).isEmpty());
        ProdutoRequest comFoto = new ProdutoRequest("Camisa com foto", "UNIDADE", null, null, null, 1,
                new CategoriaRequest("Roupas"), "Camisa preta", "https://res.cloudinary.com/test/image/upload/camisa.webp");
        assertThrows(IllegalArgumentException.class, () -> produtos.criar(comFoto, outra));
    }

    @Test void cadastroEmMassaRespeitaLojaEPermissaoDeAdministrador() throws Exception {
        String corpo = "{\"produtos\":[{\"nome\":\"Produto em massa\",\"tipo\":\"UNIDADE\"," +
                "\"preco\":20,\"custoUnitario\":10,\"quantidadeEstoque\":3," +
                "\"categoria\":{\"nome\":\"Geral\"},\"descricao\":null,\"imagemUrl\":null}]}";
        assertEquals(403, http("POST", "/produtos/cadastro-em-massa", jwt.gerarToken(funcionaria), corpo).statusCode());
        assertEquals(201, http("POST", "/produtos/cadastro-em-massa", jwt.gerarToken(admin), corpo).statusCode());
        assertEquals(2, produtos.buscarTodos(loja).size());
    }

    @Test void permissoesBloqueioRevogacaoESenhaSaoAplicadosNaApi() throws Exception {
        String adminToken = jwt.gerarToken(admin), funcToken = jwt.gerarToken(funcionaria);
        assertEquals(401, http("GET", "/produtos", null, null).statusCode());
        assertEquals(403, http("GET", "/fluxo-caixa", funcToken, null).statusCode());
        assertEquals(403, http("GET", "/admin/usuarios", funcToken, null).statusCode());
        assertEquals(200, http("GET", "/relatorios/whatsapp?periodo=DIARIO", funcToken, null).statusCode());
        String base = "/admin/usuarios/" + funcionaria.getId();
        assertEquals(200, http("POST", base + "/revogar-sessoes", adminToken, null).statusCode());
        assertEquals(401, http("GET", "/produtos", funcToken, null).statusCode());
        funcToken = jwt.gerarToken(usuarios.findById(funcionaria.getId()).orElseThrow());
        assertEquals(200, http("PUT", base + "/acesso", adminToken, "{\"ativo\":false}").statusCode());
        assertEquals(401, http("GET", "/produtos", funcToken, null).statusCode());
        assertEquals(401, http("POST", "/auth/login", null, "{\"email\":\"funcionaria@teste.local\",\"senha\":\"senha-teste-segura\"}").statusCode());
        http("PUT", base + "/acesso", adminToken, "{\"ativo\":true}");
        funcToken = jwt.gerarToken(usuarios.findById(funcionaria.getId()).orElseThrow());
        assertEquals(200, http("PUT", base + "/senha", adminToken, "{\"senha\":\"nova-senha-segura\"}").statusCode());
        assertEquals(401, http("GET", "/produtos", funcToken, null).statusCode());
        assertEquals(200, http("POST", "/auth/login", null, "{\"email\":\"funcionaria@teste.local\",\"senha\":\"nova-senha-segura\"}").statusCode());
        assertEquals(400, http("POST", "/categorias", adminToken, "{\"nome\":\"\"}").statusCode());
        assertEquals(404, http("DELETE", "/admin/ferramentas-teste/reset-financeiro", adminToken, null).statusCode());
    }

    @Test void endpointExigeChaveERespondeAoPreflightDoFrontend() throws Exception {
        String token = jwt.gerarToken(admin);
        String path = "/produtos/" + produto.getId() + "/venda-com-lucro";
        String body = "{\"quantidade\":2,\"preco\":150}";
        assertEquals(400, http("PUT", path, token, body).statusCode());
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Authorization", "Bearer " + token).header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        try (var client = HttpClient.newHttpClient()) {
            assertEquals(204, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(204, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
            var preflight = HttpRequest.newBuilder(request.uri()).header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", "PUT")
                    .header("Access-Control-Request-Headers", "authorization,content-type,idempotency-key")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var response = client.send(preflight, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Access-Control-Allow-Headers").orElse("").toLowerCase().contains("idempotency-key"));
        }
        assertEquals(1, transacoes.count());
    }

    HttpResponse<String> http(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return HttpClient.newHttpClient().send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}

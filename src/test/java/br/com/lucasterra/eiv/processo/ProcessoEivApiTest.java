package br.com.lucasterra.eiv.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa a API de ponta a ponta, com banco H2 e a data de hoje fixada em 05/10/2026. */
@SpringBootTest(properties = "eiv.dados-exemplo=false")
class ProcessoEivApiTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 5);

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock relogioFixo() {
            ZoneId fuso = ZoneId.of("America/Sao_Paulo");
            return Clock.fixed(HOJE.atStartOfDay(fuso).toInstant(), fuso);
        }
    }

    @Autowired
    WebApplicationContext contexto;

    @Autowired
    ProcessoEivRepository repository;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private ResultActions cadastrar(String protocolo, LocalDate dataProtocolo, Integer prazo) throws Exception {
        String prazoJson = prazo == null ? "" : ", \"prazoAnaliseDias\": " + prazo;
        String json = """
                {"numeroProtocolo": "%s", "empreendimento": "Supermercado", "requerente": "Mercado Ltda.",
                 "endereco": "Av. Central, 100", "dataProtocolo": "%s"%s}
                """.formatted(protocolo, dataProtocolo, prazoJson);
        return mvc.perform(post("/api/processos").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long idDoProtocolo(String protocolo) {
        return repository.findAll().stream()
                .filter(p -> p.getNumeroProtocolo().equals(protocolo))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void cadastraProcessoComPrazoPadraoECalculaVencimento() throws Exception {
        cadastrar("EIV-2026-0001", LocalDate.of(2026, 10, 1), null)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.prazoAnaliseDias").value(30))
                .andExpect(jsonPath("$.dataVencimento").value("2026-10-31"))
                .andExpect(jsonPath("$.diasRestantes").value(26))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.situacaoPrazo").value("NO_PRAZO"));
    }

    @Test
    void rejeitaCamposObrigatoriosEmBranco() throws Exception {
        mvc.perform(post("/api/processos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.numeroProtocolo").exists())
                .andExpect(jsonPath("$.campos.dataProtocolo").exists());
    }

    @Test
    void rejeitaDataDeProtocoloNoFuturo() throws Exception {
        cadastrar("EIV-2026-0002", HOJE.plusDays(1), 30)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.dataProtocolo").exists());
    }

    @Test
    void naoPermiteProtocoloRepetido() throws Exception {
        cadastrar("EIV-2026-0003", HOJE, 30).andExpect(status().isCreated());

        cadastrar("EIV-2026-0003", HOJE, 30)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Já existe um processo com o protocolo EIV-2026-0003."));
    }

    @Test
    void alertasTrazemSoProcessosEmAnaliseVencidosOuVencendo() throws Exception {
        cadastrar("NO-PRAZO", HOJE.minusDays(10), 30);       // vence em 20 dias
        cadastrar("VENCE-LOGO", HOJE.minusDays(25), 30);     // vence em 5 dias
        cadastrar("VENCIDO", HOJE.minusDays(40), 30);        // venceu há 10 dias
        cadastrar("SUSPENSO", HOJE.minusDays(40), 30);
        mvc.perform(patch("/api/processos/{id}/status", idDoProtocolo("SUSPENSO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AGUARDANDO_DOCUMENTOS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoPrazo").value("SUSPENSO"));

        mvc.perform(get("/api/processos/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].numeroProtocolo").value("VENCIDO"))
                .andExpect(jsonPath("$[0].diasRestantes").value(-10))
                .andExpect(jsonPath("$[1].numeroProtocolo").value("VENCE-LOGO"));

        mvc.perform(get("/api/processos/resumo"))
                .andExpect(jsonPath("$.noPrazo").value(1))
                .andExpect(jsonPath("$.venceEmBreve").value(1))
                .andExpect(jsonPath("$.vencidos").value(1))
                .andExpect(jsonPath("$.suspensos").value(1))
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    void filtraListagemPorSituacao() throws Exception {
        cadastrar("NO-PRAZO", HOJE.minusDays(10), 30);
        cadastrar("VENCIDO", HOJE.minusDays(40), 30);

        mvc.perform(get("/api/processos").param("situacao", "VENCIDO"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroProtocolo").value("VENCIDO"));
    }

    @Test
    void naoReabreProcessoConcluido() throws Exception {
        cadastrar("EIV-2026-0004", HOJE, 30);
        long id = idDoProtocolo("EIV-2026-0004");
        mvc.perform(patch("/api/processos/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"APROVADO\"}"))
                .andExpect(jsonPath("$.dataConclusao").value("2026-10-05"));

        mvc.perform(patch("/api/processos/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"EM_ANALISE\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void retorna404ParaProcessoInexistente() throws Exception {
        mvc.perform(get("/api/processos/{id}", 9999)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/processos/{id}", 9999)).andExpect(status().isNotFound());
    }
}

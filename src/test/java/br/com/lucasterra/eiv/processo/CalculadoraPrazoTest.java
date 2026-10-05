package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.compartilhado.EivProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraPrazoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 5);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final CalculadoraPrazo calculadora = new CalculadoraPrazo(
            Clock.fixed(HOJE.atStartOfDay(FUSO).toInstant(), FUSO),
            new EivProperties(30, 7, false));

    private ProcessoEiv venceEm(int diasAPartirDeHoje) {
        // Prazo de 30 dias: protocolar 30 dias antes do vencimento desejado.
        LocalDate protocolo = HOJE.plusDays(diasAPartirDeHoje).minusDays(30);
        return new ProcessoEiv("EIV-1", "Loja", "Requerente", "Rua A, 1", null, protocolo, 30, null);
    }

    @Test
    void foraDoPeriodoDeAlertaEstaNoPrazo() {
        ProcessoEiv processo = venceEm(8);

        assertThat(calculadora.situacao(processo)).isEqualTo(SituacaoPrazo.NO_PRAZO);
        assertThat(calculadora.diasRestantes(processo)).isEqualTo(8);
    }

    @Test
    void dentroDoPeriodoDeAlertaVenceEmBreve() {
        assertThat(calculadora.situacao(venceEm(7))).isEqualTo(SituacaoPrazo.VENCE_EM_BREVE);
        assertThat(calculadora.situacao(venceEm(0))).isEqualTo(SituacaoPrazo.VENCE_EM_BREVE);
    }

    @Test
    void depoisDoVencimentoEstaVencido() {
        ProcessoEiv processo = venceEm(-1);

        assertThat(calculadora.situacao(processo)).isEqualTo(SituacaoPrazo.VENCIDO);
        assertThat(calculadora.diasRestantes(processo)).isEqualTo(-1);
    }

    @Test
    void aguardandoDocumentosFicaSuspensoSemDiasRestantes() {
        ProcessoEiv processo = venceEm(-3);
        processo.alterarStatus(StatusProcesso.AGUARDANDO_DOCUMENTOS, HOJE);

        assertThat(calculadora.situacao(processo)).isEqualTo(SituacaoPrazo.SUSPENSO);
        assertThat(calculadora.diasRestantes(processo)).isNull();
    }

    @Test
    void processoAprovadoEstaConcluidoMesmoVencido() {
        ProcessoEiv processo = venceEm(-10);
        processo.alterarStatus(StatusProcesso.APROVADO, HOJE);

        assertThat(calculadora.situacao(processo)).isEqualTo(SituacaoPrazo.CONCLUIDO);
    }
}

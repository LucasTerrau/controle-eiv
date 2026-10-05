package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.compartilhado.EivProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Diz em que situação está o prazo de um processo na data de hoje.
 * Recebe um {@link Clock} para que os testes possam fixar a data.
 */
@Component
public class CalculadoraPrazo {

    private final Clock clock;
    private final int alertaDias;

    public CalculadoraPrazo(Clock clock, EivProperties properties) {
        this.clock = clock;
        this.alertaDias = properties.alertaDias();
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    /** Último vencimento que ainda entra na lista de alertas. */
    public LocalDate limiteDeAlerta() {
        return hoje().plusDays(alertaDias);
    }

    public SituacaoPrazo situacao(ProcessoEiv processo) {
        if (processo.getStatus().isConcluido()) {
            return SituacaoPrazo.CONCLUIDO;
        }
        if (processo.getStatus() == StatusProcesso.AGUARDANDO_DOCUMENTOS) {
            return SituacaoPrazo.SUSPENSO;
        }
        long restantes = ChronoUnit.DAYS.between(hoje(), processo.getDataVencimento());
        if (restantes < 0) {
            return SituacaoPrazo.VENCIDO;
        }
        if (restantes <= alertaDias) {
            return SituacaoPrazo.VENCE_EM_BREVE;
        }
        return SituacaoPrazo.NO_PRAZO;
    }

    /** Dias até o vencimento, negativo se já venceu. Nulo quando o prazo não está correndo. */
    public Long diasRestantes(ProcessoEiv processo) {
        if (processo.getStatus() != StatusProcesso.EM_ANALISE) {
            return null;
        }
        return ChronoUnit.DAYS.between(hoje(), processo.getDataVencimento());
    }
}

package br.com.lucasterra.eiv.processo.dto;

import br.com.lucasterra.eiv.processo.SituacaoPrazo;

import java.util.List;

/** Contagem de processos por situação do prazo, para um painel de acompanhamento. */
public record ResumoPrazos(
        long noPrazo,
        long venceEmBreve,
        long vencidos,
        long suspensos,
        long concluidos,
        long total
) {

    public static ResumoPrazos de(List<SituacaoPrazo> situacoes) {
        return new ResumoPrazos(
                contar(situacoes, SituacaoPrazo.NO_PRAZO),
                contar(situacoes, SituacaoPrazo.VENCE_EM_BREVE),
                contar(situacoes, SituacaoPrazo.VENCIDO),
                contar(situacoes, SituacaoPrazo.SUSPENSO),
                contar(situacoes, SituacaoPrazo.CONCLUIDO),
                situacoes.size());
    }

    private static long contar(List<SituacaoPrazo> situacoes, SituacaoPrazo alvo) {
        return situacoes.stream().filter(s -> s == alvo).count();
    }
}

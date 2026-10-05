package br.com.lucasterra.eiv.processo.dto;

import br.com.lucasterra.eiv.processo.ProcessoEiv;
import br.com.lucasterra.eiv.processo.SituacaoPrazo;
import br.com.lucasterra.eiv.processo.StatusProcesso;

import java.time.LocalDate;

public record ProcessoResponse(
        Long id,
        String numeroProtocolo,
        String empreendimento,
        String requerente,
        String endereco,
        String engenheiroResponsavel,
        LocalDate dataProtocolo,
        int prazoAnaliseDias,
        int diasSuspensos,
        LocalDate dataVencimento,
        Long diasRestantes,
        StatusProcesso status,
        SituacaoPrazo situacaoPrazo,
        LocalDate dataConclusao,
        String observacoes
) {

    public static ProcessoResponse de(ProcessoEiv p, SituacaoPrazo situacao, Long diasRestantes) {
        return new ProcessoResponse(p.getId(), p.getNumeroProtocolo(), p.getEmpreendimento(), p.getRequerente(),
                p.getEndereco(), p.getEngenheiroResponsavel(), p.getDataProtocolo(), p.getPrazoAnaliseDias(),
                p.getDiasSuspensos(), p.getDataVencimento(), diasRestantes, p.getStatus(), situacao,
                p.getDataConclusao(), p.getObservacoes());
    }
}

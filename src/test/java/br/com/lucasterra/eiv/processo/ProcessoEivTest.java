package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.compartilhado.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessoEivTest {

    private static final LocalDate PROTOCOLO = LocalDate.of(2026, 3, 2);

    private ProcessoEiv novoProcesso(int prazo) {
        return new ProcessoEiv("EIV-1", "Loja", "Requerente", "Rua A, 1", null, PROTOCOLO, prazo, null);
    }

    @Test
    void vencimentoEhDataDeProtocoloMaisPrazo() {
        ProcessoEiv processo = novoProcesso(30);

        assertThat(processo.getStatus()).isEqualTo(StatusProcesso.EM_ANALISE);
        assertThat(processo.getDataVencimento()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    void diasAguardandoDocumentosSaoSomadosAoVencimento() {
        ProcessoEiv processo = novoProcesso(30);

        processo.alterarStatus(StatusProcesso.AGUARDANDO_DOCUMENTOS, LocalDate.of(2026, 3, 10));
        processo.alterarStatus(StatusProcesso.EM_ANALISE, LocalDate.of(2026, 3, 22));

        assertThat(processo.getDiasSuspensos()).isEqualTo(12);
        assertThat(processo.getDataVencimento()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(processo.getDataInicioSuspensao()).isNull();
    }

    @Test
    void suspensoesSeguidasSeAcumulam() {
        ProcessoEiv processo = novoProcesso(30);

        processo.alterarStatus(StatusProcesso.AGUARDANDO_DOCUMENTOS, LocalDate.of(2026, 3, 5));
        processo.alterarStatus(StatusProcesso.EM_ANALISE, LocalDate.of(2026, 3, 8));
        processo.alterarStatus(StatusProcesso.AGUARDANDO_DOCUMENTOS, LocalDate.of(2026, 3, 15));
        processo.alterarStatus(StatusProcesso.EM_ANALISE, LocalDate.of(2026, 3, 20));

        assertThat(processo.getDiasSuspensos()).isEqualTo(8);
    }

    @Test
    void concluirRegistraADataDeConclusao() {
        ProcessoEiv processo = novoProcesso(30);

        processo.alterarStatus(StatusProcesso.APROVADO, LocalDate.of(2026, 3, 20));

        assertThat(processo.getDataConclusao()).isEqualTo(LocalDate.of(2026, 3, 20));
    }

    @Test
    void processoConcluidoNaoMudaMaisDeStatus() {
        ProcessoEiv processo = novoProcesso(30);
        processo.alterarStatus(StatusProcesso.REPROVADO, LocalDate.of(2026, 3, 20));

        assertThatThrownBy(() -> processo.alterarStatus(StatusProcesso.EM_ANALISE, LocalDate.of(2026, 3, 21)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já foi concluído");
    }
}

package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.compartilhado.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Processo de Estudo de Impacto de Vizinhança (EIV) protocolado na prefeitura.
 *
 * O vencimento da análise é a data de protocolo + prazo de análise + dias em que
 * o processo ficou parado aguardando documentos do requerente.
 */
@Entity
@Table(name = "processo_eiv")
public class ProcessoEiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_protocolo", nullable = false, unique = true, length = 30)
    private String numeroProtocolo;

    @Column(nullable = false, length = 150)
    private String empreendimento;

    @Column(nullable = false, length = 150)
    private String requerente;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Column(name = "engenheiro_responsavel", length = 150)
    private String engenheiroResponsavel;

    @Column(name = "data_protocolo", nullable = false)
    private LocalDate dataProtocolo;

    @Column(name = "prazo_analise_dias", nullable = false)
    private int prazoAnaliseDias;

    @Column(name = "dias_suspensos", nullable = false)
    private int diasSuspensos;

    @Column(name = "data_inicio_suspensao")
    private LocalDate dataInicioSuspensao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private StatusProcesso status;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(length = 1000)
    private String observacoes;

    protected ProcessoEiv() {
        // exigido pelo JPA
    }

    public ProcessoEiv(String numeroProtocolo, String empreendimento, String requerente, String endereco,
                       String engenheiroResponsavel, LocalDate dataProtocolo, int prazoAnaliseDias,
                       String observacoes) {
        this.numeroProtocolo = numeroProtocolo;
        this.status = StatusProcesso.EM_ANALISE;
        atualizarDados(empreendimento, requerente, endereco, engenheiroResponsavel,
                dataProtocolo, prazoAnaliseDias, observacoes);
    }

    public void atualizarDados(String empreendimento, String requerente, String endereco,
                               String engenheiroResponsavel, LocalDate dataProtocolo, int prazoAnaliseDias,
                               String observacoes) {
        this.empreendimento = empreendimento;
        this.requerente = requerente;
        this.endereco = endereco;
        this.engenheiroResponsavel = engenheiroResponsavel;
        this.dataProtocolo = dataProtocolo;
        this.prazoAnaliseDias = prazoAnaliseDias;
        this.observacoes = observacoes;
        recalcularVencimento();
    }

    public void alterarNumeroProtocolo(String numeroProtocolo) {
        this.numeroProtocolo = numeroProtocolo;
    }

    /**
     * Muda o status aplicando as regras de prazo:
     * - ao entrar em AGUARDANDO_DOCUMENTOS, o prazo fica suspenso a partir de hoje;
     * - ao sair dele, os dias parados são somados ao vencimento;
     * - processos aprovados ou reprovados não podem mais mudar de status.
     */
    public void alterarStatus(StatusProcesso novoStatus, LocalDate hoje) {
        if (status.isConcluido()) {
            throw new RegraDeNegocioException(
                    "O processo " + numeroProtocolo + " já foi concluído como " + status + ".");
        }
        if (novoStatus == status) {
            return;
        }
        if (status == StatusProcesso.AGUARDANDO_DOCUMENTOS) {
            diasSuspensos += (int) ChronoUnit.DAYS.between(dataInicioSuspensao, hoje);
            dataInicioSuspensao = null;
        }
        if (novoStatus == StatusProcesso.AGUARDANDO_DOCUMENTOS) {
            dataInicioSuspensao = hoje;
        }
        dataConclusao = novoStatus.isConcluido() ? hoje : null;
        status = novoStatus;
        recalcularVencimento();
    }

    private void recalcularVencimento() {
        dataVencimento = dataProtocolo.plusDays(prazoAnaliseDias + diasSuspensos);
    }

    public Long getId() {
        return id;
    }

    public String getNumeroProtocolo() {
        return numeroProtocolo;
    }

    public String getEmpreendimento() {
        return empreendimento;
    }

    public String getRequerente() {
        return requerente;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getEngenheiroResponsavel() {
        return engenheiroResponsavel;
    }

    public LocalDate getDataProtocolo() {
        return dataProtocolo;
    }

    public int getPrazoAnaliseDias() {
        return prazoAnaliseDias;
    }

    public int getDiasSuspensos() {
        return diasSuspensos;
    }

    public LocalDate getDataInicioSuspensao() {
        return dataInicioSuspensao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public String getObservacoes() {
        return observacoes;
    }
}

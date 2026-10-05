package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.compartilhado.EivProperties;
import br.com.lucasterra.eiv.compartilhado.ProcessoNaoEncontradoException;
import br.com.lucasterra.eiv.compartilhado.RegraDeNegocioException;
import br.com.lucasterra.eiv.processo.dto.ProcessoRequest;
import br.com.lucasterra.eiv.processo.dto.ProcessoResponse;
import br.com.lucasterra.eiv.processo.dto.ResumoPrazos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProcessoEivService {

    private final ProcessoEivRepository repository;
    private final CalculadoraPrazo calculadora;
    private final int prazoPadraoDias;

    public ProcessoEivService(ProcessoEivRepository repository, CalculadoraPrazo calculadora,
                              EivProperties properties) {
        this.repository = repository;
        this.calculadora = calculadora;
        this.prazoPadraoDias = properties.prazoPadraoDias();
    }

    @Transactional
    public ProcessoResponse criar(ProcessoRequest request) {
        String protocolo = request.numeroProtocolo().trim();
        if (repository.existsByNumeroProtocolo(protocolo)) {
            throw new RegraDeNegocioException("Já existe um processo com o protocolo " + protocolo + ".");
        }
        ProcessoEiv processo = new ProcessoEiv(protocolo, request.empreendimento(), request.requerente(),
                request.endereco(), request.engenheiroResponsavel(), request.dataProtocolo(),
                prazoOuPadrao(request), request.observacoes());
        return paraResponse(repository.save(processo));
    }

    public List<ProcessoResponse> listar(StatusProcesso status, SituacaoPrazo situacao) {
        List<ProcessoEiv> processos = status == null
                ? repository.findAllByOrderByDataVencimentoAsc()
                : repository.findByStatusOrderByDataVencimentoAsc(status);
        return processos.stream()
                .filter(p -> situacao == null || calculadora.situacao(p) == situacao)
                .map(this::paraResponse)
                .toList();
    }

    public ProcessoResponse buscar(Long id) {
        return paraResponse(encontrar(id));
    }

    @Transactional
    public ProcessoResponse atualizar(Long id, ProcessoRequest request) {
        ProcessoEiv processo = encontrar(id);
        String protocolo = request.numeroProtocolo().trim();
        if (repository.existsByNumeroProtocoloAndIdNot(protocolo, id)) {
            throw new RegraDeNegocioException("Já existe um processo com o protocolo " + protocolo + ".");
        }
        processo.alterarNumeroProtocolo(protocolo);
        processo.atualizarDados(request.empreendimento(), request.requerente(), request.endereco(),
                request.engenheiroResponsavel(), request.dataProtocolo(), prazoOuPadrao(request),
                request.observacoes());
        return paraResponse(processo);
    }

    @Transactional
    public ProcessoResponse alterarStatus(Long id, StatusProcesso novoStatus) {
        ProcessoEiv processo = encontrar(id);
        processo.alterarStatus(novoStatus, calculadora.hoje());
        return paraResponse(processo);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
    }

    /** Processos em análise que já venceram ou vencem dentro do período de alerta. */
    public List<ProcessoResponse> alertas() {
        return repository.findByStatusAndDataVencimentoLessThanEqualOrderByDataVencimentoAsc(
                        StatusProcesso.EM_ANALISE, calculadora.limiteDeAlerta())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    public ResumoPrazos resumo() {
        List<SituacaoPrazo> situacoes = repository.findAll().stream()
                .map(calculadora::situacao)
                .toList();
        return ResumoPrazos.de(situacoes);
    }

    private ProcessoEiv encontrar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProcessoNaoEncontradoException(id));
    }

    private int prazoOuPadrao(ProcessoRequest request) {
        return request.prazoAnaliseDias() != null ? request.prazoAnaliseDias() : prazoPadraoDias;
    }

    private ProcessoResponse paraResponse(ProcessoEiv processo) {
        return ProcessoResponse.de(processo, calculadora.situacao(processo), calculadora.diasRestantes(processo));
    }
}

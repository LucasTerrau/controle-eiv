package br.com.lucasterra.eiv.processo;

import br.com.lucasterra.eiv.processo.dto.AlteracaoStatusRequest;
import br.com.lucasterra.eiv.processo.dto.ProcessoRequest;
import br.com.lucasterra.eiv.processo.dto.ProcessoResponse;
import br.com.lucasterra.eiv.processo.dto.ResumoPrazos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/processos")
public class ProcessoEivController {

    private final ProcessoEivService service;

    public ProcessoEivController(ProcessoEivService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcessoResponse> criar(@Valid @RequestBody ProcessoRequest request) {
        ProcessoResponse criado = service.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping
    public List<ProcessoResponse> listar(@RequestParam(required = false) StatusProcesso status,
                                         @RequestParam(required = false) SituacaoPrazo situacao) {
        return service.listar(status, situacao);
    }

    @GetMapping("/alertas")
    public List<ProcessoResponse> alertas() {
        return service.alertas();
    }

    @GetMapping("/resumo")
    public ResumoPrazos resumo() {
        return service.resumo();
    }

    @GetMapping("/{id}")
    public ProcessoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ProcessoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProcessoRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProcessoResponse alterarStatus(@PathVariable Long id,
                                          @Valid @RequestBody AlteracaoStatusRequest request) {
        return service.alterarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

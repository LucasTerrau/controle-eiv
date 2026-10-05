package br.com.lucasterra.eiv.processo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Cadastra processos fictícios com datas relativas a hoje, para que a API já mostre
 * processos no prazo, vencendo e vencidos logo na primeira execução.
 */
@Component
@ConditionalOnProperty(name = "eiv.dados-exemplo", havingValue = "true")
public class DadosExemplo implements ApplicationRunner {

    private final ProcessoEivRepository repository;
    private final CalculadoraPrazo calculadora;

    public DadosExemplo(ProcessoEivRepository repository, CalculadoraPrazo calculadora) {
        this.repository = repository;
        this.calculadora = calculadora;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        LocalDate hoje = calculadora.hoje();

        repository.save(new ProcessoEiv("EIV-2026-0101", "Supermercado Bom Preço", "Bom Preço Comércio Ltda.",
                "Av. Central, 1200", "Eng. Carla Mendes", hoje.minusDays(5), 30, null));
        repository.save(new ProcessoEiv("EIV-2026-0098", "Residencial Jardim das Acácias", "Construtora Horizonte S.A.",
                "Rua das Acácias, 45", "Eng. Paulo Ribeiro", hoje.minusDays(25), 30,
                "Projeto com 120 unidades habitacionais."));
        repository.save(new ProcessoEiv("EIV-2026-0090", "Posto de combustível Rota Sul", "Rota Sul Combustíveis Ltda.",
                "Rodovia Estadual, km 12", "Eng. Carla Mendes", hoje.minusDays(36), 30, null));

        ProcessoEiv aguardando = new ProcessoEiv("EIV-2026-0095", "Galpão logístico Passos Log", "Passos Log Ltda.",
                "Distrito Industrial, Quadra 7", "Eng. Paulo Ribeiro", hoje.minusDays(20), 45, null);
        aguardando.alterarStatus(StatusProcesso.AGUARDANDO_DOCUMENTOS, hoje.minusDays(4));
        repository.save(aguardando);

        ProcessoEiv aprovado = new ProcessoEiv("EIV-2026-0071", "Clínica Vida Plena", "Vida Plena Serviços Médicos",
                "Rua das Flores, 310", "Eng. Ana Souza", hoje.minusDays(60), 30, null);
        aprovado.alterarStatus(StatusProcesso.APROVADO, hoje.minusDays(33));
        repository.save(aprovado);
    }
}

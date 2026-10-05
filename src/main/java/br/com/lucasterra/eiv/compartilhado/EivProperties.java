package br.com.lucasterra.eiv.compartilhado;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurações do sistema, definidas em application.properties.
 *
 * @param prazoPadraoDias prazo de análise usado quando o processo não informa um
 * @param alertaDias      quantos dias antes do vencimento o processo entra nos alertas
 * @param dadosExemplo    se deve cadastrar processos de exemplo ao iniciar com o banco vazio
 */
@ConfigurationProperties(prefix = "eiv")
public record EivProperties(int prazoPadraoDias, int alertaDias, boolean dadosExemplo) {
}

package br.com.lucasterra.eiv.compartilhado;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(EivProperties.class)
public class ConfiguracaoGeral {

    /** Relógio no fuso de Brasília; os testes substituem por um relógio fixo. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}

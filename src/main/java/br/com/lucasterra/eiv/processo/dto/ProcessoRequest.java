package br.com.lucasterra.eiv.processo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dados para cadastrar ou atualizar um processo. Sem prazo informado, usa o prazo padrão. */
public record ProcessoRequest(
        @NotBlank @Size(max = 30) String numeroProtocolo,
        @NotBlank @Size(max = 150) String empreendimento,
        @NotBlank @Size(max = 150) String requerente,
        @NotBlank @Size(max = 200) String endereco,
        @Size(max = 150) String engenheiroResponsavel,
        @NotNull @PastOrPresent LocalDate dataProtocolo,
        @Positive @Max(365) Integer prazoAnaliseDias,
        @Size(max = 1000) String observacoes
) {
}

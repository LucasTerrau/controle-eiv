package br.com.lucasterra.eiv.processo.dto;

import br.com.lucasterra.eiv.processo.StatusProcesso;
import jakarta.validation.constraints.NotNull;

public record AlteracaoStatusRequest(@NotNull StatusProcesso status) {
}

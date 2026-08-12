package com.mesaayuda.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketDerivarRequest(
        @NotNull Long areaDestinoId,
        @NotBlank String motivo) {
}

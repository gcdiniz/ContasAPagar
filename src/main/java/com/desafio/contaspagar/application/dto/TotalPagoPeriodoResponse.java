package com.desafio.contaspagar.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TotalPagoPeriodoResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal totalPago
) {
}

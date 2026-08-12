package com.mesaayuda.ticket.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EstadoTicketTest {

    @ParameterizedTest(name = "{0} puede pasar a {1}")
    @MethodSource("paresValidos")
    void puedeTransicionarADevuelveTrueParaLosParesValidos(EstadoTicket origen, EstadoTicket destino) {
        assertThat(origen.puedeTransicionarA(destino)).isTrue();
    }

    @Test
    void laSumaDeDestinosPermitidosEsDiez() {
        long total = Arrays.stream(EstadoTicket.values())
                .mapToLong(estado -> estado.destinosPermitidos().size())
                .sum();
        assertThat(total).isEqualTo(10);
    }

    static Stream<Arguments> paresValidos() {
        return Stream.of(
                Arguments.of(EstadoTicket.NUEVO, EstadoTicket.RESUELTO),
                Arguments.of(EstadoTicket.NUEVO, EstadoTicket.DERIVADO),
                Arguments.of(EstadoTicket.DERIVADO, EstadoTicket.EN_PROGRESO),
                Arguments.of(EstadoTicket.DERIVADO, EstadoTicket.DERIVADO),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.ESPERANDO_CLIENTE),
                Arguments.of(EstadoTicket.ESPERANDO_CLIENTE, EstadoTicket.EN_PROGRESO),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.RESUELTO),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.DERIVADO),
                Arguments.of(EstadoTicket.RESUELTO, EstadoTicket.CERRADO),
                Arguments.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO));
    }
}

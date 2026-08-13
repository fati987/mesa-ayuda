package com.mesaayuda.ticket;

/**
 * Publicado por TransicionService cuando una transición afecta una columna
 * visible del tablero de un área. El payload es intencionalmente mínimo: el
 * frontend no lo mergea a mano, solo invalida su query de react-query para
 * refetchear el estado real — ni ticketCodigo ni areaId son sensibles, ya
 * están visibles en el propio tablero REST.
 */
public record TableroActualizadoEvento(String ticketCodigo, Long areaId) {
}

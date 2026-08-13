import type { EstadoTicket } from '../api/types';

// Fuente de verdad del grafo de transiciones de estado (espejo de
// EstadoTicket + TransicionService en el backend). El frontend nunca debe
// ofrecer una transición fuera de este grafo, ni como drop de
// drag-and-drop ni como botón — aunque el backend igual la rechazaría con
// 400/409, no tiene sentido dejar que el usuario la intente.
const GRAFO: Record<EstadoTicket, EstadoTicket[]> = {
  NUEVO: ['RESUELTO', 'DERIVADO'],
  DERIVADO: ['EN_PROGRESO', 'DERIVADO'],
  EN_PROGRESO: ['ESPERANDO_CLIENTE', 'RESUELTO', 'DERIVADO'],
  ESPERANDO_CLIENTE: ['EN_PROGRESO'],
  RESUELTO: ['CERRADO'],
  CERRADO: ['EN_PROGRESO'],
};

export function destinosPermitidos(origen: EstadoTicket): EstadoTicket[] {
  return GRAFO[origen] ?? [];
}

export function esTransicionValida(origen: EstadoTicket, destino: EstadoTicket): boolean {
  return destinosPermitidos(origen).includes(destino);
}

// Las 4 columnas que existen físicamente en el tablero. NUEVO y CERRADO no
// tienen columna: los tickets resueltos en la misma llamada no ingresan al
// tablero (regla de negocio 10) y los NUEVO tampoco viven ahí.
export const COLUMNAS_TABLERO: EstadoTicket[] = [
  'DERIVADO',
  'EN_PROGRESO',
  'ESPERANDO_CLIENTE',
  'RESUELTO',
];

export function destinosPermitidosEnTablero(origen: EstadoTicket): EstadoTicket[] {
  return destinosPermitidos(origen).filter((estado) => COLUMNAS_TABLERO.includes(estado));
}

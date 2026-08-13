import type { EstadoTicket } from '../api/types';

const ETIQUETAS: Record<EstadoTicket, string> = {
  NUEVO: 'Nuevo',
  DERIVADO: 'Derivado',
  EN_PROGRESO: 'En progreso',
  ESPERANDO_CLIENTE: 'Esperando cliente',
  RESUELTO: 'Resuelto',
  CERRADO: 'Cerrado',
};

// Color por estado usando la paleta de estado reservada (nunca la paleta
// categórica de series de gráficos, para no mezclar semánticas).
const COLORES: Record<EstadoTicket, { color: string; fondo: string }> = {
  NUEVO: { color: 'var(--color-acento-hover)', fondo: 'var(--color-acento-fondo)' },
  DERIVADO: { color: 'var(--color-alerta)', fondo: 'var(--color-alerta-fondo)' },
  EN_PROGRESO: { color: 'var(--color-acento-hover)', fondo: 'var(--color-acento-fondo)' },
  ESPERANDO_CLIENTE: { color: 'var(--color-grave)', fondo: 'var(--color-grave-fondo)' },
  RESUELTO: { color: 'var(--color-bien)', fondo: 'var(--color-bien-fondo)' },
  CERRADO: { color: 'var(--color-texto-muted)', fondo: 'var(--color-superficie-hover)' },
};

export function EstadoBadge({ estado }: { estado: EstadoTicket }) {
  const { color, fondo } = COLORES[estado];
  return (
    <span className="badge" style={{ color, background: fondo }}>
      {ETIQUETAS[estado]}
    </span>
  );
}

import type { Prioridad } from '../api/types';

const ETIQUETAS: Record<Prioridad, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
};

const COLORES: Record<Prioridad, { color: string; fondo: string }> = {
  BAJA: { color: 'var(--color-bien)', fondo: 'var(--color-bien-fondo)' },
  MEDIA: { color: 'var(--color-acento-hover)', fondo: 'var(--color-acento-fondo)' },
  ALTA: { color: 'var(--color-grave)', fondo: 'var(--color-grave-fondo)' },
  CRITICA: { color: 'var(--color-critico)', fondo: 'var(--color-critico-fondo)' },
};

export function PrioridadBadge({ prioridad }: { prioridad: Prioridad }) {
  const { color, fondo } = COLORES[prioridad];
  return (
    <span className="badge" style={{ color, background: fondo }}>
      {ETIQUETAS[prioridad]}
    </span>
  );
}

import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface SlaIndicadorProps {
  fechaVencimiento: string | null;
  escalado: boolean;
}

// Comparación de reloj de pared contra `new Date()`: es una aproximación de
// UX para mostrar si el ticket luce vencido, no una reimplementación del
// cálculo real de horario hábil / calendario laboral que hace el backend.
export function SlaIndicador({ fechaVencimiento, escalado }: SlaIndicadorProps) {
  if (!fechaVencimiento) {
    return <span className="badge texto-muted" style={{ background: 'var(--color-superficie-hover)' }}>Sin SLA</span>;
  }

  const vencido = new Date(fechaVencimiento) < new Date();
  const fechaFormateada = format(new Date(fechaVencimiento), "dd/MM/yyyy HH:mm", { locale: es });

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
      <span
        className="badge"
        style={
          vencido
            ? { color: 'var(--color-critico)', background: 'var(--color-critico-fondo)' }
            : { color: 'var(--color-bien)', background: 'var(--color-bien-fondo)' }
        }
      >
        {vencido ? 'Vencido' : 'Al día'}
      </span>
      <span className="texto-secundario">Vence {fechaFormateada}</span>
      {escalado && (
        <span className="badge" style={{ color: 'var(--color-alerta)', background: 'var(--color-alerta-fondo)' }}>
          Escalado
        </span>
      )}
    </div>
  );
}

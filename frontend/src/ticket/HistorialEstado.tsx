import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { EstadoBadge } from '../shared/EstadoBadge';
import type { HistorialEstadoDto } from '../api/types';

export function HistorialEstado({ historial }: { historial: HistorialEstadoDto[] }) {
  if (historial.length === 0) {
    return <p className="texto-muted">Sin movimientos registrados.</p>;
  }

  return (
    <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 12 }}>
      {historial.map((item, indice) => (
        <li key={indice} style={{ borderLeft: '2px solid var(--color-borde)', paddingLeft: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            {item.estadoAnterior && <EstadoBadge estado={item.estadoAnterior} />}
            {item.estadoAnterior && <span className="texto-muted">→</span>}
            <EstadoBadge estado={item.estadoNuevo} />
            <span className="texto-muted" style={{ fontSize: 12 }}>
              {format(new Date(item.creadoEn), 'dd/MM/yyyy HH:mm', { locale: es })}
            </span>
          </div>
          <p className="texto-secundario" style={{ margin: '4px 0 0' }}>
            {item.usuarioNombre}
            {item.comentario && ` — ${item.comentario}`}
          </p>
        </li>
      ))}
    </ul>
  );
}

import { useDroppable } from '@dnd-kit/core';
import { TicketCard } from './TicketCard';
import type { ColumnaTableroDto, TicketResumenDto } from '../api/types';

interface ColumnaTableroProps {
  columna: ColumnaTableroDto;
  resaltada: boolean;
  limiteWip?: number;
  cargandoMas: boolean;
  onCargarMas: (columnaId: number) => void;
  onDerivarDeNuevo: (ticket: TicketResumenDto) => void;
}

export function ColumnaTablero({
  columna,
  resaltada,
  limiteWip,
  cargandoMas,
  onCargarMas,
  onDerivarDeNuevo,
}: ColumnaTableroProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: columna.estadoAsociado,
    data: { estadoAsociado: columna.estadoAsociado, columnaId: columna.id },
  });

  const hayMas = columna.tickets.pagina < columna.tickets.totalPaginas - 1;

  return (
    <div
      ref={setNodeRef}
      style={{
        background: resaltada ? 'var(--color-acento-fondo)' : 'var(--color-superficie-hover)',
        border: `1px dashed ${resaltada ? 'var(--color-acento)' : 'transparent'}`,
        borderRadius: 'var(--radio)',
        padding: 10,
        minWidth: 260,
        flex: 1,
        transition: 'background-color 0.15s, border-color 0.15s',
        outline: isOver && resaltada ? '2px solid var(--color-acento)' : 'none',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
        <h3 style={{ margin: 0, fontSize: 14 }}>{columna.nombre}</h3>
        <span className="badge" style={{ background: 'var(--color-superficie)' }}>
          {columna.tickets.totalElementos}
          {limiteWip !== undefined ? ` / ${limiteWip}` : ''}
        </span>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {columna.tickets.content.map((ticket) => (
          <TicketCard key={ticket.codigo} ticket={ticket} onDerivarDeNuevo={onDerivarDeNuevo} />
        ))}
        {columna.tickets.content.length === 0 && <p className="texto-muted" style={{ fontSize: 13 }}>Sin tickets</p>}
      </div>

      {hayMas && (
        <button
          type="button"
          className="boton boton-secundario boton-chico"
          style={{ marginTop: 10, width: '100%' }}
          disabled={cargandoMas}
          onClick={() => onCargarMas(columna.id)}
        >
          {cargandoMas ? 'Cargando…' : 'Cargar más'}
        </button>
      )}
    </div>
  );
}

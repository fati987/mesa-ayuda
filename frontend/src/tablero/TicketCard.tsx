import { useDraggable } from '@dnd-kit/core';
import { CSS } from '@dnd-kit/utilities';
import { Link } from 'react-router-dom';
import { PrioridadBadge } from '../shared/PrioridadBadge';
import type { TicketResumenDto } from '../api/types';

interface TicketCardProps {
  ticket: TicketResumenDto;
  onDerivarDeNuevo?: (ticket: TicketResumenDto) => void;
}

export function TicketCard({ ticket, onDerivarDeNuevo }: TicketCardProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: ticket.codigo,
    data: { estado: ticket.estado },
  });

  const estilo = {
    transform: CSS.Translate.toString(transform),
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div ref={setNodeRef} style={estilo} className="tarjeta" data-testid="ticket-card">
      <div {...listeners} {...attributes} style={{ cursor: 'grab' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 6 }}>
          <Link to={`/tickets/${ticket.codigo}`} onClick={(e) => e.stopPropagation()} style={{ fontWeight: 600 }}>
            {ticket.codigo}
          </Link>
          <PrioridadBadge prioridad={ticket.prioridad} />
        </div>
        <p style={{ margin: '6px 0', fontSize: 14 }}>{ticket.titulo}</p>
        <p className="texto-muted" style={{ fontSize: 12, margin: 0 }}>{ticket.contactoNombre}</p>
        <p className="texto-muted" style={{ fontSize: 12, margin: 0 }}>
          {ticket.usuarioAsignadoNombre ?? 'Sin asignar'}
        </p>
      </div>
      {ticket.estado === 'DERIVADO' && onDerivarDeNuevo && (
        <button
          type="button"
          className="boton boton-secundario boton-chico"
          style={{ marginTop: 8 }}
          onClick={() => onDerivarDeNuevo(ticket)}
        >
          Derivar de nuevo
        </button>
      )}
    </div>
  );
}

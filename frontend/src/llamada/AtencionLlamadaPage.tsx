import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FormularioContacto } from './FormularioContacto';
import { FormularioTicket } from './FormularioTicket';
import { AccionesCierre } from './AccionesCierre';
import { EstadoBadge } from '../shared/EstadoBadge';
import type { LlamadaDetalleDto, TicketDetalleDto } from '../api/types';

type Paso = 1 | 2 | 3 | 'resumen';

export function AtencionLlamadaPage() {
  const [paso, setPaso] = useState<Paso>(1);
  const [llamada, setLlamada] = useState<LlamadaDetalleDto | null>(null);
  const [areaSeleccionadaId, setAreaSeleccionadaId] = useState<number | null>(null);
  const [ticket, setTicket] = useState<TicketDetalleDto | null>(null);

  function reiniciar() {
    setPaso(1);
    setLlamada(null);
    setAreaSeleccionadaId(null);
    setTicket(null);
  }

  return (
    <div>
      <h1>Atención de llamada</h1>
      <p className="texto-secundario" style={{ marginBottom: 20 }}>
        Paso {paso === 'resumen' ? 3 : paso} de 3
      </p>

      {paso === 1 && (
        <FormularioContacto
          onCreada={(llamadaCreada) => {
            setLlamada(llamadaCreada);
            setPaso(2);
          }}
        />
      )}

      {paso === 2 && llamada && (
        <FormularioTicket
          llamadaId={llamada.id}
          onCreado={(ticketCreado, areaId) => {
            setTicket(ticketCreado);
            setAreaSeleccionadaId(areaId);
            setPaso(3);
          }}
        />
      )}

      {paso === 3 && ticket && areaSeleccionadaId !== null && (
        <AccionesCierre
          ticket={ticket}
          areaActualId={areaSeleccionadaId}
          onFinalizado={(ticketFinal) => {
            setTicket(ticketFinal);
            setPaso('resumen');
          }}
        />
      )}

      {paso === 'resumen' && ticket && (
        <div className="tarjeta">
          <h2>Atención finalizada</h2>
          <p>
            Ticket <Link to={`/tickets/${ticket.codigo}`}>{ticket.codigo}</Link> quedó en estado{' '}
            <EstadoBadge estado={ticket.estado} />
          </p>
          <button type="button" className="boton boton-primario" onClick={reiniciar} style={{ marginTop: 12 }}>
            Atender otra llamada
          </button>
        </div>
      )}
    </div>
  );
}

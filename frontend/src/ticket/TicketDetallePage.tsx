import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import {
  cerrarTicket,
  derivarTicket,
  obtenerTicket,
  ponerEnEsperaTicket,
  reabrirTicket,
  reanudarTicket,
  resolverTicket,
  tomarTicket,
} from './ticketApi';
import { HistorialEstado } from './HistorialEstado';
import { DerivacionesList } from './DerivacionesList';
import { ComentariosPanel } from './ComentariosPanel';
import { SlaIndicador } from './SlaIndicador';
import { EstadoBadge } from '../shared/EstadoBadge';
import { PrioridadBadge } from '../shared/PrioridadBadge';
import { destinosPermitidos } from '../tablero/estadoTicketGrafo';
import { ModalResolucion } from '../tablero/ModalResolucion';
import { ModalDerivacion } from '../tablero/ModalDerivacion';
import type { EstadoTicket, TicketDetalleDto } from '../api/types';

type Modal = { tipo: 'resolucion' | 'derivacion'; destino: EstadoTicket } | null;

export function TicketDetallePage() {
  const { codigo } = useParams<{ codigo: string }>();
  const queryClient = useQueryClient();
  const [modal, setModal] = useState<Modal>(null);

  const { data: ticket, isLoading } = useQuery({
    queryKey: ['ticket', codigo],
    queryFn: () => obtenerTicket(codigo!),
    enabled: !!codigo,
  });

  const mutacion = useMutation({
    mutationFn: (accion: () => Promise<TicketDetalleDto>) => accion(),
    onSuccess: (ticketActualizado) => {
      queryClient.setQueryData(['ticket', codigo], ticketActualizado);
      setModal(null);
    },
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!ticket || !codigo) return <p>No se encontró el ticket.</p>;

  function accionDirecta(fn: () => Promise<TicketDetalleDto>) {
    mutacion.mutate(fn);
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap', marginBottom: 4 }}>
        <h1 style={{ margin: 0 }}>{ticket.codigo}</h1>
        <EstadoBadge estado={ticket.estado} />
        <PrioridadBadge prioridad={ticket.prioridad} />
        {ticket.resueltoEnLlamada && (
          <span className="badge" style={{ color: 'var(--color-bien)', background: 'var(--color-bien-fondo)' }}>
            Resuelto en llamada
          </span>
        )}
      </div>
      <h2 style={{ fontWeight: 500, fontSize: 18, color: 'var(--color-texto-secundario)' }}>{ticket.titulo}</h2>

      <div className="tarjeta" style={{ marginBottom: 20 }}>
        <SlaIndicador fechaVencimiento={ticket.fechaVencimiento} escalado={ticket.escalado} />
      </div>

      <div className="fila-campos" style={{ marginBottom: 20 }}>
        <div className="tarjeta">
          <h3>Detalle</h3>
          <p><strong>Descripción:</strong> {ticket.descripcion}</p>
          <p><strong>Tipo:</strong> {ticket.tipo}</p>
          <p><strong>Urgencia:</strong> {ticket.urgencia}</p>
          <p><strong>Impacto:</strong> {ticket.impacto}</p>
          <p><strong>Origen:</strong> {ticket.origen}</p>
          <p><strong>Área actual:</strong> {ticket.areaActualNombre}</p>
          <p><strong>Categoría:</strong> {ticket.categoriaNombre}</p>
          <p><strong>Creado por:</strong> {ticket.usuarioCreadorNombre}</p>
          <p><strong>Asignado a:</strong> {ticket.usuarioAsignadoNombre ?? 'Sin asignar'}</p>
          <p><strong>Minutos pausado (SLA):</strong> {ticket.minutosPausado}</p>
          {ticket.solucion && <p><strong>Solución:</strong> {ticket.solucion}</p>}
          <p className="texto-muted" style={{ fontSize: 12 }}>
            Creado {format(new Date(ticket.creadoEn), 'dd/MM/yyyy HH:mm', { locale: es })} · Actualizado{' '}
            {format(new Date(ticket.actualizadoEn), 'dd/MM/yyyy HH:mm', { locale: es })}
          </p>
        </div>

        <div className="tarjeta">
          <h3>Contacto</h3>
          <p><strong>Nombre:</strong> {ticket.contacto.nombreCompleto}</p>
          <p><strong>Teléfono:</strong> {ticket.contacto.telefono}</p>
          <p><strong>Correo:</strong> {ticket.contacto.correo ?? '—'}</p>
          {ticket.llamada && (
            <>
              <h3 style={{ marginTop: 16 }}>Llamada</h3>
              <p><strong>Fecha:</strong> {format(new Date(ticket.llamada.fechaHora), 'dd/MM/yyyy HH:mm', { locale: es })}</p>
              <p><strong>Duración:</strong> {ticket.llamada.duracionSegundos} s</p>
              <p><strong>Atendió:</strong> {ticket.llamada.usuarioAtiendeNombre}</p>
            </>
          )}
        </div>
      </div>

      <div className="tarjeta" style={{ marginBottom: 20 }}>
        <h3>Transiciones disponibles</h3>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          {destinosPermitidos(ticket.estado).map((destino) => (
            <BotonTransicion
              key={destino}
              origen={ticket.estado}
              destino={destino}
              ticket={ticket}
              enviando={mutacion.isPending}
              onDirecta={accionDirecta}
              onAbrirModal={setModal}
            />
          ))}
        </div>
      </div>

      <div className="fila-campos" style={{ marginBottom: 20 }}>
        <div className="tarjeta">
          <h3>Historial de estado</h3>
          <HistorialEstado historial={ticket.historial} />
        </div>
        <div className="tarjeta">
          <h3>Derivaciones</h3>
          <DerivacionesList derivaciones={ticket.derivaciones} />
        </div>
      </div>

      <div className="tarjeta">
        <h3>Comentarios</h3>
        <ComentariosPanel codigo={codigo} />
      </div>

      {modal?.tipo === 'resolucion' && (
        <ModalResolucion
          codigoTicket={ticket.codigo}
          solucionInicial={ticket.solucion ?? ''}
          enviando={mutacion.isPending}
          onCancelar={() => setModal(null)}
          onConfirmar={(solucion) =>
            mutacion.mutate(() => resolverTicket(ticket.codigo, { solucion: solucion || undefined }))
          }
        />
      )}
      {modal?.tipo === 'derivacion' && (
        <ModalDerivacion
          codigoTicket={ticket.codigo}
          areaActualNombre={ticket.areaActualNombre}
          enviando={mutacion.isPending}
          onCancelar={() => setModal(null)}
          onConfirmar={(areaDestinoId, motivo) =>
            mutacion.mutate(() => derivarTicket(ticket.codigo, { areaDestinoId, motivo }))
          }
        />
      )}
    </div>
  );
}

interface BotonTransicionProps {
  origen: EstadoTicket;
  destino: EstadoTicket;
  ticket: TicketDetalleDto;
  enviando: boolean;
  onDirecta: (fn: () => Promise<TicketDetalleDto>) => void;
  onAbrirModal: (modal: Modal) => void;
}

function BotonTransicion({ origen, destino, ticket, enviando, onDirecta, onAbrirModal }: BotonTransicionProps) {
  const codigo = ticket.codigo;

  if (origen === 'NUEVO' && destino === 'RESUELTO') {
    const habilitado = ticket.resueltoEnLlamada;
    return (
      <button
        type="button"
        className="boton boton-primario"
        disabled={!habilitado || enviando}
        title={habilitado ? undefined : 'Solo se puede resolver en la llamada si se marcó como resuelto al crear el ticket'}
        onClick={() => onDirecta(() => resolverTicket(codigo, { solucion: ticket.solucion ?? undefined }))}
      >
        Resolver
      </button>
    );
  }
  if (destino === 'RESUELTO') {
    return (
      <button
        type="button"
        className="boton boton-primario"
        disabled={enviando}
        onClick={() => onAbrirModal({ tipo: 'resolucion', destino })}
      >
        Resolver
      </button>
    );
  }
  if (destino === 'DERIVADO') {
    return (
      <button
        type="button"
        className="boton boton-secundario"
        disabled={enviando}
        onClick={() => onAbrirModal({ tipo: 'derivacion', destino })}
      >
        {origen === 'DERIVADO' ? 'Derivar de nuevo' : 'Derivar'}
      </button>
    );
  }
  if (origen === 'DERIVADO' && destino === 'EN_PROGRESO') {
    return (
      <button type="button" className="boton boton-primario" disabled={enviando} onClick={() => onDirecta(() => tomarTicket(codigo))}>
        Tomar ticket
      </button>
    );
  }
  if (origen === 'ESPERANDO_CLIENTE' && destino === 'EN_PROGRESO') {
    return (
      <button type="button" className="boton boton-primario" disabled={enviando} onClick={() => onDirecta(() => reanudarTicket(codigo))}>
        Reanudar
      </button>
    );
  }
  if (origen === 'CERRADO' && destino === 'EN_PROGRESO') {
    return (
      <button type="button" className="boton boton-secundario" disabled={enviando} onClick={() => onDirecta(() => reabrirTicket(codigo))}>
        Reabrir
      </button>
    );
  }
  if (origen === 'EN_PROGRESO' && destino === 'ESPERANDO_CLIENTE') {
    return (
      <button type="button" className="boton boton-secundario" disabled={enviando} onClick={() => onDirecta(() => ponerEnEsperaTicket(codigo))}>
        Poner en espera
      </button>
    );
  }
  if (origen === 'RESUELTO' && destino === 'CERRADO') {
    return (
      <button type="button" className="boton boton-secundario" disabled={enviando} onClick={() => onDirecta(() => cerrarTicket(codigo))}>
        Cerrar
      </button>
    );
  }
  return null;
}

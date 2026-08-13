import { useState } from 'react';
import { DndContext } from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth/AuthContext';
import { useAreas } from '../area/areaApi';
import { obtenerTablero, obtenerTicketsColumna } from './tableroApi';
import { ColumnaTablero } from './ColumnaTablero';
import { ModalResolucion } from './ModalResolucion';
import { ModalDerivacion } from './ModalDerivacion';
import { destinosPermitidosEnTablero } from './estadoTicketGrafo';
import { ponerEnEsperaTicket, reanudarTicket, resolverTicket, tomarTicket, derivarTicket } from '../ticket/ticketApi';
import type { EstadoTicket, TableroDto, TicketDetalleDto, TicketResumenDto } from '../api/types';

type ModalPendiente =
  | { tipo: 'resolucion'; codigo: string }
  | { tipo: 'derivacion'; codigo: string; areaActualNombre: string }
  | null;

function moverTicketEnCache(
  tablero: TableroDto | undefined,
  codigo: string,
  destino: EstadoTicket,
): TableroDto | undefined {
  if (!tablero) return tablero;

  let ticketMovido: TicketResumenDto | undefined;
  const sinTicket = tablero.columnas.map((columna) => {
    const encontrado = columna.tickets.content.find((t) => t.codigo === codigo);
    if (!encontrado) return columna;
    ticketMovido = encontrado;
    return {
      ...columna,
      tickets: {
        ...columna.tickets,
        content: columna.tickets.content.filter((t) => t.codigo !== codigo),
        totalElementos: Math.max(0, columna.tickets.totalElementos - 1),
      },
    };
  });

  if (!ticketMovido) return tablero;
  const ticketActualizado: TicketResumenDto = { ...ticketMovido, estado: destino };

  const conTicket = sinTicket.map((columna) => {
    if (columna.estadoAsociado !== destino) return columna;
    return {
      ...columna,
      tickets: {
        ...columna.tickets,
        content: [ticketActualizado, ...columna.tickets.content],
        totalElementos: columna.tickets.totalElementos + 1,
      },
    };
  });

  return { ...tablero, columnas: conTicket };
}

export function TableroPage() {
  const { usuario } = useAuth();
  const queryClient = useQueryClient();
  const { data: paginaAreas } = useAreas();

  const esAgente = usuario?.rol === 'AGENTE';
  const [areaSeleccionada, setAreaSeleccionada] = useState<number | null>(
    !esAgente && usuario ? usuario.areaId : null,
  );
  const [origenActivo, setOrigenActivo] = useState<EstadoTicket | null>(null);
  const [modal, setModal] = useState<ModalPendiente>(null);
  const [columnaCargandoMas, setColumnaCargandoMas] = useState<number | null>(null);

  const queryKey = ['tablero', esAgente ? 'propia' : areaSeleccionada] as const;

  const { data: tablero, isLoading } = useQuery({
    queryKey,
    queryFn: () => obtenerTablero(esAgente ? undefined : areaSeleccionada ?? undefined),
    enabled: esAgente || areaSeleccionada !== null,
  });

  const areaActual = paginaAreas?.content.find((a) => a.id === tablero?.areaId);

  const mutacionMover = useMutation({
    mutationFn: (variables: { codigo: string; destino: EstadoTicket; accion: () => Promise<TicketDetalleDto> }) =>
      variables.accion(),
    onMutate: async (variables) => {
      await queryClient.cancelQueries({ queryKey });
      const previo = queryClient.getQueryData<TableroDto>(queryKey);
      queryClient.setQueryData<TableroDto>(queryKey, (old) =>
        moverTicketEnCache(old, variables.codigo, variables.destino),
      );
      return { previo };
    },
    onError: (_err, _variables, context) => {
      // El toast con el mensaje real del backend ya lo publica el
      // interceptor de httpClient; acá solo revertimos la tarjeta.
      if (context?.previo) {
        queryClient.setQueryData(queryKey, context.previo);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey });
    },
  });

  function handleDragStart(event: DragStartEvent) {
    const estado = event.active.data.current?.estado as EstadoTicket | undefined;
    setOrigenActivo(estado ?? null);
  }

  function handleDragEnd(event: DragEndEvent) {
    setOrigenActivo(null);
    const { active, over } = event;
    if (!over) return;

    const codigo = String(active.id);
    const origen = active.data.current?.estado as EstadoTicket | undefined;
    const destino = over.data.current?.estadoAsociado as EstadoTicket | undefined;
    if (!origen || !destino || origen === destino) return;
    if (!destinosPermitidosEnTablero(origen).includes(destino)) return;

    if (destino === 'RESUELTO') {
      setModal({ tipo: 'resolucion', codigo });
      return;
    }
    if (destino === 'DERIVADO') {
      const ticket = buscarTicket(tablero, codigo);
      if (!ticket) return;
      setModal({ tipo: 'derivacion', codigo, areaActualNombre: ticket.areaActualNombre });
      return;
    }
    if (destino === 'EN_PROGRESO' && origen === 'DERIVADO') {
      mutacionMover.mutate({ codigo, destino, accion: () => tomarTicket(codigo) });
      return;
    }
    if (destino === 'EN_PROGRESO' && origen === 'ESPERANDO_CLIENTE') {
      mutacionMover.mutate({ codigo, destino, accion: () => reanudarTicket(codigo) });
      return;
    }
    if (destino === 'ESPERANDO_CLIENTE' && origen === 'EN_PROGRESO') {
      mutacionMover.mutate({ codigo, destino, accion: () => ponerEnEsperaTicket(codigo) });
      return;
    }
  }

  function handleDerivarDeNuevo(ticket: TicketResumenDto) {
    setModal({ tipo: 'derivacion', codigo: ticket.codigo, areaActualNombre: ticket.areaActualNombre });
  }

  async function handleCargarMas(columnaId: number) {
    if (!tablero) return;
    const columna = tablero.columnas.find((c) => c.id === columnaId);
    if (!columna) return;
    setColumnaCargandoMas(columnaId);
    try {
      const siguiente = await obtenerTicketsColumna(columnaId, columna.tickets.pagina + 1, columna.tickets.tamano);
      queryClient.setQueryData<TableroDto>(queryKey, (old) => {
        if (!old) return old;
        return {
          ...old,
          columnas: old.columnas.map((c) =>
            c.id === columnaId
              ? { ...c, tickets: { ...siguiente, content: [...c.tickets.content, ...siguiente.content] } }
              : c,
          ),
        };
      });
    } finally {
      setColumnaCargandoMas(null);
    }
  }

  // Para SUPERVISOR/ADMIN el backend exige elegir un área explícitamente
  // (409 si no se envía `area`); para AGENTE el backend fuerza su propia
  // área sin importar lo que se envíe.

  const destinosResaltados = origenActivo
    ? destinosPermitidosEnTablero(origenActivo).filter((d) => d !== origenActivo)
    : [];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 10 }}>
        <h1 style={{ margin: 0 }}>Tablero{tablero ? ` — ${tablero.areaNombre}` : ''}</h1>
        {!esAgente && (
          <div style={{ minWidth: 220 }}>
            <select
              value={areaSeleccionada ?? ''}
              onChange={(e) => setAreaSeleccionada(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">Seleccioná un área…</option>
              {paginaAreas?.content.map((area) => (
                <option key={area.id} value={area.id}>
                  {area.nombre}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {!esAgente && areaSeleccionada === null && <p className="texto-muted">Elegí un área para ver su tablero.</p>}
      {isLoading && (esAgente || areaSeleccionada !== null) && <div className="pagina-cargando">Cargando…</div>}

      {tablero && (
        <DndContext onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
          <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start', overflowX: 'auto' }}>
            {tablero.columnas.map((columna) => (
              <ColumnaTablero
                key={columna.id}
                columna={columna}
                resaltada={destinosResaltados.includes(columna.estadoAsociado)}
                limiteWip={columna.estadoAsociado === 'EN_PROGRESO' ? areaActual?.limiteWipAgente : undefined}
                cargandoMas={columnaCargandoMas === columna.id}
                onCargarMas={handleCargarMas}
                onDerivarDeNuevo={handleDerivarDeNuevo}
              />
            ))}
          </div>
        </DndContext>
      )}

      {modal?.tipo === 'resolucion' && (
        <ModalResolucion
          codigoTicket={modal.codigo}
          enviando={mutacionMover.isPending}
          onCancelar={() => setModal(null)}
          onConfirmar={(solucion) => {
            const codigo = modal.codigo;
            mutacionMover.mutate(
              { codigo, destino: 'RESUELTO', accion: () => resolverTicket(codigo, { solucion: solucion || undefined }) },
              { onSuccess: () => setModal(null) },
            );
          }}
        />
      )}
      {modal?.tipo === 'derivacion' && (
        <ModalDerivacion
          codigoTicket={modal.codigo}
          areaActualNombre={modal.areaActualNombre}
          enviando={mutacionMover.isPending}
          onCancelar={() => setModal(null)}
          onConfirmar={(areaDestinoId, motivo) => {
            const codigo = modal.codigo;
            mutacionMover.mutate(
              { codigo, destino: 'DERIVADO', accion: () => derivarTicket(codigo, { areaDestinoId, motivo }) },
              { onSuccess: () => setModal(null) },
            );
          }}
        />
      )}
    </div>
  );
}

function buscarTicket(tablero: TableroDto | undefined, codigo: string): TicketResumenDto | undefined {
  if (!tablero) return undefined;
  for (const columna of tablero.columnas) {
    const encontrado = columna.tickets.content.find((t) => t.codigo === codigo);
    if (encontrado) return encontrado;
  }
  return undefined;
}

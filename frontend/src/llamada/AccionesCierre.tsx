import { useState } from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { useAreas } from '../area/areaApi';
import { derivarTicket, resolverTicket } from '../ticket/ticketApi';
import type { TicketDetalleDto } from '../api/types';

interface AccionesCierreProps {
  ticket: TicketDetalleDto;
  areaActualId: number;
  onFinalizado: (ticketFinal: TicketDetalleDto) => void;
}

interface FormDerivacion {
  areaDestinoId: string;
  motivo: string;
}

export function AccionesCierre({ ticket, areaActualId, onFinalizado }: AccionesCierreProps) {
  const { data: paginaAreas } = useAreas();
  const [mostrarDerivacion, setMostrarDerivacion] = useState(false);
  const [resolviendo, setResolviendo] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const areasDestino = (paginaAreas?.content ?? []).filter((area) => area.id !== areaActualId);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormDerivacion>();

  const puedeResolver = ticket.resueltoEnLlamada;

  async function handleResolver() {
    setError(null);
    setResolviendo(true);
    try {
      const actualizado = await resolverTicket(ticket.codigo, {
        solucion: ticket.solucion ?? undefined,
      });
      onFinalizado(actualizado);
    } catch {
      setError('No se pudo resolver el ticket.');
    } finally {
      setResolviendo(false);
    }
  }

  const onSubmitDerivacion: SubmitHandler<FormDerivacion> = async (values) => {
    setError(null);
    try {
      const actualizado = await derivarTicket(ticket.codigo, {
        areaDestinoId: Number(values.areaDestinoId),
        motivo: values.motivo.trim(),
      });
      onFinalizado(actualizado);
    } catch {
      setError('No se pudo derivar el ticket.');
    }
  };

  return (
    <div className="tarjeta">
      <h2>3. Cerrar la atención</h2>
      <p className="texto-secundario">
        Ticket <strong>{ticket.codigo}</strong> — {ticket.titulo}
      </p>

      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginTop: 16 }}>
        <button
          type="button"
          className="boton boton-primario"
          disabled={!puedeResolver || resolviendo}
          title={
            puedeResolver
              ? undefined
              : 'Solo se puede resolver en la llamada si se marcó como resuelto al crear el ticket'
          }
          onClick={handleResolver}
        >
          {resolviendo ? 'Resolviendo…' : 'Resolver'}
        </button>
        <button
          type="button"
          className="boton boton-secundario"
          onClick={() => setMostrarDerivacion((valor) => !valor)}
        >
          Derivar
        </button>
      </div>

      {mostrarDerivacion && (
        <form className="campo" style={{ marginTop: 20 }} onSubmit={handleSubmit(onSubmitDerivacion)} noValidate>
          <div className="campo">
            <label htmlFor="areaDestinoId">Área destino</label>
            <select id="areaDestinoId" {...register('areaDestinoId', { required: true })}>
              <option value="">Seleccioná un área…</option>
              {areasDestino.map((area) => (
                <option key={area.id} value={area.id}>
                  {area.nombre}
                </option>
              ))}
            </select>
            {errors.areaDestinoId && <span className="texto-secundario">El área destino es obligatoria</span>}
          </div>
          <div className="campo">
            <label htmlFor="motivo">Motivo</label>
            <textarea id="motivo" {...register('motivo', { required: 'El motivo es obligatorio' })} />
            {errors.motivo && <span className="texto-secundario">{errors.motivo.message}</span>}
          </div>
          <button type="submit" className="boton boton-primario" disabled={isSubmitting}>
            {isSubmitting ? 'Derivando…' : 'Confirmar derivación'}
          </button>
        </form>
      )}

      {error && <p style={{ color: 'var(--color-critico)', marginTop: 12 }}>{error}</p>}
    </div>
  );
}

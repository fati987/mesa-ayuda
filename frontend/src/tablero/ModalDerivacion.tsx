import { useState } from 'react';
import { useAreas } from '../area/areaApi';

interface ModalDerivacionProps {
  codigoTicket: string;
  // TicketResumenDto y TicketDetalleDto solo exponen el nombre del área
  // actual, no su id, así que la exclusión del área actual en el selector
  // se hace comparando por nombre.
  areaActualNombre: string;
  onConfirmar: (areaDestinoId: number, motivo: string) => void;
  onCancelar: () => void;
  enviando?: boolean;
}

export function ModalDerivacion({
  codigoTicket,
  areaActualNombre,
  onConfirmar,
  onCancelar,
  enviando,
}: ModalDerivacionProps) {
  const { data: paginaAreas, isLoading } = useAreas();
  const [areaDestinoId, setAreaDestinoId] = useState('');
  const [motivo, setMotivo] = useState('');

  const areasDestino = (paginaAreas?.content ?? []).filter((area) => area.nombre !== areaActualNombre);
  const puedeConfirmar = areaDestinoId !== '' && motivo.trim() !== '';

  return (
    <div className="modal-fondo" onClick={onCancelar}>
      <div className="modal-caja" onClick={(e) => e.stopPropagation()}>
        <h2>Derivar {codigoTicket}</h2>
        <div className="campo">
          <label htmlFor="area-destino-modal">Área destino</label>
          <select
            id="area-destino-modal"
            value={areaDestinoId}
            onChange={(e) => setAreaDestinoId(e.target.value)}
            disabled={isLoading}
          >
            <option value="">Selecciona un área…</option>
            {areasDestino.map((area) => (
              <option key={area.id} value={area.id}>
                {area.nombre}
              </option>
            ))}
          </select>
        </div>
        <div className="campo">
          <label htmlFor="motivo-modal">Motivo</label>
          <textarea
            id="motivo-modal"
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            placeholder="Por qué se deriva el ticket"
          />
        </div>
        <div className="modal-acciones">
          <button type="button" className="boton boton-secundario" onClick={onCancelar} disabled={enviando}>
            Cancelar
          </button>
          <button
            type="button"
            className="boton boton-primario"
            onClick={() => onConfirmar(Number(areaDestinoId), motivo.trim())}
            disabled={enviando || !puedeConfirmar}
          >
            {enviando ? 'Confirmando…' : 'Confirmar derivación'}
          </button>
        </div>
      </div>
    </div>
  );
}

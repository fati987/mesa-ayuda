import { useState } from 'react';

interface ModalResolucionProps {
  codigoTicket: string;
  onConfirmar: (solucion: string) => void;
  onCancelar: () => void;
  enviando?: boolean;
  solucionInicial?: string;
}

export function ModalResolucion({
  codigoTicket,
  onConfirmar,
  onCancelar,
  enviando,
  solucionInicial,
}: ModalResolucionProps) {
  const [solucion, setSolucion] = useState(solucionInicial ?? '');

  return (
    <div className="modal-fondo" onClick={onCancelar}>
      <div className="modal-caja" onClick={(e) => e.stopPropagation()}>
        <h2>Resolver {codigoTicket}</h2>
        <div className="campo">
          <label htmlFor="solucion-modal">Solución</label>
          <textarea
            id="solucion-modal"
            value={solucion}
            onChange={(e) => setSolucion(e.target.value)}
            placeholder="Describe cómo se resolvió el ticket"
            autoFocus
          />
        </div>
        <div className="modal-acciones">
          <button type="button" className="boton boton-secundario" onClick={onCancelar} disabled={enviando}>
            Cancelar
          </button>
          <button
            type="button"
            className="boton boton-primario"
            onClick={() => onConfirmar(solucion.trim())}
            disabled={enviando}
          >
            {enviando ? 'Confirmando…' : 'Confirmar resolución'}
          </button>
        </div>
      </div>
    </div>
  );
}

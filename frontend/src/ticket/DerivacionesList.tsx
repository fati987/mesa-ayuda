import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import type { DerivacionDto } from '../api/types';

export function DerivacionesList({ derivaciones }: { derivaciones: DerivacionDto[] }) {
  if (derivaciones.length === 0) {
    return <p className="texto-muted">Este ticket no fue derivado.</p>;
  }

  return (
    <div className="tabla-envoltorio">
      <table>
        <thead>
          <tr>
            <th>Origen</th>
            <th>Destino</th>
            <th>Motivo</th>
            <th>Derivó</th>
            <th>Fecha</th>
          </tr>
        </thead>
        <tbody>
          {derivaciones.map((derivacion, indice) => (
            <tr key={indice}>
              <td>{derivacion.areaOrigenNombre}</td>
              <td>{derivacion.areaDestinoNombre}</td>
              <td>{derivacion.motivo}</td>
              <td>{derivacion.usuarioDerivaNombre}</td>
              <td>{format(new Date(derivacion.creadoEn), 'dd/MM/yyyy HH:mm', { locale: es })}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

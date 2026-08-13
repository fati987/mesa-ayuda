import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { obtenerRebotes, obtenerTasaDerivacion } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoNumero, formatoPorcentaje } from '../../shared/formato';
import { Pagination } from '../../shared/Pagination';

export function TasaDerivacionTab({ rango }: { rango: RangoFechas }) {
  const [pagina, setPagina] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'tasa-derivacion', rango],
    queryFn: () => obtenerTasaDerivacion(rango),
  });

  const { data: rebotes } = useQuery({
    queryKey: ['metricas', 'tasa-derivacion', 'rebotes', rango, pagina],
    queryFn: () => obtenerRebotes(rango, pagina, 10),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  return (
    <div>
      <div className="kpi-grid">
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Tickets creados</div>
          <div className="kpi-valor">{formatoNumero(data.totalCreados)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Derivados al menos 1 vez</div>
          <div className="kpi-valor">{formatoNumero(data.derivadosAlMenosUnaVez)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Tasa de derivación</div>
          <div className="kpi-valor">{formatoPorcentaje(data.tasaDerivacion)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Tickets rebotados</div>
          <div className="kpi-valor">{formatoNumero(data.ticketsRebotados)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Tasa de rebote</div>
          <div className="kpi-valor">{formatoPorcentaje(data.tasaRebote)}</div>
        </div>
      </div>

      <div className="tarjeta" style={{ marginBottom: 20 }}>
        <h3>Por área</h3>
        <div className="tabla-envoltorio">
          <table>
            <thead>
              <tr>
                <th>Área</th>
                <th>Creados</th>
                <th>Derivados</th>
                <th>Tasa de derivación</th>
              </tr>
            </thead>
            <tbody>
              {data.porArea.map((fila) => (
                <tr key={fila.areaId}>
                  <td>{fila.areaNombre}</td>
                  <td>{formatoNumero(fila.totalCreados)}</td>
                  <td>{formatoNumero(fila.derivadosAlMenosUnaVez)}</td>
                  <td>{formatoPorcentaje(fila.tasaDerivacion)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="tarjeta">
        <h3>Tickets más rebotados</h3>
        <div className="tabla-envoltorio">
          <table>
            <thead>
              <tr>
                <th>Código</th>
                <th>Área actual</th>
                <th>Cantidad de derivaciones</th>
              </tr>
            </thead>
            <tbody>
              {rebotes?.content.map((fila) => (
                <tr key={fila.codigo}>
                  <td>{fila.codigo}</td>
                  <td>{fila.areaActualNombre}</td>
                  <td>{fila.cantidadDerivaciones}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {rebotes && (
          <Pagination
            pagina={rebotes.pagina}
            totalPaginas={rebotes.totalPaginas}
            totalElementos={rebotes.totalElementos}
            onCambiarPagina={setPagina}
          />
        )}
      </div>
    </div>
  );
}

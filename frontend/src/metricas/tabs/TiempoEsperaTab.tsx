import { useQuery } from '@tanstack/react-query';
import { obtenerTiempoEspera } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoMinutos, formatoNumero } from '../../shared/formato';

export function TiempoEsperaTab({ rango }: { rango: RangoFechas }) {
  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'tiempo-espera', rango],
    queryFn: () => obtenerTiempoEspera(rango),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  return (
    <div>
      <div className="kpi-grid">
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Tomas consideradas</div>
          <div className="kpi-valor">{formatoNumero(data.tomasConsideradas)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Espera promedio</div>
          <div className="kpi-valor">{formatoMinutos(data.esperaPromedioMinutos)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Espera mediana</div>
          <div className="kpi-valor">{formatoMinutos(data.esperaMedianaMinutos)}</div>
        </div>
      </div>

      <div className="tarjeta">
        <h3>Por área</h3>
        <div className="tabla-envoltorio">
          <table>
            <thead>
              <tr>
                <th>Área</th>
                <th>Tomas consideradas</th>
                <th>Espera promedio</th>
              </tr>
            </thead>
            <tbody>
              {data.porArea.map((fila) => (
                <tr key={fila.areaId}>
                  <td>{fila.areaNombre}</td>
                  <td>{formatoNumero(fila.tomasConsideradas)}</td>
                  <td>{formatoMinutos(fila.esperaPromedioMinutos)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

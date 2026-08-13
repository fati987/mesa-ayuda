import { useQuery } from '@tanstack/react-query';
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { obtenerFcr } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoNumero, formatoPorcentaje } from '../../shared/formato';

export function FcrTab({ rango }: { rango: RangoFechas }) {
  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'fcr', rango],
    queryFn: () => obtenerFcr(rango),
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
          <div className="kpi-etiqueta">Resueltos en llamada</div>
          <div className="kpi-valor">{formatoNumero(data.resueltosEnLlamada)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">% FCR</div>
          <div className="kpi-valor">{formatoPorcentaje(data.porcentajeFcr)}</div>
        </div>
      </div>

      <div className="tarjeta" style={{ marginBottom: 20 }}>
        <h3>FCR por día</h3>
        <div style={{ width: '100%', height: 280 }}>
          <ResponsiveContainer>
            <LineChart data={data.porDia} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid stroke="var(--color-borde)" vertical={false} />
              <XAxis dataKey="dia" tick={{ fill: 'var(--color-texto-muted)', fontSize: 12 }} />
              <YAxis
                unit="%"
                tick={{ fill: 'var(--color-texto-muted)', fontSize: 12 }}
                domain={[0, 100]}
              />
              <Tooltip
                contentStyle={{ background: 'var(--color-superficie)', border: '1px solid var(--color-borde)', borderRadius: 8 }}
                formatter={(valor) => [`${Number(valor).toFixed(1)}%`, '% FCR']}
              />
              <Line
                type="monotone"
                dataKey="porcentajeFcr"
                name="% FCR"
                stroke="var(--serie-1)"
                strokeWidth={2}
                dot={{ r: 3 }}
                connectNulls
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="tarjeta">
        <h3>Por área</h3>
        <div className="tabla-envoltorio">
          <table>
            <thead>
              <tr>
                <th>Área</th>
                <th>Creados</th>
                <th>Resueltos en llamada</th>
                <th>% FCR</th>
              </tr>
            </thead>
            <tbody>
              {data.porArea.map((fila) => (
                <tr key={fila.areaId}>
                  <td>{fila.areaNombre}</td>
                  <td>{formatoNumero(fila.totalCreados)}</td>
                  <td>{formatoNumero(fila.resueltosEnLlamada)}</td>
                  <td>{formatoPorcentaje(fila.porcentajeFcr)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

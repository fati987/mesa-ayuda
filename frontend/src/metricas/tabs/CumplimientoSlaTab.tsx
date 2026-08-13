import { useQuery } from '@tanstack/react-query';
import { obtenerCumplimientoSla } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoNumero, formatoPorcentaje } from '../../shared/formato';
import type { DimensionDto } from '../../api/types';

function BarraCumplimiento({ dimension }: { dimension: DimensionDto }) {
  const total = dimension.total || 1;
  const pctCumplidos = (dimension.cumplidos / total) * 100;
  const pctIncumplidos = (dimension.incumplidos / total) * 100;
  const pctEnCurso = (dimension.enCurso / total) * 100;

  return (
    <div style={{ display: 'flex', height: 8, borderRadius: 999, overflow: 'hidden', background: 'var(--color-borde)', minWidth: 100 }}>
      <div style={{ width: `${pctCumplidos}%`, background: 'var(--color-bien)' }} title={`Cumplidos: ${dimension.cumplidos}`} />
      <div style={{ width: `${pctIncumplidos}%`, background: 'var(--color-critico)' }} title={`Incumplidos: ${dimension.incumplidos}`} />
      <div style={{ width: `${pctEnCurso}%`, background: 'var(--color-texto-muted)' }} title={`En curso: ${dimension.enCurso}`} />
    </div>
  );
}

function TablaDimension({ titulo, filas }: { titulo: string; filas: DimensionDto[] }) {
  return (
    <div className="tarjeta" style={{ marginBottom: 20 }}>
      <h3>{titulo}</h3>
      <div className="tabla-envoltorio">
        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Total</th>
              <th>Cumplidos</th>
              <th>Incumplidos</th>
              <th>En curso</th>
              <th>% Cumplimiento</th>
              <th>Distribución</th>
            </tr>
          </thead>
          <tbody>
            {filas.map((fila) => (
              <tr key={fila.nombre}>
                <td>{fila.nombre}</td>
                <td>{formatoNumero(fila.total)}</td>
                <td>{formatoNumero(fila.cumplidos)}</td>
                <td>{formatoNumero(fila.incumplidos)}</td>
                <td>{formatoNumero(fila.enCurso)}</td>
                <td>{formatoPorcentaje(fila.porcentajeCumplimiento)}</td>
                <td style={{ minWidth: 120 }}><BarraCumplimiento dimension={fila} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export function CumplimientoSlaTab({ rango }: { rango: RangoFechas }) {
  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'cumplimiento-sla', rango],
    queryFn: () => obtenerCumplimientoSla(rango),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  return (
    <div>
      <div className="kpi-grid">
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Total</div>
          <div className="kpi-valor">{formatoNumero(data.total)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Cumplidos</div>
          <div className="kpi-valor" style={{ color: 'var(--color-bien)' }}>{formatoNumero(data.cumplidos)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Incumplidos</div>
          <div className="kpi-valor" style={{ color: 'var(--color-critico)' }}>{formatoNumero(data.incumplidos)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">En curso</div>
          <div className="kpi-valor">{formatoNumero(data.enCurso)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">% Cumplimiento</div>
          <div className="kpi-valor">{formatoPorcentaje(data.porcentajeCumplimiento)}</div>
        </div>
      </div>

      <TablaDimension titulo="Por categoría" filas={data.porCategoria} />
      <TablaDimension titulo="Por área" filas={data.porArea} />
      <TablaDimension titulo="Por prioridad" filas={data.porPrioridad} />
    </div>
  );
}

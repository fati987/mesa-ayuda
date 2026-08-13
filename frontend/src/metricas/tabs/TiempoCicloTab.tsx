import { useQuery } from '@tanstack/react-query';
import { obtenerTiempoCiclo } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoMinutos, formatoNumero } from '../../shared/formato';

export function TiempoCicloTab({ rango }: { rango: RangoFechas }) {
  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'tiempo-ciclo', rango],
    queryFn: () => obtenerTiempoCiclo(rango),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  return (
    <div>
      <div className="tarjeta" style={{ marginBottom: 20 }}>
        <h3>Lead time (creación → cierre)</h3>
        <p className="texto-muted" style={{ marginBottom: 12 }}>
          {formatoNumero(data.ticketsLeadTime)} tickets considerados
        </p>
        <div className="kpi-grid">
          <div className="kpi-tile">
            <div className="kpi-etiqueta">Promedio</div>
            <div className="kpi-valor">{formatoMinutos(data.leadTimePromedioMinutos)}</div>
          </div>
          <div className="kpi-tile">
            <div className="kpi-etiqueta">Mediana</div>
            <div className="kpi-valor">{formatoMinutos(data.leadTimeMedianoMinutos)}</div>
          </div>
        </div>
      </div>

      <div className="tarjeta">
        <h3>Cycle time (derivación → resolución)</h3>
        <p className="texto-muted" style={{ marginBottom: 12 }}>
          {formatoNumero(data.ticketsCycleTime)} tickets considerados
        </p>
        <div className="kpi-grid">
          <div className="kpi-tile">
            <div className="kpi-etiqueta">Promedio</div>
            <div className="kpi-valor">{formatoMinutos(data.cycleTimePromedioMinutos)}</div>
          </div>
          <div className="kpi-tile">
            <div className="kpi-etiqueta">Mediana</div>
            <div className="kpi-valor">{formatoMinutos(data.cycleTimeMedianoMinutos)}</div>
          </div>
        </div>
      </div>
    </div>
  );
}

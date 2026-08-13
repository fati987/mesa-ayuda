import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { obtenerLlamadasMultiplesTickets, obtenerMetricasLlamadas } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import { formatoNumero, formatoPorcentaje } from '../../shared/formato';
import { Pagination } from '../../shared/Pagination';

export function LlamadasTab({ rango }: { rango: RangoFechas }) {
  const [pagina, setPagina] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'llamadas', rango],
    queryFn: () => obtenerMetricasLlamadas(rango),
  });

  const { data: multiples } = useQuery({
    queryKey: ['metricas', 'llamadas', 'multiples-tickets', rango, pagina],
    queryFn: () => obtenerLlamadasMultiplesTickets(rango, pagina, 10),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  return (
    <div>
      <div className="kpi-grid">
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Total llamadas</div>
          <div className="kpi-valor">{formatoNumero(data.totalLlamadas)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Duración promedio</div>
          <div className="kpi-valor">
            {data.duracionPromedioSegundos === null ? '—' : `${Math.round(data.duracionPromedioSegundos)}s`}
          </div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">Con múltiples tickets</div>
          <div className="kpi-valor">{formatoNumero(data.llamadasConMultiplesTickets)}</div>
        </div>
        <div className="kpi-tile">
          <div className="kpi-etiqueta">% con múltiples tickets</div>
          <div className="kpi-valor">{formatoPorcentaje(data.porcentajeMultiplesTickets)}</div>
        </div>
      </div>

      <div className="tarjeta">
        <h3>Llamadas con múltiples tickets</h3>
        <div className="tabla-envoltorio">
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Contacto</th>
                <th>Cantidad de tickets</th>
              </tr>
            </thead>
            <tbody>
              {multiples?.content.map((fila) => (
                <tr key={fila.llamadaId}>
                  <td>{format(new Date(fila.fechaHora), 'dd/MM/yyyy HH:mm', { locale: es })}</td>
                  <td>{fila.contactoNombre}</td>
                  <td>{fila.cantidadTickets}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {multiples && (
          <Pagination
            pagina={multiples.pagina}
            totalPaginas={multiples.totalPaginas}
            totalElementos={multiples.totalElementos}
            onCambiarPagina={setPagina}
          />
        )}
      </div>
    </div>
  );
}

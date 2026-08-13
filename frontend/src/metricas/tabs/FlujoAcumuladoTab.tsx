import { useQuery } from '@tanstack/react-query';
import { Area, AreaChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { obtenerFlujoAcumulado } from '../metricasApi';
import type { RangoFechas } from '../metricasApi';
import type { EstadoTicket } from '../../api/types';

// Orden fijo (nunca ciclado) para que cada estado siempre tenga el mismo
// color entre renders, sin importar qué subconjunto de estados aparezca en
// el rango de fechas elegido.
const ESTADOS_ORDEN: EstadoTicket[] = [
  'NUEVO',
  'DERIVADO',
  'EN_PROGRESO',
  'ESPERANDO_CLIENTE',
  'RESUELTO',
  'CERRADO',
];

const COLOR_POR_ESTADO: Record<EstadoTicket, string> = {
  NUEVO: 'var(--serie-1)',
  DERIVADO: 'var(--serie-2)',
  EN_PROGRESO: 'var(--serie-3)',
  ESPERANDO_CLIENTE: 'var(--serie-4)',
  RESUELTO: 'var(--serie-5)',
  CERRADO: 'var(--serie-6)',
};

const ETIQUETA_POR_ESTADO: Record<EstadoTicket, string> = {
  NUEVO: 'Nuevo',
  DERIVADO: 'Derivado',
  EN_PROGRESO: 'En progreso',
  ESPERANDO_CLIENTE: 'Esperando cliente',
  RESUELTO: 'Resuelto',
  CERRADO: 'Cerrado',
};

export function FlujoAcumuladoTab({ rango }: { rango: RangoFechas }) {
  const { data, isLoading } = useQuery({
    queryKey: ['metricas', 'flujo-acumulado', rango],
    queryFn: () => obtenerFlujoAcumulado(rango),
  });

  if (isLoading) return <div className="pagina-cargando">Cargando…</div>;
  if (!data) return null;

  const dias = Array.from(new Set(data.puntos.map((p) => p.dia))).sort();
  const estadosPresentes = ESTADOS_ORDEN.filter((estado) => data.puntos.some((p) => p.estado === estado));

  const filas = dias.map((dia) => {
    const fila: Record<string, number | string> = { dia };
    for (const estado of estadosPresentes) {
      const punto = data.puntos.find((p) => p.dia === dia && p.estado === estado);
      fila[estado] = punto?.cantidad ?? 0;
    }
    return fila;
  });

  return (
    <div className="tarjeta">
      <h3>Flujo acumulado por estado</h3>
      {filas.length === 0 ? (
        <p className="texto-muted">Sin datos en el rango seleccionado.</p>
      ) : (
        <div style={{ width: '100%', height: 380 }}>
          <ResponsiveContainer>
            <AreaChart data={filas} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid stroke="var(--color-borde)" vertical={false} />
              <XAxis dataKey="dia" tick={{ fill: 'var(--color-texto-muted)', fontSize: 12 }} />
              <YAxis tick={{ fill: 'var(--color-texto-muted)', fontSize: 12 }} allowDecimals={false} />
              <Tooltip
                contentStyle={{ background: 'var(--color-superficie)', border: '1px solid var(--color-borde)', borderRadius: 8 }}
              />
              <Legend wrapperStyle={{ fontSize: 12 }} formatter={(value) => ETIQUETA_POR_ESTADO[value as EstadoTicket] ?? value} />
              {estadosPresentes.map((estado) => (
                <Area
                  key={estado}
                  type="monotone"
                  dataKey={estado}
                  name={estado}
                  stackId="1"
                  stroke={COLOR_POR_ESTADO[estado]}
                  fill={COLOR_POR_ESTADO[estado]}
                  fillOpacity={0.65}
                />
              ))}
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}

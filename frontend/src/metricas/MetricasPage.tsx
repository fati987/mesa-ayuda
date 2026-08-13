import { useState } from 'react';
import { format, subDays } from 'date-fns';
import { SelectorRangoFechas } from './SelectorRangoFechas';
import type { RangoFechas } from './metricasApi';
import { FcrTab } from './tabs/FcrTab';
import { TasaDerivacionTab } from './tabs/TasaDerivacionTab';
import { TiempoCicloTab } from './tabs/TiempoCicloTab';
import { TiempoEsperaTab } from './tabs/TiempoEsperaTab';
import { CumplimientoSlaTab } from './tabs/CumplimientoSlaTab';
import { FlujoAcumuladoTab } from './tabs/FlujoAcumuladoTab';
import { LlamadasTab } from './tabs/LlamadasTab';

type Pestana = 'fcr' | 'derivacion' | 'ciclo' | 'espera' | 'sla' | 'flujo' | 'llamadas';

const PESTANAS: { id: Pestana; etiqueta: string }[] = [
  { id: 'fcr', etiqueta: 'FCR' },
  { id: 'derivacion', etiqueta: 'Tasa de derivación' },
  { id: 'ciclo', etiqueta: 'Tiempo de ciclo' },
  { id: 'espera', etiqueta: 'Tiempo de espera' },
  { id: 'sla', etiqueta: 'Cumplimiento SLA' },
  { id: 'flujo', etiqueta: 'Flujo acumulado' },
  { id: 'llamadas', etiqueta: 'Llamadas' },
];

function rangoPorDefecto(): RangoFechas {
  const hoy = new Date();
  return {
    desde: format(subDays(hoy, 30), 'yyyy-MM-dd'),
    hasta: format(hoy, 'yyyy-MM-dd'),
  };
}

export function MetricasPage() {
  const [rango, setRango] = useState<RangoFechas>(rangoPorDefecto);
  const [pestana, setPestana] = useState<Pestana>('fcr');

  return (
    <div>
      <h1>Métricas</h1>
      <SelectorRangoFechas rango={rango} onCambiar={setRango} />

      <div style={{ display: 'flex', gap: 4, borderBottom: '1px solid var(--color-borde)', marginBottom: 20, flexWrap: 'wrap' }}>
        {PESTANAS.map((p) => (
          <button
            key={p.id}
            type="button"
            onClick={() => setPestana(p.id)}
            style={{
              background: 'none',
              border: 'none',
              borderBottom: pestana === p.id ? '2px solid var(--color-acento)' : '2px solid transparent',
              color: pestana === p.id ? 'var(--color-acento)' : 'var(--color-texto-secundario)',
              fontWeight: 600,
              fontSize: 13,
              padding: '10px 12px',
              cursor: 'pointer',
            }}
          >
            {p.etiqueta}
          </button>
        ))}
      </div>

      {/* Solo se monta (y por lo tanto dispara su query) la pestaña activa. */}
      {pestana === 'fcr' && <FcrTab rango={rango} />}
      {pestana === 'derivacion' && <TasaDerivacionTab rango={rango} />}
      {pestana === 'ciclo' && <TiempoCicloTab rango={rango} />}
      {pestana === 'espera' && <TiempoEsperaTab rango={rango} />}
      {pestana === 'sla' && <CumplimientoSlaTab rango={rango} />}
      {pestana === 'flujo' && <FlujoAcumuladoTab rango={rango} />}
      {pestana === 'llamadas' && <LlamadasTab rango={rango} />}
    </div>
  );
}

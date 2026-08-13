import { useState } from 'react';
import type { RangoFechas } from './metricasApi';

interface SelectorRangoFechasProps {
  rango: RangoFechas;
  onCambiar: (rango: RangoFechas) => void;
}

export function SelectorRangoFechas({ rango, onCambiar }: SelectorRangoFechasProps) {
  const [desde, setDesde] = useState(rango.desde);
  const [hasta, setHasta] = useState(rango.hasta);

  const valido = desde !== '' && hasta !== '' && hasta >= desde;

  function actualizar(nuevoDesde: string, nuevoHasta: string) {
    setDesde(nuevoDesde);
    setHasta(nuevoHasta);
    if (nuevoDesde && nuevoHasta && nuevoHasta >= nuevoDesde) {
      onCambiar({ desde: nuevoDesde, hasta: nuevoHasta });
    }
  }

  return (
    <div className="tarjeta" style={{ marginBottom: 20, display: 'flex', gap: 16, alignItems: 'end', flexWrap: 'wrap' }}>
      <div className="campo" style={{ marginBottom: 0 }}>
        <label htmlFor="desde">Desde</label>
        <input id="desde" type="date" value={desde} onChange={(e) => actualizar(e.target.value, hasta)} />
      </div>
      <div className="campo" style={{ marginBottom: 0 }}>
        <label htmlFor="hasta">Hasta</label>
        <input id="hasta" type="date" value={hasta} onChange={(e) => actualizar(desde, e.target.value)} />
      </div>
      {!valido && <span style={{ color: 'var(--color-critico)' }}>El rango debe tener "hasta" igual o posterior a "desde".</span>}
    </div>
  );
}

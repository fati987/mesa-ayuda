// Helpers de formato compartidos por las pestañas de métricas. No forman
// parte de la lista de archivos del diseño original, pero evitan duplicar
// la misma lógica de "mostrar — cuando no hay datos" en 7 pestañas.

export function formatoPorcentaje(valor: number | null): string {
  if (valor === null || valor === undefined) return '—';
  return `${valor.toFixed(1)}%`;
}

export function formatoNumero(valor: number | null): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('es-CL');
}

export function formatoMinutos(valor: number | null): string {
  if (valor === null || valor === undefined) return '—';
  const total = Math.round(valor);
  const horas = Math.floor(total / 60);
  const minutos = total % 60;
  if (horas === 0) return `${minutos}m`;
  return `${horas}h ${minutos}m`;
}

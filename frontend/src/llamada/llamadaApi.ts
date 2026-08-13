import { httpClient } from '../api/httpClient';
import type { LlamadaCrearRequest, LlamadaDetalleDto } from '../api/types';

export async function crearLlamada(body: LlamadaCrearRequest): Promise<LlamadaDetalleDto> {
  const { data } = await httpClient.post<LlamadaDetalleDto>('/api/llamadas', body);
  return data;
}

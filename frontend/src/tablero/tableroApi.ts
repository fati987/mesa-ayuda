import { httpClient } from '../api/httpClient';
import type { PaginaResponse, TableroDto, TicketResumenDto } from '../api/types';

export async function obtenerTablero(areaId?: number, tamano?: number): Promise<TableroDto> {
  const params: Record<string, number> = {};
  if (areaId !== undefined) params.area = areaId;
  if (tamano !== undefined) params.tamano = tamano;
  const { data } = await httpClient.get<TableroDto>('/api/tablero', { params });
  return data;
}

export async function obtenerTicketsColumna(
  columnaId: number,
  page = 0,
  size = 20,
): Promise<PaginaResponse<TicketResumenDto>> {
  const { data } = await httpClient.get<PaginaResponse<TicketResumenDto>>(
    `/api/tablero/columnas/${columnaId}/tickets`,
    { params: { page, size } },
  );
  return data;
}

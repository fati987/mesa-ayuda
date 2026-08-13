import { useQuery } from '@tanstack/react-query';
import { httpClient } from '../api/httpClient';
import type { AreaDto, PaginaResponse } from '../api/types';

// size=100: en este alcance el catálogo de áreas es chico y se usa completo
// para poblar selects; no se pagina en la UI.
export async function listarAreas(page = 0, size = 100): Promise<PaginaResponse<AreaDto>> {
  const { data } = await httpClient.get<PaginaResponse<AreaDto>>('/api/areas', {
    params: { page, size },
  });
  return data;
}

// Lectura abierta a cualquier autenticado: se cachea con react-query porque
// varias pantallas (wizard de llamada, selector del tablero) la necesitan.
export function useAreas() {
  return useQuery({ queryKey: ['areas'], queryFn: () => listarAreas(0, 100) });
}

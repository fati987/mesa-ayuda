import { useQuery } from '@tanstack/react-query';
import { httpClient } from '../api/httpClient';
import type { CategoriaDto, PaginaResponse } from '../api/types';

export async function listarCategorias(page = 0, size = 100): Promise<PaginaResponse<CategoriaDto>> {
  const { data } = await httpClient.get<PaginaResponse<CategoriaDto>>('/api/categorias', {
    params: { page, size },
  });
  return data;
}

export function useCategorias() {
  return useQuery({ queryKey: ['categorias'], queryFn: () => listarCategorias(0, 100) });
}

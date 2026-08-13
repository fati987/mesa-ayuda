import { httpClient } from '../api/httpClient';
import type {
  CumplimientoSlaResponse,
  FcrResponse,
  FlujoAcumuladoResponse,
  LlamadaMultiplesTicketsItem,
  LlamadasMetricasResponse,
  PaginaResponse,
  RebotesItem,
  TasaDerivacionResponse,
  TiempoCicloResponse,
  TiempoEsperaResponse,
} from '../api/types';

export interface RangoFechas {
  desde: string;
  hasta: string;
}

export async function obtenerFcr(rango: RangoFechas): Promise<FcrResponse> {
  const { data } = await httpClient.get<FcrResponse>('/api/metricas/fcr', { params: rango });
  return data;
}

export async function obtenerTasaDerivacion(rango: RangoFechas): Promise<TasaDerivacionResponse> {
  const { data } = await httpClient.get<TasaDerivacionResponse>('/api/metricas/tasa-derivacion', {
    params: rango,
  });
  return data;
}

export async function obtenerRebotes(
  rango: RangoFechas,
  page = 0,
  size = 10,
): Promise<PaginaResponse<RebotesItem>> {
  const { data } = await httpClient.get<PaginaResponse<RebotesItem>>(
    '/api/metricas/tasa-derivacion/rebotes',
    { params: { ...rango, page, size } },
  );
  return data;
}

export async function obtenerTiempoCiclo(rango: RangoFechas): Promise<TiempoCicloResponse> {
  const { data } = await httpClient.get<TiempoCicloResponse>('/api/metricas/tiempo-ciclo', {
    params: rango,
  });
  return data;
}

export async function obtenerTiempoEspera(rango: RangoFechas): Promise<TiempoEsperaResponse> {
  const { data } = await httpClient.get<TiempoEsperaResponse>('/api/metricas/tiempo-espera', {
    params: rango,
  });
  return data;
}

export async function obtenerCumplimientoSla(rango: RangoFechas): Promise<CumplimientoSlaResponse> {
  const { data } = await httpClient.get<CumplimientoSlaResponse>('/api/metricas/cumplimiento-sla', {
    params: rango,
  });
  return data;
}

export async function obtenerFlujoAcumulado(rango: RangoFechas): Promise<FlujoAcumuladoResponse> {
  const { data } = await httpClient.get<FlujoAcumuladoResponse>('/api/metricas/flujo-acumulado', {
    params: rango,
  });
  return data;
}

export async function obtenerMetricasLlamadas(rango: RangoFechas): Promise<LlamadasMetricasResponse> {
  const { data } = await httpClient.get<LlamadasMetricasResponse>('/api/metricas/llamadas', {
    params: rango,
  });
  return data;
}

export async function obtenerLlamadasMultiplesTickets(
  rango: RangoFechas,
  page = 0,
  size = 10,
): Promise<PaginaResponse<LlamadaMultiplesTicketsItem>> {
  const { data } = await httpClient.get<PaginaResponse<LlamadaMultiplesTicketsItem>>(
    '/api/metricas/llamadas/multiples-tickets',
    { params: { ...rango, page, size } },
  );
  return data;
}

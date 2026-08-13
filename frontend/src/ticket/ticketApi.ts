import { httpClient } from '../api/httpClient';
import type {
  ComentarioCrearRequest,
  ComentarioDto,
  EstadoTicket,
  PaginaResponse,
  Prioridad,
  TicketCrearRequest,
  TicketDerivarRequest,
  TicketDetalleDto,
  TicketResolverRequest,
  TicketResumenDto,
} from '../api/types';

export interface FiltrosTickets {
  estado?: EstadoTicket;
  prioridad?: Prioridad;
  areaId?: number;
  page?: number;
  size?: number;
}

export async function listarTickets(filtros: FiltrosTickets = {}): Promise<PaginaResponse<TicketResumenDto>> {
  const { data } = await httpClient.get<PaginaResponse<TicketResumenDto>>('/api/tickets', {
    params: filtros,
  });
  return data;
}

export async function obtenerTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.get<TicketDetalleDto>(`/api/tickets/${codigo}`);
  return data;
}

export async function crearTicket(body: TicketCrearRequest): Promise<TicketDetalleDto> {
  const { data } = await httpClient.post<TicketDetalleDto>('/api/tickets', body);
  return data;
}

export async function derivarTicket(codigo: string, body: TicketDerivarRequest): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/derivacion`, body);
  return data;
}

export async function tomarTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/toma`);
  return data;
}

export async function resolverTicket(codigo: string, body: TicketResolverRequest): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/resolucion`, body);
  return data;
}

export async function ponerEnEsperaTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/espera`);
  return data;
}

export async function reanudarTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/reanudacion`);
  return data;
}

export async function cerrarTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/cierre`);
  return data;
}

export async function reabrirTicket(codigo: string): Promise<TicketDetalleDto> {
  const { data } = await httpClient.patch<TicketDetalleDto>(`/api/tickets/${codigo}/reapertura`);
  return data;
}

export async function listarComentarios(
  codigo: string,
  page = 0,
  size = 20,
): Promise<PaginaResponse<ComentarioDto>> {
  const { data } = await httpClient.get<PaginaResponse<ComentarioDto>>(
    `/api/tickets/${codigo}/comentarios`,
    { params: { page, size } },
  );
  return data;
}

export async function crearComentario(
  codigo: string,
  body: ComentarioCrearRequest,
): Promise<ComentarioDto> {
  const { data } = await httpClient.post<ComentarioDto>(`/api/tickets/${codigo}/comentarios`, body);
  return data;
}

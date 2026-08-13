// Tipos que reflejan EXACTAMENTE los contratos del backend (DTOs, enums,
// paginación y forma de error). No se agregan ni se renombran campos.
// erasableSyntaxOnly (tsconfig) impide `enum`: los enums del backend se
// modelan como uniones de string literal, que además viajan tal cual en JSON.

export type Rol = 'AGENTE' | 'SUPERVISOR' | 'ADMIN';
export type TipoTicket = 'INCIDENTE' | 'SOLICITUD';
export type EstadoTicket =
  | 'NUEVO'
  | 'DERIVADO'
  | 'EN_PROGRESO'
  | 'ESPERANDO_CLIENTE'
  | 'RESUELTO'
  | 'CERRADO';
export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type Urgencia = 'BAJA' | 'MEDIA' | 'ALTA';
export type Impacto = 'INDIVIDUAL' | 'AREA' | 'ORGANIZACION';
export type Origen = 'TELEFONO' | 'CORREO' | 'WEB' | 'CHAT';
export type Visibilidad = 'PUBLICO' | 'INTERNO';

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  codigoError: string;
}

export interface PaginaResponse<T> {
  content: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

// ---------- Auth ----------

export interface LoginRequest {
  correo: string;
  contrasena: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tipoToken: 'Bearer';
  expiraEnSegundos: number;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
  tipoToken: 'Bearer';
}

export interface UsuarioMeDto {
  nombreCompleto: string;
  correo: string;
  rol: Rol;
  areaId: number;
  areaNombre: string;
}

// ---------- Área / Categoría ----------

export interface AreaDto {
  id: number;
  nombre: string;
  recibeLlamadas: boolean;
  limiteWipAgente: number;
  activo: boolean;
}

export interface CategoriaDto {
  id: number;
  nombre: string;
  descripcion: string;
  activo: boolean;
}

// ---------- Llamada ----------

export interface LlamadaCrearRequest {
  contactoTelefono: string;
  contactoNombreCompleto: string;
  contactoCorreo?: string;
  duracionSegundos: number;
}

export interface ContactoResumenDto {
  nombreCompleto: string;
  telefono: string;
  correo: string | null;
}

export interface LlamadaResumenDto {
  fechaHora: string;
  duracionSegundos: number;
  usuarioAtiendeNombre: string;
}

export interface LlamadaDetalleDto {
  id: number;
  llamada: LlamadaResumenDto;
  contacto: ContactoResumenDto;
}

// ---------- Ticket ----------

export interface TicketResumenDto {
  codigo: string;
  titulo: string;
  tipo: TipoTicket;
  estado: EstadoTicket;
  prioridad: Prioridad;
  urgencia: Urgencia;
  impacto: Impacto;
  areaActualNombre: string;
  categoriaNombre: string;
  contactoNombre: string;
  resueltoEnLlamada: boolean;
  usuarioAsignadoNombre: string | null;
  creadoEn: string;
  actualizadoEn: string;
}

export interface HistorialEstadoDto {
  estadoAnterior: EstadoTicket | null;
  estadoNuevo: EstadoTicket;
  usuarioNombre: string;
  comentario: string | null;
  creadoEn: string;
}

export interface DerivacionDto {
  motivo: string;
  areaOrigenNombre: string;
  areaDestinoNombre: string;
  usuarioDerivaNombre: string;
  creadoEn: string;
}

export interface TicketDetalleDto {
  codigo: string;
  titulo: string;
  descripcion: string;
  tipo: TipoTicket;
  estado: EstadoTicket;
  prioridad: Prioridad;
  urgencia: Urgencia;
  impacto: Impacto;
  origen: Origen;
  areaActualNombre: string;
  categoriaNombre: string;
  usuarioCreadorNombre: string;
  usuarioAsignadoNombre: string | null;
  resueltoEnLlamada: boolean;
  solucion: string | null;
  minutosPausado: number;
  contacto: ContactoResumenDto;
  llamada: LlamadaResumenDto | null;
  historial: HistorialEstadoDto[];
  derivaciones: DerivacionDto[];
  fechaVencimiento: string | null;
  escalado: boolean;
  creadoEn: string;
  actualizadoEn: string;
}

export interface TicketCrearRequest {
  llamadaId: number;
  areaId: number;
  categoriaId: number;
  tipo: TipoTicket;
  prioridad: Prioridad;
  urgencia: Urgencia;
  impacto: Impacto;
  titulo: string;
  descripcion: string;
  resueltoEnLlamada: boolean;
  solucion?: string;
}

export interface TicketDerivarRequest {
  areaDestinoId: number;
  motivo: string;
}

export interface TicketResolverRequest {
  solucion?: string;
}

// ---------- Comentarios ----------

export interface ComentarioDto {
  autorNombre: string;
  visibilidad: Visibilidad;
  contenido: string;
  creadoEn: string;
}

export interface ComentarioCrearRequest {
  visibilidad: Visibilidad;
  contenido: string;
}

// ---------- Tablero ----------

export interface ColumnaTableroDto {
  id: number;
  nombre: string;
  estadoAsociado: EstadoTicket;
  orden: number;
  tickets: PaginaResponse<TicketResumenDto>;
}

export interface TableroDto {
  areaId: number;
  areaNombre: string;
  columnas: ColumnaTableroDto[];
}

// ---------- Métricas ----------
// Los campos numéricos que en el backend son BigDecimal (porcentajes,
// promedios, medianas) llegan como number en el JSON. Todos pueden ser
// null cuando no hay datos suficientes (denominador cero).

export interface FcrPorAreaDto {
  areaId: number;
  areaNombre: string;
  totalCreados: number;
  resueltosEnLlamada: number;
  porcentajeFcr: number | null;
}

export interface FcrPorDiaDto {
  dia: string;
  totalCreados: number;
  resueltosEnLlamada: number;
  porcentajeFcr: number | null;
}

export interface FcrResponse {
  desde: string;
  hasta: string;
  totalCreados: number;
  resueltosEnLlamada: number;
  porcentajeFcr: number | null;
  porArea: FcrPorAreaDto[];
  porDia: FcrPorDiaDto[];
}

export interface TasaDerivacionPorAreaDto {
  areaId: number;
  areaNombre: string;
  totalCreados: number;
  derivadosAlMenosUnaVez: number;
  tasaDerivacion: number | null;
}

export interface TasaDerivacionResponse {
  desde: string;
  hasta: string;
  totalCreados: number;
  derivadosAlMenosUnaVez: number;
  tasaDerivacion: number | null;
  ticketsRebotados: number;
  tasaRebote: number | null;
  porArea: TasaDerivacionPorAreaDto[];
}

export interface RebotesItem {
  codigo: string;
  areaActualNombre: string;
  cantidadDerivaciones: number;
}

export interface TiempoCicloResponse {
  desde: string;
  hasta: string;
  ticketsLeadTime: number;
  leadTimePromedioMinutos: number | null;
  leadTimeMedianoMinutos: number | null;
  ticketsCycleTime: number;
  cycleTimePromedioMinutos: number | null;
  cycleTimeMedianoMinutos: number | null;
}

export interface TiempoEsperaPorAreaDto {
  areaId: number;
  areaNombre: string;
  tomasConsideradas: number;
  esperaPromedioMinutos: number | null;
}

export interface TiempoEsperaResponse {
  desde: string;
  hasta: string;
  tomasConsideradas: number;
  esperaPromedioMinutos: number | null;
  esperaMedianaMinutos: number | null;
  porArea: TiempoEsperaPorAreaDto[];
}

export interface DimensionDto {
  nombre: string;
  total: number;
  cumplidos: number;
  incumplidos: number;
  enCurso: number;
  porcentajeCumplimiento: number | null;
}

export interface CumplimientoSlaResponse {
  desde: string;
  hasta: string;
  total: number;
  cumplidos: number;
  incumplidos: number;
  enCurso: number;
  porcentajeCumplimiento: number | null;
  porCategoria: DimensionDto[];
  porArea: DimensionDto[];
  porPrioridad: DimensionDto[];
}

export interface FlujoAcumuladoPunto {
  dia: string;
  estado: EstadoTicket;
  cantidad: number;
}

export interface FlujoAcumuladoResponse {
  desde: string;
  hasta: string;
  puntos: FlujoAcumuladoPunto[];
}

export interface LlamadasMetricasResponse {
  desde: string;
  hasta: string;
  totalLlamadas: number;
  duracionPromedioSegundos: number | null;
  llamadasConMultiplesTickets: number;
  porcentajeMultiplesTickets: number | null;
}

export interface LlamadaMultiplesTicketsItem {
  llamadaId: number;
  fechaHora: string;
  contactoNombre: string;
  cantidadTickets: number;
}

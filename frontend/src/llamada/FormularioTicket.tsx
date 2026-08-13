import { useState } from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { useAreas } from '../area/areaApi';
import { useCategorias } from '../categoria/categoriaApi';
import { crearTicket } from '../ticket/ticketApi';
import type { Impacto, Prioridad, TicketDetalleDto, TipoTicket, Urgencia } from '../api/types';

interface FormValues {
  areaId: string;
  categoriaId: string;
  tipo: TipoTicket;
  prioridad: Prioridad;
  urgencia: Urgencia;
  impacto: Impacto;
  titulo: string;
  descripcion: string;
  resueltoEnLlamada: boolean;
  solucion: string;
}

interface FormularioTicketProps {
  llamadaId: number;
  onCreado: (ticket: TicketDetalleDto, areaId: number) => void;
}

const TIPOS: TipoTicket[] = ['INCIDENTE', 'SOLICITUD'];
const PRIORIDADES: Prioridad[] = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];
const URGENCIAS: Urgencia[] = ['BAJA', 'MEDIA', 'ALTA'];
const IMPACTOS: Impacto[] = ['INDIVIDUAL', 'AREA', 'ORGANIZACION'];

export function FormularioTicket({ llamadaId, onCreado }: FormularioTicketProps) {
  const { data: paginaAreas, isLoading: cargandoAreas } = useAreas();
  const { data: paginaCategorias, isLoading: cargandoCategorias } = useCategorias();
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  // Regla de negocio: solo las áreas con recibeLlamadas=true pueden originar
  // tickets telefónicos. Filtrado en el cliente porque GET /api/areas
  // devuelve todas las áreas activas, sin este filtro aplicado.
  const areasQueReciben = (paginaAreas?.content ?? []).filter((area) => area.recibeLlamadas);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    defaultValues: {
      tipo: 'INCIDENTE',
      prioridad: 'MEDIA',
      urgencia: 'MEDIA',
      impacto: 'INDIVIDUAL',
      resueltoEnLlamada: false,
    },
  });

  const resueltoEnLlamada = watch('resueltoEnLlamada');

  const onSubmit: SubmitHandler<FormValues> = async (values) => {
    setErrorEnvio(null);
    try {
      const ticket = await crearTicket({
        llamadaId,
        areaId: Number(values.areaId),
        categoriaId: Number(values.categoriaId),
        tipo: values.tipo,
        prioridad: values.prioridad,
        urgencia: values.urgencia,
        impacto: values.impacto,
        titulo: values.titulo.trim(),
        descripcion: values.descripcion.trim(),
        resueltoEnLlamada: values.resueltoEnLlamada,
        solucion: values.resueltoEnLlamada ? values.solucion.trim() || undefined : undefined,
      });
      onCreado(ticket, Number(values.areaId));
    } catch {
      setErrorEnvio('No se pudo crear el ticket. Revisá los datos e intentá de nuevo.');
    }
  };

  return (
    <form className="tarjeta" onSubmit={handleSubmit(onSubmit)} noValidate>
      <h2>2. Ticket</h2>
      <p className="texto-secundario">Registrá el motivo de la llamada como ticket.</p>

      <div className="fila-campos">
        <div className="campo">
          <label htmlFor="areaId">Área</label>
          <select id="areaId" disabled={cargandoAreas} {...register('areaId', { required: true })}>
            <option value="">Seleccioná un área…</option>
            {areasQueReciben.map((area) => (
              <option key={area.id} value={area.id}>
                {area.nombre}
              </option>
            ))}
          </select>
          {errors.areaId && <span className="texto-secundario">El área es obligatoria</span>}
        </div>

        <div className="campo">
          <label htmlFor="categoriaId">Categoría</label>
          <select id="categoriaId" disabled={cargandoCategorias} {...register('categoriaId', { required: true })}>
            <option value="">Seleccioná una categoría…</option>
            {(paginaCategorias?.content ?? []).map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nombre}
              </option>
            ))}
          </select>
          {errors.categoriaId && <span className="texto-secundario">La categoría es obligatoria</span>}
        </div>
      </div>

      <div className="fila-campos">
        <div className="campo">
          <label htmlFor="tipo">Tipo</label>
          <select id="tipo" {...register('tipo')}>
            {TIPOS.map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo === 'INCIDENTE' ? 'Incidente' : 'Solicitud'}
              </option>
            ))}
          </select>
        </div>

        <div className="campo">
          <label htmlFor="prioridad">Prioridad</label>
          <select id="prioridad" {...register('prioridad')}>
            {PRIORIDADES.map((prioridad) => (
              <option key={prioridad} value={prioridad}>
                {prioridad}
              </option>
            ))}
          </select>
        </div>

        <div className="campo">
          <label htmlFor="urgencia">Urgencia</label>
          <select id="urgencia" {...register('urgencia')}>
            {URGENCIAS.map((urgencia) => (
              <option key={urgencia} value={urgencia}>
                {urgencia}
              </option>
            ))}
          </select>
        </div>

        <div className="campo">
          <label htmlFor="impacto">Impacto</label>
          <select id="impacto" {...register('impacto')}>
            {IMPACTOS.map((impacto) => (
              <option key={impacto} value={impacto}>
                {impacto}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="campo">
        <label htmlFor="titulo">Título</label>
        <input id="titulo" type="text" {...register('titulo', { required: 'El título es obligatorio' })} />
        {errors.titulo && <span className="texto-secundario">{errors.titulo.message}</span>}
      </div>

      <div className="campo">
        <label htmlFor="descripcion">Descripción</label>
        <textarea id="descripcion" {...register('descripcion', { required: 'La descripción es obligatoria' })} />
        {errors.descripcion && <span className="texto-secundario">{errors.descripcion.message}</span>}
      </div>

      <div className="campo">
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input type="checkbox" style={{ width: 'auto' }} {...register('resueltoEnLlamada')} />
          ¿Se resolvió en la llamada?
        </label>
      </div>

      {resueltoEnLlamada && (
        <div className="campo">
          <label htmlFor="solucion">Solución</label>
          <textarea id="solucion" {...register('solucion')} placeholder="Describí cómo se resolvió" />
        </div>
      )}

      {errorEnvio && <p style={{ color: 'var(--color-critico)' }}>{errorEnvio}</p>}

      <button type="submit" className="boton boton-primario" disabled={isSubmitting}>
        {isSubmitting ? 'Creando ticket…' : 'Crear ticket'}
      </button>
    </form>
  );
}

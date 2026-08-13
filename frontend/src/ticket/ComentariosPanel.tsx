import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { crearComentario, listarComentarios } from './ticketApi';
import { Pagination } from '../shared/Pagination';
import type { Visibilidad } from '../api/types';

export function ComentariosPanel({ codigo }: { codigo: string }) {
  const queryClient = useQueryClient();
  const [pagina, setPagina] = useState(0);
  const [contenido, setContenido] = useState('');
  const [visibilidad, setVisibilidad] = useState<Visibilidad>('PUBLICO');

  const { data } = useQuery({
    queryKey: ['comentarios', codigo, pagina],
    queryFn: () => listarComentarios(codigo, pagina, 10),
  });

  const mutacion = useMutation({
    mutationFn: () => crearComentario(codigo, { visibilidad, contenido: contenido.trim() }),
    onSuccess: () => {
      setContenido('');
      queryClient.invalidateQueries({ queryKey: ['comentarios', codigo] });
    },
  });

  return (
    <div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {(data?.content.length ?? 0) === 0 && <p className="texto-muted">Todavía no hay comentarios.</p>}
        {data?.content.map((comentario, indice) => (
          <div key={indice} className="tarjeta" style={{ padding: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' }}>
              <strong>{comentario.autorNombre}</strong>
              <span
                className="badge"
                style={
                  comentario.visibilidad === 'INTERNO'
                    ? { color: 'var(--color-alerta)', background: 'var(--color-alerta-fondo)' }
                    : { color: 'var(--color-bien)', background: 'var(--color-bien-fondo)' }
                }
              >
                {comentario.visibilidad === 'INTERNO' ? 'Interno' : 'Público'}
              </span>
            </div>
            <p style={{ margin: '6px 0' }}>{comentario.contenido}</p>
            <span className="texto-muted" style={{ fontSize: 12 }}>
              {format(new Date(comentario.creadoEn), 'dd/MM/yyyy HH:mm', { locale: es })}
            </span>
          </div>
        ))}
      </div>

      {data && (
        <Pagination
          pagina={data.pagina}
          totalPaginas={data.totalPaginas}
          totalElementos={data.totalElementos}
          onCambiarPagina={setPagina}
        />
      )}

      <form
        style={{ marginTop: 16 }}
        onSubmit={(e) => {
          e.preventDefault();
          if (contenido.trim()) mutacion.mutate();
        }}
      >
        <div className="campo">
          <label htmlFor="contenido-comentario">Agregar comentario</label>
          <textarea
            id="contenido-comentario"
            value={contenido}
            onChange={(e) => setContenido(e.target.value)}
            placeholder="Escribe un comentario…"
          />
        </div>
        <div className="fila-campos" style={{ alignItems: 'end' }}>
          <div className="campo" style={{ marginBottom: 0, maxWidth: 200 }}>
            <label htmlFor="visibilidad-comentario">Visibilidad</label>
            <select
              id="visibilidad-comentario"
              value={visibilidad}
              onChange={(e) => setVisibilidad(e.target.value as Visibilidad)}
            >
              <option value="PUBLICO">Público</option>
              <option value="INTERNO">Interno</option>
            </select>
          </div>
          <button type="submit" className="boton boton-primario" disabled={mutacion.isPending || !contenido.trim()}>
            {mutacion.isPending ? 'Enviando…' : 'Comentar'}
          </button>
        </div>
      </form>
    </div>
  );
}

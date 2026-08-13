interface PaginationProps {
  pagina: number;
  totalPaginas: number;
  totalElementos: number;
  onCambiarPagina: (pagina: number) => void;
}

export function Pagination({ pagina, totalPaginas, totalElementos, onCambiarPagina }: PaginationProps) {
  if (totalPaginas <= 1) return null;

  return (
    <div className="paginacion">
      <button
        type="button"
        className="boton boton-secundario boton-chico"
        disabled={pagina <= 0}
        onClick={() => onCambiarPagina(pagina - 1)}
      >
        Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas} · {totalElementos} en total
      </span>
      <button
        type="button"
        className="boton boton-secundario boton-chico"
        disabled={pagina >= totalPaginas - 1}
        onClick={() => onCambiarPagina(pagina + 1)}
      >
        Siguiente
      </button>
    </div>
  );
}

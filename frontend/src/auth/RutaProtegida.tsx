import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import type { Rol } from '../api/types';

interface RutaProtegidaProps {
  children: ReactNode;
  /**
   * Restringe qué roles ven esta ruta en la navegación. IMPORTANTE: esto es
   * SOLO una conveniencia de UX (evita que un agente vea un link a una
   * pantalla que no le sirve). El mecanismo de seguridad real vive en el
   * backend: @PreAuthorize por rol y validación de área en el servicio. Un
   * usuario que fuerce la URL sin tener el rol correcto será rechazado por
   * la API, no por este componente.
   */
  rolesPermitidos?: Rol[];
}

export function RutaProtegida({ children, rolesPermitidos }: RutaProtegidaProps) {
  const { usuario, cargando } = useAuth();
  const location = useLocation();

  if (cargando) {
    return <div className="pagina-cargando">Cargando…</div>;
  }
  if (!usuario) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    return <Navigate to="/tablero" replace />;
  }
  return <>{children}</>;
}

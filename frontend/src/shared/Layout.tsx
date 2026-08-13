import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const ETIQUETAS_ROL: Record<string, string> = {
  AGENTE: 'Agente',
  SUPERVISOR: 'Supervisor',
  ADMIN: 'Administrador',
};

// Los links que se muestran acá dependen del rol SOLO para no ofrecer
// navegación hacia pantallas que el usuario no puede usar (UX). El control
// de acceso real ocurre en el backend (@PreAuthorize + validación de área);
// ocultar un link acá no impide nada por sí mismo.
export function Layout({ children }: { children: ReactNode }) {
  const { usuario, logout } = useAuth();

  return (
    <div>
      <nav className="layout-nav">
        <div className="layout-nav-links">
          <NavLink to="/tablero" className={({ isActive }) => (isActive ? 'activo' : '')}>
            Tablero
          </NavLink>
          {usuario && (usuario.rol === 'AGENTE' || usuario.rol === 'SUPERVISOR') && (
            <NavLink to="/atencion-llamada" className={({ isActive }) => (isActive ? 'activo' : '')}>
              Atención de llamada
            </NavLink>
          )}
          {usuario && (usuario.rol === 'SUPERVISOR' || usuario.rol === 'ADMIN') && (
            <NavLink to="/metricas" className={({ isActive }) => (isActive ? 'activo' : '')}>
              Métricas
            </NavLink>
          )}
        </div>
        {usuario && (
          <div className="layout-usuario">
            <div className="layout-usuario-info">
              <div className="layout-usuario-nombre">{usuario.nombreCompleto}</div>
              <div className="texto-muted">
                {ETIQUETAS_ROL[usuario.rol] ?? usuario.rol} · {usuario.areaNombre}
              </div>
            </div>
            <button type="button" className="boton boton-secundario boton-chico" onClick={logout}>
              Salir
            </button>
          </div>
        )}
      </nav>
      <main className="pagina">{children}</main>
    </div>
  );
}

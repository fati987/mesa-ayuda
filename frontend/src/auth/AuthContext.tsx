import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { login as loginRequest, obtenerUsuarioActual } from './authApi';
import { getAccessToken, setTokens, clearTokens } from './tokenStorage';
import { registerAuthFailureHandler } from '../api/httpClient';
import type { UsuarioMeDto } from '../api/types';

interface AuthContextValue {
  usuario: UsuarioMeDto | null;
  cargando: boolean;
  login: (correo: string, contrasena: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioMeDto | null>(null);
  const [cargando, setCargando] = useState(true);

  const logout = useCallback(() => {
    clearTokens();
    setUsuario(null);
  }, []);

  useEffect(() => {
    registerAuthFailureHandler(() => {
      setUsuario(null);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    });
  }, []);

  useEffect(() => {
    const token = getAccessToken();
    if (!token) {
      setCargando(false);
      return;
    }
    obtenerUsuarioActual()
      .then(setUsuario)
      .catch(() => clearTokens())
      .finally(() => setCargando(false));
  }, []);

  const login = useCallback(async (correo: string, contrasena: string) => {
    const respuesta = await loginRequest({ correo, contrasena });
    setTokens(respuesta.accessToken, respuesta.refreshToken);
    const usuarioActual = await obtenerUsuarioActual();
    setUsuario(usuarioActual);
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, cargando, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  }
  return ctx;
}

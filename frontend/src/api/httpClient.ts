import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, getRefreshToken, setAccessToken, clearTokens } from '../auth/tokenStorage';
import { publishToast } from '../shared/toastBus';
import type { ApiError, RefreshResponse } from './types';

const baseURL = import.meta.env.VITE_API_BASE_URL;

export const httpClient = axios.create({ baseURL });

// Instancia aparte, sin interceptores, solo para /api/auth/refresh: si se
// reutilizara `httpClient` para llamar a refresh y el propio refresh diera
// 401, el interceptor de respuesta volvería a intentar refrescar y entraría
// en bucle.
const refreshClient = axios.create({ baseURL });

httpClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

interface ConfigConReintento extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

// Varias requests pueden recibir 401 en paralelo (p. ej. el tablero dispara
// varias llamadas a la vez). Se comparte una única promesa de refresh entre
// todas para no disparar N requests de /refresh simultáneos.
let refreshEnCurso: Promise<string> | null = null;

async function refrescarAccessToken(): Promise<string> {
  if (!refreshEnCurso) {
    refreshEnCurso = (async () => {
      const refreshToken = getRefreshToken();
      if (!refreshToken) {
        throw new Error('No hay refresh token disponible');
      }
      const { data } = await refreshClient.post<RefreshResponse>('/api/auth/refresh', {
        refreshToken,
      });
      setAccessToken(data.accessToken);
      return data.accessToken;
    })().finally(() => {
      refreshEnCurso = null;
    });
  }
  return refreshEnCurso;
}

// Registrado por AuthContext para limpiar el estado de React y redirigir a
// /login cuando el refresh falla. Vive fuera del árbol de React porque los
// interceptores de axios se ejecutan sin acceso a los hooks.
let onAuthFailure: (() => void) | null = null;

export function registerAuthFailureHandler(handler: () => void): void {
  onAuthFailure = handler;
}

function esEndpointDeAuth(url: string | undefined): boolean {
  if (!url) return false;
  return url.includes('/api/auth/login') || url.includes('/api/auth/refresh');
}

httpClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiError>) => {
    const config = error.config as ConfigConReintento | undefined;
    const esAuth = esEndpointDeAuth(config?.url);

    if (error.response?.status === 401 && config && !config._retry && !esAuth) {
      config._retry = true;
      try {
        const nuevoToken = await refrescarAccessToken();
        config.headers.set('Authorization', `Bearer ${nuevoToken}`);
        return httpClient(config);
      } catch {
        clearTokens();
        onAuthFailure?.();
        return Promise.reject(error);
      }
    }

    // El 401 de login/refresh y el 409 del drag-and-drop del tablero se
    // manejan localmente en sus respectivos componentes; el resto de los
    // errores de negocio se muestran acá, en un único lugar, con el mensaje
    // real del backend (nunca un texto inventado).
    if (!esAuth) {
      const mensaje = error.response?.data?.mensaje;
      if (mensaje) {
        publishToast(mensaje, 'error');
      }
    }
    return Promise.reject(error);
  },
);

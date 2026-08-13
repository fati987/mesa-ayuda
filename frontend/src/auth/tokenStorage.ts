// Wrapper sobre localStorage para los tokens JWT.
//
// Tradeoff consciente: se usa localStorage (legible por cualquier script, por
// lo tanto vulnerable a robo de token vía XSS) en vez de una cookie httpOnly,
// porque este es un proyecto de portafolio sin infraestructura de cookies
// firmadas en el backend, y localStorage evita perder la sesión en cada
// refresh de página (una cookie de sesión in-memory obligaría a loguearse de
// nuevo en cada F5). Si esto fuera a producción real, el refreshToken debería
// vivir en una cookie httpOnly + SameSite gestionada por el backend.

const ACCESS_TOKEN_KEY = 'mesaayuda.accessToken';
const REFRESH_TOKEN_KEY = 'mesaayuda.refreshToken';

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function setTokens(accessToken: string, refreshToken: string): void {
  setAccessToken(accessToken);
  setRefreshToken(refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

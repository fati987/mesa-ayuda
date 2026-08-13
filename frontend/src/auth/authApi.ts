import { httpClient } from '../api/httpClient';
import type { LoginRequest, LoginResponse, UsuarioMeDto } from '../api/types';

export async function login(body: LoginRequest): Promise<LoginResponse> {
  const { data } = await httpClient.post<LoginResponse>('/api/auth/login', body);
  return data;
}

export async function obtenerUsuarioActual(): Promise<UsuarioMeDto> {
  const { data } = await httpClient.get<UsuarioMeDto>('/api/auth/me');
  return data;
}

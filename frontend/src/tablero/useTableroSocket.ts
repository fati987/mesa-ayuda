import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import type { QueryKey } from '@tanstack/react-query';
import { useQueryClient } from '@tanstack/react-query';
import { getAccessToken } from '../auth/tokenStorage';

const apiBaseUrl: string = import.meta.env.VITE_API_BASE_URL;
const wsBaseUrl = apiBaseUrl.replace(/^http/, 'ws');

/**
 * Se suscribe a /topic/tablero/{areaId} y, ante cualquier mensaje, invalida
 * la query del tablero para que react-query refetcheé el estado real — no
 * se intenta mergear el payload del WS a mano, mismo criterio que ya usa
 * TableroPage en onSettled tras una mutación local propia.
 */
export function useTableroSocket(areaId: number | null, queryKey: QueryKey): void {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (areaId === null) {
      return;
    }
    const token = getAccessToken();
    if (!token) {
      return;
    }

    const client = new Client({
      brokerURL: `${wsBaseUrl}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/tablero/${areaId}`, () => {
          queryClient.invalidateQueries({ queryKey });
        });
      },
    });
    client.activate();

    return () => {
      client.deactivate();
    };
  }, [areaId, queryClient, queryKey]);
}

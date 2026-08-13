// Bus de notificaciones simple, sin librería externa. httpClient publica acá
// los mensajes de error de negocio (ApiError.mensaje) y ToastContainer se
// suscribe para renderizarlos. Cualquier otra parte de la app puede publicar
// mensajes de éxito/información del mismo modo.

export type TipoToast = 'error' | 'exito' | 'info';

export interface Toast {
  id: number;
  mensaje: string;
  tipo: TipoToast;
}

type Listener = (toasts: Toast[]) => void;

let toasts: Toast[] = [];
let nextId = 1;
const listeners = new Set<Listener>();

function emitir(): void {
  for (const listener of listeners) listener(toasts);
}

export function publishToast(mensaje: string, tipo: TipoToast = 'info'): void {
  const toast: Toast = { id: nextId++, mensaje, tipo };
  toasts = [...toasts, toast];
  emitir();
  setTimeout(() => dismissToast(toast.id), 5000);
}

export function dismissToast(id: number): void {
  toasts = toasts.filter((t) => t.id !== id);
  emitir();
}

export function subscribeToasts(listener: Listener): () => void {
  listeners.add(listener);
  listener(toasts);
  return () => {
    listeners.delete(listener);
  };
}

import { useSyncExternalStore } from 'react';
import { dismissToast, subscribeToasts, type Toast } from './toastBus';

let toastsSnapshot: Toast[] = [];

function subscribe(callback: () => void): () => void {
  return subscribeToasts((toasts) => {
    toastsSnapshot = toasts;
    callback();
  });
}

function getSnapshot(): Toast[] {
  return toastsSnapshot;
}

export function ToastContainer() {
  const toasts = useSyncExternalStore(subscribe, getSnapshot);

  if (toasts.length === 0) return null;

  return (
    <div className="toast-contenedor" role="status" aria-live="polite">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`toast toast-${toast.tipo}`}
          onClick={() => dismissToast(toast.id)}
          title="Click para cerrar"
        >
          {toast.mensaje}
        </div>
      ))}
    </div>
  );
}

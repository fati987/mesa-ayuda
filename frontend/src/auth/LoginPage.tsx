import { useState } from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { useLocation, useNavigate } from 'react-router-dom';
import type { AxiosError } from 'axios';
import { useAuth } from './AuthContext';
import type { ApiError } from '../api/types';
import styles from './LoginPage.module.css';

interface FormValues {
  correo: string;
  contrasena: string;
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [errorLogin, setErrorLogin] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>();

  // El mensaje de credenciales inválidas se captura localmente acá en vez de
  // venir del toast central: httpClient no publica toasts para endpoints de
  // /api/auth, precisamente para no duplicar este mismo mensaje dos veces.
  const onSubmit: SubmitHandler<FormValues> = async (values) => {
    setErrorLogin(null);
    try {
      await login(values.correo, values.contrasena);
      const destino = (location.state as { from?: string } | null)?.from ?? '/tablero';
      navigate(destino, { replace: true });
    } catch (err) {
      const axiosError = err as AxiosError<ApiError>;
      setErrorLogin(axiosError.response?.data?.mensaje ?? 'No se pudo iniciar sesión. Verificá tus credenciales.');
    }
  };

  return (
    <div className={styles.contenedor}>
      <form className={styles.formulario} onSubmit={handleSubmit(onSubmit)} noValidate>
        <h1>Mesa de ayuda</h1>
        <p className={styles.subtitulo}>Soporte telefónico — ingresá con tu cuenta de personal interno</p>

        <label htmlFor="correo">Correo</label>
        <input
          id="correo"
          type="email"
          autoComplete="username"
          {...register('correo', { required: 'El correo es obligatorio' })}
        />
        {errors.correo && <span className={styles.errorCampo}>{errors.correo.message}</span>}

        <label htmlFor="contrasena">Contraseña</label>
        <input
          id="contrasena"
          type="password"
          autoComplete="current-password"
          {...register('contrasena', { required: 'La contraseña es obligatoria' })}
        />
        {errors.contrasena && <span className={styles.errorCampo}>{errors.contrasena.message}</span>}

        {errorLogin && (
          <div className={styles.errorGeneral} role="alert">
            {errorLogin}
          </div>
        )}

        <button type="submit" className="boton boton-primario" disabled={isSubmitting}>
          {isSubmitting ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>
    </div>
  );
}

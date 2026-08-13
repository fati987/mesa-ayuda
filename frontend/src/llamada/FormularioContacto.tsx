import { useState } from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { crearLlamada } from './llamadaApi';
import type { LlamadaDetalleDto } from '../api/types';

interface FormValues {
  contactoTelefono: string;
  contactoNombreCompleto: string;
  contactoCorreo: string;
  duracionSegundos: number;
}

interface FormularioContactoProps {
  onCreada: (llamada: LlamadaDetalleDto) => void;
}

export function FormularioContacto({ onCreada }: FormularioContactoProps) {
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ defaultValues: { duracionSegundos: 60 } });

  const onSubmit: SubmitHandler<FormValues> = async (values) => {
    setErrorEnvio(null);
    try {
      const llamada = await crearLlamada({
        contactoTelefono: values.contactoTelefono.trim(),
        contactoNombreCompleto: values.contactoNombreCompleto.trim(),
        contactoCorreo: values.contactoCorreo.trim() || undefined,
        duracionSegundos: Number(values.duracionSegundos),
      });
      onCreada(llamada);
    } catch {
      setErrorEnvio('No se pudo registrar la llamada. Revisá los datos e intentá de nuevo.');
    }
  };

  return (
    <form className="tarjeta" onSubmit={handleSubmit(onSubmit)} noValidate>
      <h2>1. Contacto y llamada</h2>
      <p className="texto-secundario">Registrá al contacto que llama y los datos de la llamada en curso.</p>

      <div className="fila-campos">
        <div className="campo">
          <label htmlFor="contactoTelefono">Teléfono</label>
          <input
            id="contactoTelefono"
            type="tel"
            {...register('contactoTelefono', { required: 'El teléfono es obligatorio' })}
          />
          {errors.contactoTelefono && <span className="texto-secundario">{errors.contactoTelefono.message}</span>}
        </div>

        <div className="campo">
          <label htmlFor="contactoNombreCompleto">Nombre completo</label>
          <input
            id="contactoNombreCompleto"
            type="text"
            {...register('contactoNombreCompleto', { required: 'El nombre es obligatorio' })}
          />
          {errors.contactoNombreCompleto && (
            <span className="texto-secundario">{errors.contactoNombreCompleto.message}</span>
          )}
        </div>
      </div>

      <div className="fila-campos">
        <div className="campo">
          <label htmlFor="contactoCorreo">Correo (opcional)</label>
          <input
            id="contactoCorreo"
            type="email"
            {...register('contactoCorreo', {
              pattern: { value: /^\S+@\S+\.\S+$/, message: 'Correo inválido' },
            })}
          />
          {errors.contactoCorreo && <span className="texto-secundario">{errors.contactoCorreo.message}</span>}
        </div>

        <div className="campo">
          <label htmlFor="duracionSegundos">Duración de la llamada (segundos)</label>
          <input
            id="duracionSegundos"
            type="number"
            min={1}
            step={1}
            {...register('duracionSegundos', {
              required: true,
              valueAsNumber: true,
              min: { value: 1, message: 'Debe ser un número positivo' },
            })}
          />
          {errors.duracionSegundos && <span className="texto-secundario">{errors.duracionSegundos.message}</span>}
        </div>
      </div>

      {errorEnvio && <p style={{ color: 'var(--color-critico)' }}>{errorEnvio}</p>}

      <button type="submit" className="boton boton-primario" disabled={isSubmitting}>
        {isSubmitting ? 'Registrando…' : 'Continuar'}
      </button>
    </form>
  );
}

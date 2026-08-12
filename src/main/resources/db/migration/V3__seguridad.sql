-- =========================================================================
-- contraseña de usuario
-- =========================================================================
-- Se agrega con un DEFAULT (hash BCrypt real de la contraseña de prueba)
-- para poder ponerla NOT NULL de una sola vez sobre las filas existentes;
-- el DEFAULT se elimina después: las altas nuevas (vía CRUD de admin)
-- siempre deben enviar su propio hash explícito.
alter table usuario
    add column contrasena_hash varchar(100) not null
        default '$2a$10$FS1mDJvMzbgBjlgu4SWPAeCEQDo./cfVJfmCzPDv1J41ShBChuXY6';

alter table usuario
    alter column contrasena_hash drop default;

comment on column usuario.contrasena_hash is
    'Hash BCrypt de la contraseña. Contraseña de prueba para los 4 usuarios semilla de Sprint 1: Demo1234!';

-- =========================================================================
-- usuario ADMIN demo (no existía ninguno hasta ahora)
-- =========================================================================
-- Contraseña de prueba: Admin1234!
insert into usuario (nombre_completo, correo, rol, area_id, contrasena_hash, activo,
                      creado_en, actualizado_en, creado_por, actualizado_por, version)
select 'Admin Sistema', 'admin@mesaayuda.cl', 'ADMIN', a.id,
       '$2a$10$STMPv5F.DzfcnUKwgKniwelQy4yHCkJYozWVMPy.D4dVDdUaXacV.', true,
       now(), now(), 'sistema', 'sistema', 0
from area a
where a.nombre = 'Mesa de Ayuda';

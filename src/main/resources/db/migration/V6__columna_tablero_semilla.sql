-- Columnas de tablero por defecto para cada area sembrada en V2. Se excluye
-- NUEVO (un ticket telefonico no "vive" en ese estado mas alla de la
-- llamada misma: se resuelve en el momento o se deriva) y CERRADO (fuera
-- del flujo de trabajo activo).
insert into columna_tablero (area_id, nombre, estado_asociado, orden, creado_en, actualizado_en, creado_por, actualizado_por, version)
select a.id, v.nombre, v.estado_asociado, v.orden, now(), now(), 'sistema', 'sistema', 0
from area a
cross join (values
    ('Derivado',          'DERIVADO',          1),
    ('En progreso',       'EN_PROGRESO',       2),
    ('Esperando cliente', 'ESPERANDO_CLIENTE', 3),
    ('Resuelto',          'RESUELTO',          4)
) as v(nombre, estado_asociado, orden);

-- Datos semilla: catálogos (áreas, categorías, políticas SLA, calendario
-- laboral chileno con feriados) más un pequeño set de datos demo (usuarios,
-- contactos, llamadas, tickets con su historial inicial) para poder ejercitar
-- los tres endpoints de lectura de Sprint 1 con datos reales.

-- =========================================================================
-- áreas
-- =========================================================================
insert into area (nombre, recibe_llamadas, limite_wip_agente, creado_en, actualizado_en, creado_por, actualizado_por, version)
values
    ('Mesa de Ayuda',            true,  8, now(), now(), 'sistema', 'sistema', 0),
    ('Soporte Tecnico',          false, 6, now(), now(), 'sistema', 'sistema', 0),
    ('Redes y Conectividad',     false, 5, now(), now(), 'sistema', 'sistema', 0),
    ('Sistemas y Aplicaciones',  false, 5, now(), now(), 'sistema', 'sistema', 0),
    ('Recursos Humanos',         false, 4, now(), now(), 'sistema', 'sistema', 0);

-- =========================================================================
-- categorías
-- =========================================================================
insert into categoria (nombre, descripcion, creado_en, actualizado_en, creado_por, actualizado_por, version)
values
    ('Hardware',                    'Equipos, periféricos y componentes físicos', now(), now(), 'sistema', 'sistema', 0),
    ('Software',                    'Aplicaciones, sistemas operativos y licencias', now(), now(), 'sistema', 'sistema', 0),
    ('Redes',                       'Conectividad, VPN y acceso a internet', now(), now(), 'sistema', 'sistema', 0),
    ('Accesos y Credenciales',      'Cuentas, contraseñas y permisos', now(), now(), 'sistema', 'sistema', 0),
    ('Correo Electronico',          'Cuentas y clientes de correo', now(), now(), 'sistema', 'sistema', 0),
    ('Telefonia',                   'Anexos, centrales y telefonía IP', now(), now(), 'sistema', 'sistema', 0),
    ('Solicitud de Informacion',    'Consultas generales sin falla asociada', now(), now(), 'sistema', 'sistema', 0),
    ('Otros',                       'Casos que no encajan en otra categoría', now(), now(), 'sistema', 'sistema', 0);

-- =========================================================================
-- políticas SLA (una por prioridad, jornada de 8 horas hábiles)
-- =========================================================================
insert into politica_sla (nombre, prioridad, minutos_resolucion, minutos_primera_respuesta, creado_en, actualizado_en, creado_por, actualizado_por, version)
values
    ('SLA Critica', 'CRITICA', 240,  15,  now(), now(), 'sistema', 'sistema', 0),
    ('SLA Alta',    'ALTA',    480,  30,  now(), now(), 'sistema', 'sistema', 0),
    ('SLA Media',   'MEDIA',   1440, 60,  now(), now(), 'sistema', 'sistema', 0),
    ('SLA Baja',    'BAJA',    2400, 120, now(), now(), 'sistema', 'sistema', 0);

-- =========================================================================
-- calendario laboral chileno
-- =========================================================================
insert into calendario_laboral (
    nombre, zona_horaria, hora_inicio, hora_fin,
    aplica_lunes, aplica_martes, aplica_miercoles, aplica_jueves, aplica_viernes, aplica_sabado, aplica_domingo,
    creado_en, actualizado_en, creado_por, actualizado_por, version
)
values (
    'Calendario Chile Continental', 'America/Santiago', time '09:00', time '18:00',
    true, true, true, true, true, false, false,
    now(), now(), 'sistema', 'sistema', 0
);

-- feriados legales de Chile, año de referencia 2026 (fechas fijas, sin la
-- lógica de traslado al lunes más cercano que aplica a algunos feriados)
insert into feriado (calendario_laboral_id, fecha, descripcion, creado_en, actualizado_en, creado_por, actualizado_por, version)
select cl.id, v.fecha, v.descripcion, now(), now(), 'sistema', 'sistema', 0
from (values
    (date '2026-01-01', 'Año Nuevo'),
    (date '2026-04-03', 'Viernes Santo'),
    (date '2026-04-04', 'Sábado Santo'),
    (date '2026-05-01', 'Día Nacional del Trabajo'),
    (date '2026-05-21', 'Día de las Glorias Navales'),
    (date '2026-06-29', 'San Pedro y San Pablo'),
    (date '2026-07-16', 'Día de la Virgen del Carmen'),
    (date '2026-08-15', 'Asunción de la Virgen'),
    (date '2026-09-18', 'Independencia Nacional'),
    (date '2026-09-19', 'Glorias del Ejército'),
    (date '2026-10-12', 'Encuentro de Dos Mundos'),
    (date '2026-10-31', 'Día de las Iglesias Evangélicas y Protestantes'),
    (date '2026-11-01', 'Día de Todos los Santos'),
    (date '2026-12-08', 'Inmaculada Concepción'),
    (date '2026-12-25', 'Navidad')
) as v(fecha, descripcion)
cross join calendario_laboral cl
where cl.nombre = 'Calendario Chile Continental';

-- =========================================================================
-- datos demo: usuarios
-- =========================================================================
insert into usuario (nombre_completo, correo, rol, area_id, creado_en, actualizado_en, creado_por, actualizado_por, version)
select v.nombre_completo, v.correo, v.rol, a.id, now(), now(), 'sistema', 'sistema', 0
from (values
    ('Camila Rojas',    'camila.rojas@mesaayuda.cl',    'AGENTE',     'Mesa de Ayuda'),
    ('Diego Fuentes',   'diego.fuentes@mesaayuda.cl',   'AGENTE',     'Mesa de Ayuda'),
    ('Valentina Soto',  'valentina.soto@mesaayuda.cl',  'SUPERVISOR', 'Mesa de Ayuda'),
    ('Matias Prieto',   'matias.prieto@mesaayuda.cl',   'AGENTE',     'Soporte Tecnico')
) as v(nombre_completo, correo, rol, area_nombre)
join area a on a.nombre = v.area_nombre;

-- =========================================================================
-- datos demo: contactos
-- =========================================================================
insert into contacto (nombre_completo, telefono, correo, creado_en, actualizado_en, creado_por, actualizado_por, version)
values
    ('Fernanda Munoz',  '+56912345678', 'fernanda.munoz@example.cl',  now(), now(), 'sistema', 'sistema', 0),
    ('Rodrigo Paredes',  '+56923456789', 'rodrigo.paredes@example.cl', now(), now(), 'sistema', 'sistema', 0),
    ('Isidora Vega',     '+56934567890', null,                        now(), now(), 'sistema', 'sistema', 0);

-- =========================================================================
-- datos demo: llamadas (fecha_hora distinta en cada una para poder
-- referenciarlas sin ambigüedad desde los tickets)
-- =========================================================================
insert into llamada (contacto_id, usuario_atiende_id, fecha_hora, duracion_segundos, creado_en, actualizado_en, creado_por, actualizado_por, version)
select c.id, u.id, v.fecha_hora, v.duracion_segundos, now(), now(), 'sistema', 'sistema', 0
from (values
    ('+56912345678', 'camila.rojas@mesaayuda.cl',  timestamptz '2026-02-10 09:15:00-03', 420),
    ('+56923456789', 'diego.fuentes@mesaayuda.cl', timestamptz '2026-02-10 10:05:00-03', 300),
    ('+56934567890', 'camila.rojas@mesaayuda.cl',  timestamptz '2026-02-11 11:20:00-03', 180),
    ('+56912345678', 'diego.fuentes@mesaayuda.cl', timestamptz '2026-02-12 14:00:00-03', 240),
    ('+56923456789', 'camila.rojas@mesaayuda.cl',  timestamptz '2026-02-12 15:30:00-03', 500)
) as v(telefono, correo_atiende, fecha_hora, duracion_segundos)
join contacto c on c.telefono = v.telefono
join usuario u on u.correo = v.correo_atiende;

-- =========================================================================
-- datos demo: tickets
-- =========================================================================
insert into ticket (
    codigo, llamada_id, contacto_id, area_actual_id, categoria_id, usuario_creador_id,
    tipo, estado, prioridad, urgencia, impacto, origen,
    titulo, descripcion, resuelto_en_llamada, solucion, minutos_pausado,
    creado_en, actualizado_en, creado_por, actualizado_por, version
)
select
    v.codigo, l.id, c.id, a.id, cat.id, u.id,
    v.tipo, v.estado, v.prioridad, v.urgencia, v.impacto, 'TELEFONO',
    v.titulo, v.descripcion, v.resuelto_en_llamada, v.solucion, 0,
    now(), now(), 'sistema', 'sistema', 0
from (values
    ('SOP-2026-0001', timestamptz '2026-02-10 09:15:00-03', 'Mesa de Ayuda',   'Solicitud de Informacion', 'camila.rojas@mesaayuda.cl',
        'SOLICITUD', 'NUEVO', 'MEDIA', 'MEDIA', 'INDIVIDUAL',
        'Consulta sobre horario de atencion', 'El contacto pregunta por el horario de atencion presencial de la mesa de ayuda.',
        false, null),
    ('SOP-2026-0002', timestamptz '2026-02-10 10:05:00-03', 'Mesa de Ayuda',   'Accesos y Credenciales',   'diego.fuentes@mesaayuda.cl',
        'SOLICITUD', 'RESUELTO', 'BAJA', 'BAJA', 'INDIVIDUAL',
        'Restablecer contrasena de correo', 'El contacto olvido la contrasena de su casilla institucional.',
        true, 'Se restablecio la contrasena durante la llamada y se confirmo el acceso.'),
    ('SOP-2026-0003', timestamptz '2026-02-11 11:20:00-03', 'Soporte Tecnico', 'Hardware',                 'camila.rojas@mesaayuda.cl',
        'INCIDENTE', 'DERIVADO', 'ALTA', 'ALTA', 'AREA',
        'Impresora de sucursal no enciende', 'La impresora principal de la sucursal no enciende, afecta a todo el equipo.',
        false, null),
    ('SOP-2026-0004', timestamptz '2026-02-12 14:00:00-03', 'Soporte Tecnico', 'Redes',                    'diego.fuentes@mesaayuda.cl',
        'INCIDENTE', 'EN_PROGRESO', 'CRITICA', 'ALTA', 'ORGANIZACION',
        'Caida total de conectividad', 'No hay acceso a internet en ninguna estacion de trabajo desde esta manana.',
        false, null),
    ('SOP-2026-0005', timestamptz '2026-02-12 15:30:00-03', 'Mesa de Ayuda',   'Software',                 'camila.rojas@mesaayuda.cl',
        'SOLICITUD', 'ESPERANDO_CLIENTE', 'MEDIA', 'BAJA', 'INDIVIDUAL',
        'Instalacion de software de facturacion', 'Se pidio instalar el software de facturacion en un equipo nuevo, se espera confirmacion del contacto.',
        false, null)
) as v(codigo, fecha_hora_llamada, area_nombre, categoria_nombre, correo_creador,
       tipo, estado, prioridad, urgencia, impacto, titulo, descripcion, resuelto_en_llamada, solucion)
join llamada l on l.fecha_hora = v.fecha_hora_llamada
join contacto c on c.id = l.contacto_id
join area a on a.nombre = v.area_nombre
join categoria cat on cat.nombre = v.categoria_nombre
join usuario u on u.correo = v.correo_creador;

-- =========================================================================
-- datos demo: historial_estado inicial (NUEVO, estado_anterior nulo)
-- =========================================================================
insert into historial_estado (ticket_id, estado_anterior, estado_nuevo, usuario_id, comentario, creado_en, creado_por)
select t.id, null, 'NUEVO', t.usuario_creador_id, 'Ticket creado durante la llamada.', t.creado_en, 'sistema'
from ticket t
where t.codigo in ('SOP-2026-0001', 'SOP-2026-0002', 'SOP-2026-0003', 'SOP-2026-0004', 'SOP-2026-0005');

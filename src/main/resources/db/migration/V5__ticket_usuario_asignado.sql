-- Agente que tiene el ticket EN_PROGRESO actualmente. Nullable: un ticket
-- NUEVO/DERIVADO/RESUELTO/CERRADO puede no tener (o ya no tener) dueño.
-- Necesario para calcular el limite de WIP por agente (limite_wip_agente es
-- un limite por agente individual, no por area en conjunto).
alter table ticket
    add column usuario_asignado_id bigint references usuario(id);

create index idx_ticket_usuario_asignado_id on ticket (usuario_asignado_id);
create index idx_ticket_usuario_asignado_estado on ticket (usuario_asignado_id, estado);

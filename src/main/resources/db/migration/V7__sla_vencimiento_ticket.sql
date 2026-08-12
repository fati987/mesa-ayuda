-- Reloj de SLA: se setea recién en la primera derivación (regla invariable
-- #4), nunca en la creación. Nullable porque un ticket NUEVO (o resuelto
-- en la misma llamada) nunca llega a tener vencimiento.
alter table ticket add column fecha_vencimiento timestamptz;

-- Evita reprocesar el mismo ticket en cada corrida del job de escalamiento.
alter table ticket add column escalado boolean not null default false;

-- Compuesto, no parcial: un índice parcial con "where estado not in (...)"
-- no está garantizado de usarse con la consulta parametrizada del job (el
-- planner de Postgres no siempre puede probar que el predicado
-- parametrizado implica el del índice). El compuesto es robusto y de paso
-- resuelve el "order by fecha_vencimiento" sin sort adicional.
create index idx_ticket_estado_fecha_vencimiento on ticket (estado, fecha_vencimiento);

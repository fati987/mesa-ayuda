-- Secuencia para generar el codigo legible de ticket (SOP-AAAA-NNNN) desde
-- Java. Arranca en 6 porque los codigos SOP-2026-0001..0005 ya existen como
-- datos semilla insertados directo por SQL en V2.
--
-- Decision: la secuencia es global y NO se reinicia por año. Un ticket
-- creado en 2027 seguira la cuenta donde haya quedado (ej. SOP-2027-0042),
-- no volvera a 0001. Reiniciar por año exigiria una secuencia por año o un
-- contador compuesto; queda fuera de alcance de este sprint.
create sequence ticket_codigo_seq
    as bigint
    increment by 1
    start with 6
    no cycle;

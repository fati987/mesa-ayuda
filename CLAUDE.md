# Mesa de ayuda telefónica

Sistema de gestión de tickets de soporte originados en llamadas telefónicas.
El cliente llama, un agente levanta el ticket durante la llamada y lo resuelve en
línea o lo deriva a un área especializada. Ambos desenlaces quedan registrados.

Proyecto académico de portafolio. Prioriza claridad, trazabilidad y testabilidad
por sobre features. Es preferible una etapa terminada y desplegada que cinco a medias.

## Stack

- Java 21, Spring Boot 3 (Web, Data JPA, Security, Validation, Mail)
- PostgreSQL + Flyway para migraciones
- Maven
- JWT sin sesiones para autenticación
- WebSocket con STOMP para el tablero en vivo
- JUnit 5, Mockito, Testcontainers
- Frontend: React con Vite (fase posterior)

## Arquitectura

Paquetes agrupados por **feature**, no por tipo:

```
com.mesaayuda
├── config/        Security, CORS, WebSocket, Scheduler
├── auth/
├── usuario/
├── area/
├── contacto/
├── llamada/
├── ticket/        controller, service, repository, entity, dto, mapper, enums
├── derivacion/
├── tablero/
├── sla/           políticas, calendario laboral, escalamiento
├── metricas/
├── notificacion/
└── shared/        excepciones, ApiError, auditoría, paginación
```

Reglas de capa, sin excepciones:

- El controlador nunca ve una entidad JPA. Solo DTOs.
- El repositorio nunca ve un DTO. Solo entidades.
- Toda la lógica de negocio vive en el servicio.
- Las validaciones de permisos van en el servicio, nunca solo en el controlador.

## Dominio

### Entidades

`Contacto` (cliente, sin credenciales), `Llamada`, `Usuario` (personal interno),
`Area`, `Ticket`, `Derivacion`, `HistorialEstado`, `Comentario`, `Adjunto`,
`Categoria`, `Etiqueta`, `RegistroTiempo`, `PoliticaSla`, `CalendarioLaboral`,
`Feriado`, `ColumnaTablero`.

El cliente **no es usuario del sistema**: no se autentica, no crea tickets, no
consulta nada. Es un `Contacto` con teléfono y correo.

Agente y especialista son el **mismo rol** operando en áreas distintas. No existe
un rol `ESPECIALISTA`. Lo que los diferencia es su `area_id`.

### Enums

Siempre `@Enumerated(EnumType.STRING)`. Nunca `ORDINAL`.

```
Rol           → AGENTE, SUPERVISOR, ADMIN
TipoTicket    → INCIDENTE, SOLICITUD
EstadoTicket  → NUEVO, DERIVADO, EN_PROGRESO, ESPERANDO_CLIENTE, RESUELTO, CERRADO
Impacto       → INDIVIDUAL, AREA, ORGANIZACION
Urgencia      → BAJA, MEDIA, ALTA
Prioridad     → BAJA, MEDIA, ALTA, CRITICA
Visibilidad   → PUBLICO, INTERNO
Origen        → TELEFONO, CORREO, WEB, CHAT
```

### Máquina de estados

| Desde | Hacia | Guarda |
|---|---|---|
| NUEVO | RESUELTO | `resueltoEnLlamada` verdadero **y** el área recibe llamadas |
| NUEVO | DERIVADO | área destino distinta de la actual **y** motivo no vacío |
| DERIVADO | EN_PROGRESO | el agente pertenece al área destino **y** no supera su WIP |
| DERIVADO | DERIVADO | rebote entre áreas, permitido pero contabilizado |
| EN_PROGRESO | ESPERANDO_CLIENTE | pausa el reloj del SLA |
| ESPERANDO_CLIENTE | EN_PROGRESO | acumula el intervalo en `minutosPausado` |
| EN_PROGRESO | RESUELTO | exige solución registrada |
| EN_PROGRESO | DERIVADO | segunda derivación |
| RESUELTO | CERRADO | por job tras N días sin objeción, o manual |
| CERRADO | EN_PROGRESO | dentro de la ventana de reapertura |

Cualquier transición fuera de esta tabla lanza excepción. El grafo vive en el
enum `EstadoTicket`; las guardas que consultan base de datos viven en
`TransicionService`.

## Reglas de negocio invariables

Estas reglas no se negocian ni se simplifican "por ahora":

1. Solo áreas con `recibeLlamadas` verdadero pueden crear tickets de origen telefónico.
2. Un ticket solo salta de NUEVO a RESUELTO si se resolvió durante la llamada y lo
   cierra el área que la recibió. Sin esta guarda se falsea la métrica de FCR.
3. Un agente solo opera tickets cuya área actual coincide con la suya. El supervisor
   accede a todas las áreas.
4. El reloj del SLA de resolución arranca en la **derivación**, no en la creación.
5. El reloj solo corre en horario hábil según `CalendarioLaboral`, considerando
   feriados y zona horaria. No usar horas corridas.
6. El reloj se detiene mientras el ticket está en ESPERANDO_CLIENTE.
7. Un agente no puede superar el `limiteWipAgente` de su área en estado EN_PROGRESO.
8. Un comentario con visibilidad INTERNO nunca sale del sistema ni aparece en
   notificaciones al contacto.
9. `HistorialEstado` y `Derivacion` son de solo inserción. Nunca update ni delete.
10. Los tickets resueltos en llamada no ingresan a ningún tablero.
11. Nada se borra físicamente. Baja lógica con `activo` o `eliminadoEn`.

## Convenciones de código

- Nombres de dominio en español (`Ticket`, `Derivacion`, `AreaService`).
  Términos técnicos de Spring en inglés (`findByEstado`, `@RestController`).
- Toda entidad extiende una `@MappedSuperclass` con auditoría:
  `creadoEn`, `actualizadoEn`, `creadoPor`, `actualizadoPor`, `@Version`.
- `@Version` es obligatorio: dos agentes tomando el mismo ticket es una condición
  de carrera real.
- El esquema lo gobierna Flyway. `spring.jpa.hibernate.ddl-auto=validate`, jamás
  `update` ni `create`.
- Fechas y horas en `Instant` para timestamps, `LocalDate` para fechas puras.
  Zona horaria explícita en el calendario laboral, nunca implícita del servidor.
- Excepciones de dominio propias, capturadas en un `@RestControllerAdvice` que
  devuelve un `ApiError` uniforme.
- Toda operación que escribe en más de una tabla va en un único `@Transactional`.
  Cambiar estado y escribir historial es atómico o no es.
- El código visible del ticket es legible: `SOP-2026-0147`. No exponer UUIDs ni IDs
  autoincrementales en la API pública.
- Paginación obligatoria en todo endpoint que liste. Nunca devolver colecciones completas.

## Qué no hacer

- No usar `double` ni `float` para nada que se sume o compare.
- No poner `estado` como `String` libre.
- No exponer entidades JPA en respuestas HTTP.
- No filtrar por rol solo en el frontend.
- No agregar librerías nuevas sin justificar por qué no alcanza con lo del stack.
- No generar código de frontend hasta que la API esté probada con Postman.

## Metodología

Desarrollo en sprints de dos semanas. Cada sprint entrega algo funcional y probado.
No avanzar al siguiente sin cerrar el anterior.

1. Núcleo: entidades, Flyway, CRUD de tickets y categorías, manejo de excepciones
2. Auth y roles: JWT, permisos por rol y por área
3. Flujo: máquina de estados, historial, derivaciones, límites de WIP, tablero
4. SLA: calendario laboral, cálculo de vencimientos, job de escalamiento, correos
5. Métricas: FCR, lead time, cycle time, tasa de derivación, cumplimiento de SLA
6. Frontend
7. Tests de integración, Docker, despliegue

## Prioridades de testing

Empezar los tests por aquí, que es donde están los errores caros:

- `TransicionService`: cada par de estados, permitidas y prohibidas.
  Usar `@ParameterizedTest` con `MethodSource`.
- `CalendarioLaboral`: cálculo en horario hábil con feriados, fin de semana,
  cambio de día y reloj pausado.
- `AsignacionService`: respeto del límite de WIP.

## Comandos

```
./mvnw spring-boot:run
./mvnw test
./mvnw verify
docker compose up -d db
```

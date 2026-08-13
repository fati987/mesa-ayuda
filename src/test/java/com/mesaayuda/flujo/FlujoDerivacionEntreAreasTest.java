package com.mesaayuda.flujo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import com.mesaayuda.area.dto.AreaDto;
import com.mesaayuda.auth.dto.LoginRequest;
import com.mesaayuda.auth.dto.LoginResponse;
import com.mesaayuda.categoria.dto.CategoriaDto;
import com.mesaayuda.llamada.dto.LlamadaCrearRequest;
import com.mesaayuda.llamada.dto.LlamadaDetalleDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.tablero.dto.ColumnaTableroDto;
import com.mesaayuda.tablero.dto.TableroDto;
import com.mesaayuda.testsoporte.PostgresTestcontainer;
import com.mesaayuda.ticket.dto.TicketCrearRequest;
import com.mesaayuda.ticket.dto.TicketDerivarRequest;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResolverRequest;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

/**
 * Flujo completo de derivación entre áreas, end-to-end vía HTTP.
 *
 * Desviación respecto de lo sugerido originalmente: GET /api/tablero con un
 * AGENTE NO devuelve 403 para un área ajena — AccesoAreaValidator.resolverFiltroArea
 * simplemente IGNORA el parámetro `area` recibido y siempre resuelve al área
 * propia del agente (ver TableroService.obtener). Por eso el tablero de
 * "Soporte Tecnico" se consulta con el token de Matias (agente de esa área),
 * no con el de Camila + un parámetro area distinto: con el token de Camila
 * esa consulta devolvería silenciosamente SU PROPIO tablero (Mesa de Ayuda),
 * no el de Soporte Tecnico.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FlujoDerivacionEntreAreasTest extends PostgresTestcontainer {

    private static final String CORREO_CAMILA = "camila.rojas@mesaayuda.cl";
    private static final String CONTRASENA_CAMILA = "Demo1234!";
    private static final String CORREO_MATIAS = "matias.prieto@mesaayuda.cl";
    private static final String CONTRASENA_MATIAS = "Demo1234!";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void usarClientePatchCompatible() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void ticketDerivadoDesapareceDelTableroOrigenYApareceEnElDestino() {
        String tokenCamila = obtenerToken(CORREO_CAMILA, CONTRASENA_CAMILA);
        Map<String, Long> areas = idsDeAreasPorNombre(tokenCamila);
        Long areaMesaDeAyudaId = areas.get("Mesa de Ayuda");
        Long areaSoporteTecnicoId = areas.get("Soporte Tecnico");
        assertThat(areaMesaDeAyudaId).isNotNull();
        assertThat(areaSoporteTecnicoId).isNotNull();
        Long categoriaId = idsDeCategorias(tokenCamila).get(0);

        Long llamadaId = crearLlamada(tokenCamila, "+56911110003", "Contacto Derivacion Test");
        TicketCrearRequest crearRequest = new TicketCrearRequest(
                llamadaId, areaMesaDeAyudaId, categoriaId, TipoTicket.INCIDENTE, Prioridad.ALTA, Urgencia.ALTA,
                Impacto.AREA, "Falla que requiere Soporte Tecnico", "Descripción del incidente a derivar.", false, null);
        ResponseEntity<TicketDetalleDto> creado = restTemplate.exchange("/api/tickets", HttpMethod.POST,
                new HttpEntity<>(crearRequest, headersCon(tokenCamila)), TicketDetalleDto.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String codigo = creado.getBody().codigo();

        TicketDerivarRequest derivarRequest = new TicketDerivarRequest(areaSoporteTecnicoId, "Requiere revisión de Soporte Tecnico");
        ResponseEntity<TicketDetalleDto> derivado = restTemplate.exchange("/api/tickets/" + codigo + "/derivacion",
                HttpMethod.PATCH, new HttpEntity<>(derivarRequest, headersCon(tokenCamila)), TicketDetalleDto.class);
        assertThat(derivado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(derivado.getBody().estado()).isEqualTo(EstadoTicket.DERIVADO);
        assertThat(derivado.getBody().areaActualNombre()).isEqualTo("Soporte Tecnico");

        // Mesa de Ayuda (tablero de Camila, su propia área) ya no lo tiene.
        TableroDto tableroMesaDeAyuda = obtenerTablero(tokenCamila, areaMesaDeAyudaId);
        assertThat(codigoEstaEnAlgunaColumna(tableroMesaDeAyuda, codigo)).isFalse();

        // Soporte Tecnico (tablero de Matias, agente de esa área) sí lo
        // tiene, en la columna DERIVADO.
        String tokenMatias = obtenerToken(CORREO_MATIAS, CONTRASENA_MATIAS);
        TableroDto tableroSoporteTecnico = obtenerTablero(tokenMatias, areaSoporteTecnicoId);
        ColumnaTableroDto columnaDerivado = tableroSoporteTecnico.columnas().stream()
                .filter(c -> c.estadoAsociado() == EstadoTicket.DERIVADO)
                .findFirst().orElseThrow();
        assertThat(columnaDerivado.tickets().content()).extracting(TicketResumenDto::codigo).contains(codigo);

        // Matias toma el ticket.
        ResponseEntity<TicketDetalleDto> tomado = restTemplate.exchange("/api/tickets/" + codigo + "/toma",
                HttpMethod.PATCH, new HttpEntity<>(headersCon(tokenMatias)), TicketDetalleDto.class);
        assertThat(tomado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tomado.getBody().estado()).isEqualTo(EstadoTicket.EN_PROGRESO);
        assertThat(tomado.getBody().usuarioAsignadoNombre()).isEqualTo("Matias Prieto");

        // Matias lo resuelve.
        ResponseEntity<TicketDetalleDto> resuelto = restTemplate.exchange("/api/tickets/" + codigo + "/resolucion",
                HttpMethod.PATCH,
                new HttpEntity<>(new TicketResolverRequest("Se reemplazó el componente defectuoso."), headersCon(tokenMatias)),
                TicketDetalleDto.class);
        assertThat(resuelto.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resuelto.getBody().estado()).isEqualTo(EstadoTicket.RESUELTO);
    }

    private boolean codigoEstaEnAlgunaColumna(TableroDto tablero, String codigo) {
        return tablero.columnas().stream()
                .flatMap(c -> c.tickets().content().stream())
                .map(TicketResumenDto::codigo)
                .anyMatch(codigo::equals);
    }

    private TableroDto obtenerTablero(String token, Long areaId) {
        ResponseEntity<TableroDto> respuesta = restTemplate.exchange("/api/tablero?area=" + areaId,
                HttpMethod.GET, new HttpEntity<>(headersCon(token)), TableroDto.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return respuesta.getBody();
    }

    private String obtenerToken(String correo, String contrasena) {
        ResponseEntity<LoginResponse> respuesta = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(correo, contrasena), LoginResponse.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return respuesta.getBody().accessToken();
    }

    private Map<String, Long> idsDeAreasPorNombre(String token) {
        ResponseEntity<PaginaResponse<AreaDto>> respuesta = restTemplate.exchange("/api/areas", HttpMethod.GET,
                new HttpEntity<>(headersCon(token)), new ParameterizedTypeReference<PaginaResponse<AreaDto>>() {
                });
        return respuesta.getBody().content().stream().collect(Collectors.toMap(AreaDto::nombre, AreaDto::id));
    }

    private java.util.List<Long> idsDeCategorias(String token) {
        ResponseEntity<PaginaResponse<CategoriaDto>> respuesta = restTemplate.exchange("/api/categorias", HttpMethod.GET,
                new HttpEntity<>(headersCon(token)), new ParameterizedTypeReference<PaginaResponse<CategoriaDto>>() {
                });
        return respuesta.getBody().content().stream().map(CategoriaDto::id).collect(Collectors.toList());
    }

    private Long crearLlamada(String token, String telefono, String nombreCompleto) {
        LlamadaCrearRequest request = new LlamadaCrearRequest(telefono, nombreCompleto, null, 150);
        ResponseEntity<LlamadaDetalleDto> respuesta = restTemplate.exchange("/api/llamadas", HttpMethod.POST,
                new HttpEntity<>(request, headersCon(token)), LlamadaDetalleDto.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return respuesta.getBody().id();
    }

    private HttpHeaders headersCon(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}

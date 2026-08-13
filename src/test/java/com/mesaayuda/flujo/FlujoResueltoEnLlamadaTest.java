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
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResolverRequest;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

/**
 * Regla invariable #10: los tickets resueltos en llamada nunca ingresan a
 * ningún tablero. Verificación end-to-end vía POST /api/tickets +
 * PATCH .../resolucion + GET /api/tablero.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FlujoResueltoEnLlamadaTest extends PostgresTestcontainer {

    private static final String CORREO_CAMILA = "camila.rojas@mesaayuda.cl";
    private static final String CONTRASENA_CAMILA = "Demo1234!";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void usarClientePatchCompatible() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void ticketResueltoEnLlamadaNoApareceEnNingunaColumnaDelTablero() {
        String token = obtenerToken(CORREO_CAMILA, CONTRASENA_CAMILA);
        Long areaMesaDeAyudaId = idsDeAreasPorNombre(token).get("Mesa de Ayuda");
        assertThat(areaMesaDeAyudaId).isNotNull();
        Long categoriaId = idsDeCategorias(token).get(0);

        Long llamadaId = crearLlamada(token, "+56911110002", "Contacto Resuelto En Llamada");

        TicketCrearRequest crearRequest = new TicketCrearRequest(
                llamadaId, areaMesaDeAyudaId, categoriaId, TipoTicket.SOLICITUD, Prioridad.BAJA, Urgencia.BAJA,
                Impacto.INDIVIDUAL, "Consulta resuelta en la llamada", "El contacto quedó conforme en la misma llamada.",
                true, "Se explicó el procedimiento y quedó resuelto en el momento.");
        ResponseEntity<TicketDetalleDto> creado = restTemplate.exchange("/api/tickets", HttpMethod.POST,
                new HttpEntity<>(crearRequest, headersCon(token)), TicketDetalleDto.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketDetalleDto ticketCreado = creado.getBody();
        assertThat(ticketCreado.estado()).isEqualTo(EstadoTicket.NUEVO);
        assertThat(ticketCreado.resueltoEnLlamada()).isTrue();
        String codigo = ticketCreado.codigo();

        ResponseEntity<TicketDetalleDto> resuelto = restTemplate.exchange("/api/tickets/" + codigo + "/resolucion",
                HttpMethod.PATCH, new HttpEntity<>(new TicketResolverRequest(null), headersCon(token)), TicketDetalleDto.class);
        assertThat(resuelto.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resuelto.getBody().estado()).isEqualTo(EstadoTicket.RESUELTO);

        ResponseEntity<TableroDto> tablero = restTemplate.exchange("/api/tablero?area=" + areaMesaDeAyudaId,
                HttpMethod.GET, new HttpEntity<>(headersCon(token)), TableroDto.class);
        assertThat(tablero.getStatusCode()).isEqualTo(HttpStatus.OK);

        for (ColumnaTableroDto columna : tablero.getBody().columnas()) {
            boolean apareceEnEstaColumna = columna.tickets().content().stream()
                    .map(TicketResumenDto::codigo)
                    .anyMatch(codigo::equals);
            assertThat(apareceEnEstaColumna)
                    .as("El ticket %s (resuelto en llamada) no debería aparecer en la columna %s", codigo, columna.nombre())
                    .isFalse();
        }
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
        LlamadaCrearRequest request = new LlamadaCrearRequest(telefono, nombreCompleto, null, 90);
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

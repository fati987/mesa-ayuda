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
import com.mesaayuda.shared.excepcion.ApiError;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.testsoporte.PostgresTestcontainer;
import com.mesaayuda.ticket.dto.TicketCrearRequest;
import com.mesaayuda.ticket.dto.TicketDerivarRequest;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

/**
 * Regla invariable #3: un agente solo opera tickets cuya área actual
 * coincide con la suya.
 *
 * Desviación respecto de lo descrito originalmente: el 403 que dispara este
 * escenario NO es AgenteFueraDeAreaException (esa excepción vive en
 * TransicionService.aplicarToma y en la práctica es código muerto para este
 * endpoint) sino AccesoAreaDenegadoException, lanzada antes por
 * TicketService.cargarConAcceso — que valida exactamente la misma condición
 * (área del usuario == área actual del ticket) ANTES de siquiera llamar a
 * TransicionService. AccesoAreaDenegadoException extiende AccessDeniedException
 * de Spring Security, mapeada a 403 por GlobalExceptionHandler; en cambio
 * AgenteFueraDeAreaException extiende ReglaNegocioVioladaException (409), pero
 * ese camino nunca se alcanza a través del controller porque el guard de
 * cargarConAcceso corta antes. Este test verifica el comportamiento real.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AutorizacionAreaEndpointTest extends PostgresTestcontainer {

    private static final String CORREO_CAMILA = "camila.rojas@mesaayuda.cl";
    private static final String CONTRASENA_CAMILA = "Demo1234!";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void usarClientePatchCompatible() {
        // SimpleClientHttpRequestFactory (default de TestRestTemplate) no
        // manda PATCH real sobre HttpURLConnection; JdkClientHttpRequestFactory
        // (spring-web, sin dependencia nueva) envuelve java.net.http.HttpClient,
        // que sí soporta PATCH nativo.
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @Test
    void agenteNoPuedeTomarUnTicketQueYaNoEsDeSuArea() {
        String token = obtenerToken(CORREO_CAMILA, CONTRASENA_CAMILA);
        Map<String, Long> areas = idsDeAreasPorNombre(token);
        Long areaMesaDeAyudaId = areas.get("Mesa de Ayuda");
        Long areaSoporteTecnicoId = areas.get("Soporte Tecnico");
        assertThat(areaMesaDeAyudaId).isNotNull();
        assertThat(areaSoporteTecnicoId).isNotNull();
        Long categoriaId = idsDeCategorias(token).get(0);

        Long llamadaId = crearLlamada(token, "+56911110001", "Contacto Autorizacion Test");

        TicketCrearRequest crearRequest = new TicketCrearRequest(
                llamadaId, areaMesaDeAyudaId, categoriaId, TipoTicket.INCIDENTE, Prioridad.MEDIA, Urgencia.MEDIA,
                Impacto.INDIVIDUAL, "Ticket para test de autorización", "Descripción de prueba", false, null);
        ResponseEntity<TicketDetalleDto> creado = restTemplate.exchange("/api/tickets", HttpMethod.POST,
                new HttpEntity<>(crearRequest, headersCon(token)), TicketDetalleDto.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String codigo = creado.getBody().codigo();

        TicketDerivarRequest derivarRequest = new TicketDerivarRequest(areaSoporteTecnicoId, "Requiere especialista técnico");
        ResponseEntity<TicketDetalleDto> derivado = restTemplate.exchange("/api/tickets/" + codigo + "/derivacion",
                HttpMethod.PATCH, new HttpEntity<>(derivarRequest, headersCon(token)), TicketDetalleDto.class);
        assertThat(derivado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(derivado.getBody().areaActualNombre()).isEqualTo("Soporte Tecnico");

        // Camila (Mesa de Ayuda) intenta tomar un ticket cuya área actual
        // ya es Soporte Tecnico.
        ResponseEntity<ApiError> intentoToma = restTemplate.exchange("/api/tickets/" + codigo + "/toma",
                HttpMethod.PATCH, new HttpEntity<>(headersCon(token)), ApiError.class);

        assertThat(intentoToma.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(intentoToma.getBody().mensaje()).isEqualTo("El área de su usuario no coincide con el área actual del ticket");
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
        LlamadaCrearRequest request = new LlamadaCrearRequest(telefono, nombreCompleto, null, 120);
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

package com.mesaayuda.flujo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mesaayuda.auth.dto.LoginRequest;
import com.mesaayuda.auth.dto.LoginResponse;
import com.mesaayuda.shared.excepcion.ApiError;
import com.mesaayuda.testsoporte.PostgresTestcontainer;

/**
 * POST /api/auth/login y el 401 uniforme que dispara JwtAuthenticationEntryPoint
 * para cualquier endpoint protegido sin credenciales válidas.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AutenticacionEndpointTest extends PostgresTestcontainer {

    private static final String CORREO_CAMILA = "camila.rojas@mesaayuda.cl";
    private static final String CONTRASENA_CAMILA = "Demo1234!";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loginConCredencialesValidasDevuelveTokens() {
        ResponseEntity<LoginResponse> respuesta = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(CORREO_CAMILA, CONTRASENA_CAMILA), LoginResponse.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponse body = respuesta.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isNotBlank();
        assertThat(body.refreshToken()).isNotBlank();
        assertThat(body.tipoToken()).isEqualTo("Bearer");
        assertThat(body.expiraEnSegundos()).isGreaterThan(0);
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() {
        ResponseEntity<ApiError> respuesta = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(CORREO_CAMILA, "ContrasenaIncorrecta1!"), ApiError.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ApiError body = respuesta.getBody();
        assertThat(body).isNotNull();
        assertThat(body.mensaje()).isNotBlank();
        assertThat(body.status()).isEqualTo(401);
    }

    @Test
    void listarTicketsSinAuthorizationDevuelve401() {
        ResponseEntity<ApiError> respuesta = restTemplate.getForEntity("/api/tickets", ApiError.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listarTicketsConBearerMalFormadoDevuelve401() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-jwt-valido");
        ResponseEntity<ApiError> respuesta = restTemplate.exchange(
                "/api/tickets", HttpMethod.GET, new HttpEntity<>(headers), ApiError.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

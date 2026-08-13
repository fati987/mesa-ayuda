package com.mesaayuda.testsoporte;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @ActiveProfiles se hereda a todas las subclases (comportamiento estándar
 * del TestContext framework de Spring). "dev" carga mail/cors/logging de
 * desarrollo tal cual; "test" (application-test.yml) se aplica encima y
 * solo agrega el secreto JWT fijo — ninguno de los dos reemplaza al
 * application.yml base, a diferencia de un application.yml sin calificar en
 * test/resources (ver el comentario en application-test.yml).
 */
@ActiveProfiles({ "dev", "test" })
public abstract class PostgresTestcontainer {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("mesa_ayuda")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}

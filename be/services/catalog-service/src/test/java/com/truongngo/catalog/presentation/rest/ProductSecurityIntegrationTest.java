package com.truongngo.catalog.presentation.rest;

import com.truongngo.catalog.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "FIREBASE_PROJECT_ID=catalog-test")
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
class ProductSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsCreateProductWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Wireless mouse",
                            "description": "Bluetooth connection",
                            "price": 250000
                        }
                        """)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsCreateProductWhenUserHasNoAdminRole() throws Exception {
        Jwt jwt = Jwt.withTokenValue("user-token")
                .header("alg", "RS256")
                .subject("user-123")
                .claim("roles", java.util.List.of("USER"))
                .build();

        when(jwtDecoder.decode("user-token")).thenReturn(jwt);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Wireless mouse",
                                    "description": "Bluetooth connection",
                                    "price": 250000
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void createsProductWhenUserHasAdminRole() throws Exception {
        Jwt jwt = Jwt.withTokenValue("admin-token")
                .header("alg", "RS256")
                .subject("admin-123")
                .claim("roles", java.util.List.of("ADMIN"))
                .build();

        when(jwtDecoder.decode("admin-token")).thenReturn(jwt);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Wireless mouse",
                                    "description": "Bluetooth connection",
                                    "price": 250000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Wireless mouse"))
                .andExpect(jsonPath("$.price").value(250000));
    }
}

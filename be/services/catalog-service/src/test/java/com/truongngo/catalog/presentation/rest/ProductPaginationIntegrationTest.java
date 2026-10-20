package com.truongngo.catalog.presentation.rest;

import com.truongngo.catalog.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "FIREBASE_PROJECT_ID=catalog-test")
@AutoConfigureMockMvc
@Transactional
@Import(TestDatabaseConfig.class)
public class ProductPaginationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsEmptyPageWhenNoProductsExist() throws Exception {
        jdbcTemplate.update("DELETE FROM products");

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void returnsExpectedPageSizesAndTotals() throws Exception {
        String deleteAllQuery = "DELETE FROM products";

        jdbcTemplate.update(deleteAllQuery);

        jdbcTemplate.update("""
                INSERT INTO products (id, name, description, price, created_at)
                VALUES
                    ('00000000-0000-0000-0000-000000000001',
                     'Wireless mouse','Bluetooth connection', 100,
                     '2026-01-01T10:00:00Z'),
                    ('00000000-0000-0000-0000-000000000002',
                     'Pen','Bluetooth connection', 200,
                     '2026-01-01T11:00:00Z'),
                    ('00000000-0000-0000-0000-000000000003',
                     'Wireless display','', 300,
                     '2026-01-01T11:00:00Z')
                """);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/products")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void returnsProductsInStableOrderAcrossPages() throws Exception {
        jdbcTemplate.update("DELETE FROM products");

        jdbcTemplate.update("""
                INSERT INTO products (id, name, price, created_at)
                VALUES
                    ('00000000-0000-0000-0000-000000000001',
                     'Older product', 100,
                     '2026-01-01T10:00:00Z'),
                    ('00000000-0000-0000-0000-000000000002',
                     'Newer product A', 200,
                     '2026-01-01T11:00:00Z'),
                    ('00000000-0000-0000-0000-000000000003',
                     'Newer product B', 300,
                     '2026-01-01T11:00:00Z')
                """);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id")
                        .value("00000000-0000-0000-0000-000000000002"))
                .andExpect(jsonPath("$.items[1].id")
                        .value("00000000-0000-0000-0000-000000000003"));

        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id")
                        .value("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void searchesProductsIgnoringCaseAndPaginatesMatchingResults() throws Exception {
        jdbcTemplate.update("DELETE FROM products");

        jdbcTemplate.update("""
                INSERT INTO products (id, name, price, created_at)
                VALUES
                    ('00000000-0000-0000-0000-000000000001',
                     'Older product', 100,
                     '2026-01-01T10:00:00Z'),
                    ('00000000-0000-0000-0000-000000000002',
                     'Newer product A', 200,
                     '2026-01-01T11:00:00Z'),
                    ('00000000-0000-0000-0000-000000000003',
                     'Newer product B', 300,
                     '2026-01-01T11:00:00Z')
                """);

        mockMvc.perform(get("/api/products")
                        .param("keyword", "older")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id")
                        .value("00000000-0000-0000-0000-000000000001"));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "NEWER")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id")
                        .value("00000000-0000-0000-0000-000000000002"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "NEWER")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id")
                        .value("00000000-0000-0000-0000-000000000003"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void returnsAllProductsWhenKeywordIsBlank() throws Exception {
        jdbcTemplate.update("DELETE FROM products");

        jdbcTemplate.update("""
                INSERT INTO products (id, name, price, created_at)
                VALUES
                    ('00000000-0000-0000-0000-000000000001',
                     'Older product', 100,
                     '2026-01-01T10:00:00Z'),
                    ('00000000-0000-0000-0000-000000000002',
                     'Newer product A', 200,
                     '2026-01-01T11:00:00Z'),
                    ('00000000-0000-0000-0000-000000000003',
                     'Newer product B', 300,
                     '2026-01-01T11:00:00Z')
                """);

        mockMvc.perform(get("/api/products")
                        .param("keyword", "    ")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void returnsEmptyPageWhenKeywordDoesNotMatchAnyProduct() throws Exception {
        jdbcTemplate.update("DELETE FROM products");

        jdbcTemplate.update("""
                INSERT INTO products (id, name, price, created_at)
                VALUES
                    ('00000000-0000-0000-0000-000000000001',
                     'Older product', 100,
                     '2026-01-01T10:00:00Z'),
                    ('00000000-0000-0000-0000-000000000002',
                     'Newer product A', 200,
                     '2026-01-01T11:00:00Z'),
                    ('00000000-0000-0000-0000-000000000003',
                     'Newer product B', 300,
                     '2026-01-01T11:00:00Z')
                """);

        mockMvc.perform(get("/api/products")
                        .param("keyword", "abc")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}

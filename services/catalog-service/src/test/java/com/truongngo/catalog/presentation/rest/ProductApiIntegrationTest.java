package com.truongngo.catalog.presentation.rest;

import com.truongngo.catalog.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
class ProductApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsProductAndReadsItById() throws Exception {
        String requestBody = """
                {
                    "name": "Wireless mouse",
                    "description": "Bluetooth connection",
                    "price": 250000.00
                }
                """;

        // Gửi POST qua controller thật và lưu vào PostgreSQL.
        String responseBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(responseBody)
                .get("id").asString();

        assertDoesNotThrow(() -> UUID.fromString(id));

        // Gửi request GET riêng để đọc lại dữ liệu vừa lưu.
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Wireless mouse"))
                .andExpect(jsonPath("$.description")
                        .value("Bluetooth connection"))
                .andExpect(jsonPath("$.price").value(250000.0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void rejectsNegativePriceWithoutSaving() throws Exception {
        Long countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products",
                Long.class
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Invalid product",
                                "price": -100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        Long countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products",
                Long.class
        );

        assertEquals(countBefore, countAfter);
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/api/products/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void updatesProductAndPersistsChanges() throws Exception {
        // 1. Tạo sản phẩm ban đầu.
        String createdBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Original product",
                                "description": "Original description",
                                "price": 100000
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var created = objectMapper.readTree(createdBody);
        String id = created.get("id").asString();
        String createdAt = created.get("createdAt").asString();

        // 2. Cập nhật sản phẩm.
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated product",
                                "description": "Updated description",
                                "price": 300000
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated product"))
                .andExpect(jsonPath("$.price").value(300000.0))
                .andExpect(jsonPath("$.createdAt").value(createdAt));

        // 3. Đọc lại để xác nhận dữ liệu đã được lưu.
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated product"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.price").value(300000.0))
                .andExpect(jsonPath("$.createdAt").value(createdAt));
    }

    @Test
    void rejectsInvalidUpdateWithoutChangingProduct() throws Exception {
        String createdBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Original product",
                                "description": "Original description",
                                "price": 100000
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var created = objectMapper.readTree(createdBody);
        String id = created.get("id").asText();

        // Lấy dữ liệu trước khi cập nhật để so sánh sau đó.
        String beforeBody = mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Changed name",
                                "description": "Changed description",
                                "price": -1
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        String afterBody = mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(
                objectMapper.readTree(beforeBody),
                objectMapper.readTree(afterBody)
        );
    }

    @Test
    void returnsNotFoundWithoutCreatingProductOnUpdate() throws Exception {
        UUID missingId = UUID.randomUUID();

        Long countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products",
                Long.class
        );

        mockMvc.perform(put("/api/products/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Missing product",
                                "description": "Should not be created",
                                "price": 250000
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        Long countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products",
                Long.class
        );

        assertEquals(countBefore, countAfter);

        mockMvc.perform(get("/api/products/{id}", missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesProductAndCannotReadItAgain() throws Exception {
        // 1. Tạo sản phẩm.
        String createdBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Product to delete",
                                "description": "Delete integration test",
                                "price": 50000
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createdBody)
                .get("id").asString();

        // 2. Xác nhận sản phẩm tồn tại.
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // 3. Xóa sản phẩm.
        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // 4. Xác nhận không còn đọc được sản phẩm.
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());

        // 5. Kiểm tra trực tiếp database.
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE id = ?",
                Long.class,
                UUID.fromString(id)
        );

        assertEquals(0L, count.longValue());
    }
}

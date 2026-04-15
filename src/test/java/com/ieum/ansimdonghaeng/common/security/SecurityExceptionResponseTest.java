package com.ieum.ansimdonghaeng.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityExceptionResponseTest.AdminOnlyTestController.class)
class SecurityExceptionResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthorizedRequestReturnsJsonBody() throws Exception {
        mockMvc.perform(get("/api/v1/projects/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_401"))
                .andExpect(jsonPath("$.error.status").value(401))
                .andExpect(jsonPath("$.error.path").value("/api/v1/projects/me"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbiddenRequestReturnsJsonBody() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin-only"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_403"))
                .andExpect(jsonPath("$.error.status").value(403))
                .andExpect(jsonPath("$.error.path").value("/api/v1/test/admin-only"));
    }

    @RestController
    static class AdminOnlyTestController {

        @GetMapping("/api/v1/test/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<Map<String, String>> adminOnly() {
            return ApiResponse.success(Map.of("status", "ok"));
        }
    }
}

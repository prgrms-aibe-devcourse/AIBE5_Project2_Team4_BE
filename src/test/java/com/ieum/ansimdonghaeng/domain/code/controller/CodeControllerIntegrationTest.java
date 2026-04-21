package com.ieum.ansimdonghaeng.domain.code.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CodeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void publicCodeEndpointsReturnOnlyActiveCodes() throws Exception {
        jdbcTemplate.update(
                "insert into AVAILABLE_TIME_SLOT_CODE (TIME_SLOT_CODE, TIME_SLOT_NAME, START_MINUTE, END_MINUTE, SORT_ORDER, ACTIVE_YN) values (?, ?, ?, ?, ?, ?)",
                "INACTIVE_SLOT",
                "Inactive Slot",
                1020,
                1080,
                99,
                "N"
        );

        mockMvc.perform(get("/api/v1/codes/project-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("HOSPITAL_COMPANION"))
                .andExpect(jsonPath("$.data[0].sortOrder").value(1));

        mockMvc.perform(get("/api/v1/codes/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("SEOUL_GANGNAM"))
                .andExpect(jsonPath("$.data[0].regionLevel").value(2));

        mockMvc.perform(get("/api/v1/codes/available-time-slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].code", hasItem("MORNING")))
                .andExpect(jsonPath("$.data[*].code", not(hasItem("INACTIVE_SLOT"))))
                .andExpect(jsonPath("$.data[0].startMinute").value(540))
                .andExpect(jsonPath("$.data[0].endMinute").value(720));
    }
}

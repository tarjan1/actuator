package com.huawei.gd.actuator.health.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.huawei.gd.actuator.health.core.Health;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(value = "classpath:actuator.xml")
public class HealthControllerTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    @Autowired
    @Qualifier("mock1HealthIndicator")
    private Mock1HealthIndicator mock1HealthIndicator;
    @Autowired
    @Qualifier("mock2HealthIndicator")
    private Mock2HealthIndicator mock2HealthIndicator;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void testHealth() throws Exception {
        this.mockMvc.perform(get("/actuator/health").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.status").value(Health.DOWN));
    }
}
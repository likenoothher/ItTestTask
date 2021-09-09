package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthTestConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties")
class HelloRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void whenUserUnauthenticated_thenReturnStatus401() throws Exception {
        this.mockMvc.perform(get("/hello"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Authentication is required")));
    }

    @Test
    @WithUserDetails("test@email.com")
    public void whenUserAuthenticated_thenReturnStatus200AndGreeting() throws Exception {
        when(userService.findByEmail("test@email.com"))
                .thenReturn(new User(1L, "test@email.com", "userFirstName",
                        "userLastName", "password", UserRole.USER));

        this.mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, userFirstName")));

    }
}

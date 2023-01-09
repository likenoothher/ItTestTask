package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.repository.UserRepository;
import com.azierets.restapijwt.util.RabbitTestListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RabbitTestListener listener;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    public void whenRegisterRequestBodyCorrect_thenReturnStatus200AndToken() throws Exception {
        String testEmailName = "test@email.com";
        int expectedMessageAmount = 1;
        CountDownLatch rabbitLock = new CountDownLatch(expectedMessageAmount);

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setEmail(testEmailName);
        requestDto.setPassword("password");
        requestDto.setFirstName("firstName");
        requestDto.setLastName("lastName");

        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        listener.setLatch(rabbitLock);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testEmailName)))
                .andExpect(jsonPath("$.token", matchesPattern(tokenRegex)));

        assertTrue(rabbitLock.await(5, TimeUnit.SECONDS));
        assertEquals(expectedMessageAmount, listener.getMessageCounter());
        assertTrue(userRepository.existsByEmail("test@email.com"));
    }

    @Test
    public void whenLoginRequestBodyCorrect_thenReturnStatus200AndToken() throws Exception {
        int expectedMessageAmount = 1;
        CountDownLatch rabbitLock = new CountDownLatch(expectedMessageAmount);

        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("test@email.com");
        requestDto.setPassword("password");

        User registeredUser = new User();
        registeredUser.setPassword(requestDto.getPassword());
        registeredUser.setEmail(requestDto.getEmail());
        registeredUser.setFirstName("testFirstName");
        registeredUser.setLastName("testLastName");
        registeredUser.setRole(UserRole.USER);

        when(authenticationManager.authenticate(any())).thenReturn(null);

        userRepository.save(registeredUser);

        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        listener.setLatch(rabbitLock);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", Matchers.is("test@email.com")))
                .andExpect(jsonPath("$.token", matchesPattern(tokenRegex)));

        assertTrue(rabbitLock.await(5, TimeUnit.SECONDS));
        assertEquals(expectedMessageAmount, listener.getMessageCounter());
    }
}

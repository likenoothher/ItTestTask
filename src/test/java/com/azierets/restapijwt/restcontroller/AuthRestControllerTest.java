package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.CredentialsDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.security.jwt.JwtService;
import com.azierets.restapijwt.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties")
class AuthRestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @Test
    public void whenLoginRequestBodyCorrect_thenReturnStatus200AndToken() throws Exception {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("test@email.com");
        requestDto.setPassword("password");

        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setPassword(requestDto.getPassword());
        registeredUser.setEmail(requestDto.getEmail());
        registeredUser.setFirstName("testFirstName");
        registeredUser.setLastName("testLastName");

        String testToken = jwtService.generateToken(registeredUser);

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getEmail(),
                requestDto.getPassword()))).thenReturn(null);
        when(userService.authenticate(requestDto)).thenReturn(new CredentialsDto(registeredUser.getEmail(),
                testToken));
        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", Matchers.is("test@email.com")))
                .andExpect(jsonPath("$.token", matchesPattern(tokenRegex)));
    }

    @Test
    public void whenLoginRequestBodyIncorrect_thenReturnStatus400() throws Exception {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("");
        requestDto.setPassword("");

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", Matchers.is("must not be blank")))
                .andExpect(jsonPath("$.errors[1].message", Matchers.is("must not be blank")));
    }

    @Test
    public void whenLoginCredentialsIncorrect_thenReturnStatus403() throws Exception {
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("test@email.com");
        requestDto.setPassword("password");

        BadCredentialsException badCredentialsException = new BadCredentialsException("invalid email or password");

        when(userService.authenticate(requestDto)).thenThrow(badCredentialsException);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadCredentialsException))
                .andExpect(result -> assertEquals("invalid email or password",
                        result.getResolvedException().getMessage()));
    }


    @Test
    public void whenRegisterRequestBodyCorrect_thenReturnStatus200() throws Exception {
        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setEmail("test@email.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("firstName");
        requestDto.setLastName("lastName");

        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setPassword(requestDto.getPassword());
        registeredUser.setEmail(requestDto.getEmail());
        registeredUser.setFirstName(requestDto.getFirstName());
        registeredUser.setLastName(requestDto.getLastName());

        String testToken = jwtService.generateToken(registeredUser);
        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        when(userService.register(requestDto)).thenReturn(new CredentialsDto(registeredUser.getEmail(),
                testToken));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(registeredUser.getEmail())))
                .andExpect(jsonPath("$.token", matchesPattern(tokenRegex)));
    }

    @Test
    public void whenRegisterRequestBodyCorrectButUserAlreadyExists_thenReturnStatus400() throws Exception {
        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setEmail("test@email.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("firstName");
        requestDto.setLastName("lastName");

        when(userService.register(requestDto)).thenThrow(UserIsAlreadyRegisteredException.class);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserIsAlreadyRegisteredException));

    }

    @Test
    public void whenRegisterRequestBodyIncorrect_thenReturnStatus400() throws Exception {
        RegisterRequestDto invalidRequestDto = new RegisterRequestDto();
        invalidRequestDto.setEmail("");
        invalidRequestDto.setPassword("");
        invalidRequestDto.setFirstName("");
        invalidRequestDto.setLastName("lastName");

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(invalidRequestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors[0].message", Matchers.is("must not be blank")))
                .andExpect(jsonPath("$.errors[1].message", Matchers.is("must not be blank")))
                .andExpect(jsonPath("$.errors[2].message", Matchers.is("must not be blank")));

    }
}

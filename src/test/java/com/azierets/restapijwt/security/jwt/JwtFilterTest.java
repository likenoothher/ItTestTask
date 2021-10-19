package com.azierets.restapijwt.security.jwt;

import com.azierets.restapijwt.exceptionhandler.exception.JwtAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private MockFilterChain chain;

    @InjectMocks
    private JwtFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private UserDetails userDetails;

    @BeforeEach
    private void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public boolean isAccountNonExpired() {
                return false;
            }

            @Override
            public boolean isAccountNonLocked() {
                return false;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

    @Test
    public void whenDoFilterAndTokenValid_thenFilterDoChain() throws ServletException, IOException {
        String token = "token";
        String userEmail = "test@email.com";

        when(jwtService.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getUserEmail(token)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        doNothing().when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(jwtService, times(1)).extractTokenFromRequest(request);
        verify(jwtService, times(1)).isTokenValid(token);
        verify(jwtService, times(1)).getUserEmail(token);
        verify(userDetailsService, times(1)).loadUserByUsername(userEmail);
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void whenDoFilterAndTokenValidationThrowException_thenSetResponseStatus401() throws ServletException, IOException {
        String token = "token";
        String userEmail = "test@email.com";

        when(jwtService.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenThrow(new JwtAuthException("Token invalid"));

        filter.doFilter(request, response, chain);

        int expectedStatus = 401;
        assertEquals(expectedStatus, response.getStatus());

        verify(jwtService, times(1)).extractTokenFromRequest(request);
        verify(jwtService, times(1)).isTokenValid(token);
        verify(jwtService, times(0)).getUserEmail(token);
        verify(userDetailsService, times(0)).loadUserByUsername(userEmail);
        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    public void whenDoFilterAndTokenInvalid_thenFilterDoChainWithoutAuth() throws ServletException, IOException {
        String token = "token";
        String userEmail = "test@email.com";

        when(jwtService.extractTokenFromRequest(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenReturn(false);
        doNothing().when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(jwtService, times(1)).extractTokenFromRequest(request);
        verify(jwtService, times(1)).isTokenValid(token);
        verify(jwtService, times(0)).getUserEmail(token);
        verify(userDetailsService, times(0)).loadUserByUsername(userEmail);
        verify(chain, times(1)).doFilter(request, response);
    }
}

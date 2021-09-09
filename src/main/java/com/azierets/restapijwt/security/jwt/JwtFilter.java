package com.azierets.restapijwt.security.jwt;

import com.azierets.restapijwt.exceptionhandler.exception.JwtAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            String token = jwtService.extractTokenFromRequest(request);
            if (token != null && jwtService.isTokenValid(token)) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtService.getUserEmail(token));
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);
            }

        } catch (JwtAuthException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response
                    .getWriter()
                    .write(e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }
}

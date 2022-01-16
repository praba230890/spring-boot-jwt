package com.example.demo.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws javax.servlet.ServletException, java.io.IOException {
        
            if(request.getServletPath().equals("/api/token/login") || request.getServletPath().equals("/api/token/refresh")) {
                filterChain.doFilter(request, response);
                return;
            } else {
                String token = request.getHeader(HttpHeaders.AUTHORIZATION);
                log.info("token: {}", token);
                if(token == null || !token.startsWith("Bearer ")){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String jwt = token.substring(7);
                try {
                    DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256("secret".getBytes())).build().verify(jwt);
                    String username = decodedJWT.getSubject();
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = Stream.of(roles).map(SimpleGrantedAuthority::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);
                } catch (Exception e) {
                    log.error("JWT token is invalid: ", e.getMessage());
                    response.setHeader("error", e.getMessage());
                    // response.sendError(HttpStatus.FORBIDDEN.value());
                    Map<String, String> error = new HashMap<>();
                    error.put("error", e.getMessage());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
    }
}

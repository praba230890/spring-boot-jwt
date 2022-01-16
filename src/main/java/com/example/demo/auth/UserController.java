package com.example.demo.auth;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/v1/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping("/v1/users")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        URI uri = URI.create(ServletUriComponentsBuilder.fromContextPath(request).path("/api/v1/users/").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @PostMapping("/v1/roles")
    public ResponseEntity<Role> saveUser(@RequestBody Role role) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        URI uri = URI.create(ServletUriComponentsBuilder.fromContextPath(request).path("/api/v1/roles/").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    @PostMapping("/v1/roles/addtouser")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
        userService.addRoleToUser(form.getUsername(), form.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh_token = request.getHeader(HttpHeaders.AUTHORIZATION);
                log.info("refresh_token: {}", refresh_token);
                if(refresh_token == null || !refresh_token.startsWith("Bearer ")){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
                String jwt = refresh_token.substring(7);
                try {
                    DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256("secret".getBytes())).build().verify(jwt);
                    String username = decodedJWT.getSubject();
                    User user = userService.getUser(username);
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                    String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis()+ 10*60*1000))
                        .withIssuer(request.getRequestURI().toString())
                        .withClaim("roles", user.getRoles().stream().map(Role   ::getName).collect(Collectors.toList()))
                        .sign(algorithm);
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token", access_token);
                    tokens.put("refresh_token", refresh_token);
                    new ObjectMapper().writeValue(response.getWriter(), tokens);
                } catch (Exception e) {
                    log.error("JWT token is invalid: ", e.getMessage());
                    response.setHeader("error", e.getMessage());
                    Map<String, String> error = new HashMap<>();
                    error.put("error", e.getMessage());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
    }
}

@Data
class RoleToUserForm {
    private String username;
    private String roleName;
}
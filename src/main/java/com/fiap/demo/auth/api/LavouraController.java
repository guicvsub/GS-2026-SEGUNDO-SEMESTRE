package com.fiap.demo.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.demo.auth.service.TokenService;

@RestController
public class LavouraController {

    private final TokenService tokenService;

    public LavouraController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/api/lavoura")
    public String lavoura(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token nao fornecido");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
        }
        String token = authorization.substring(7);
        if (tokenService.isExpired(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
        }
        return "acesso liberado";
    }
}

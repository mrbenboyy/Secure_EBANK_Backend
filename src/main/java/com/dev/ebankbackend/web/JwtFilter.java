package com.dev.ebankbackend.web;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends GenericFilterBean {

    private final String SECRET_KEY = "rGJ5Y3R4Z2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2"; // Clé secrète pour signer le JWT

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String header = httpRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response); // Si pas de token, continuer le filtre
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            // Parse le JWT et récupère les claims
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject(); // Récupère l'utilisateur
            List<String> roles = claims.get("roles", List.class); // Récupère les rôles

            if (username != null && roles != null) {
                // Convertit les rôles en autorités Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Crée l'objet d'authentification
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                // Définit le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Si le token est invalide, continuer le filtre sans authentification
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response); // Continuer la chaîne de filtres
    }
}

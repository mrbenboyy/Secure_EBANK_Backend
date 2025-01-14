package com.dev.ebankbackend.web;

import com.dev.ebankbackend.dtos.ChangePasswordRequest;
import com.dev.ebankbackend.dtos.CustomerDTO;
import com.dev.ebankbackend.services.BankAccountServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final String SECRET_KEY = "rGJ5Y3R4Z2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2Y2F1dmR2"; // Clé secrète pour signer le JWT
    private final BankAccountServiceImpl bankAccountServiceImpl;

    public AuthController(AuthenticationManager authenticationManager, BankAccountServiceImpl bankAccountServiceImpl) {
        this.authenticationManager = authenticationManager;
        this.bankAccountServiceImpl = bankAccountServiceImpl;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.get("email"),
                            loginRequest.get("password")
                    )
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();
            System.out.println(user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
            String jwt = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("roles", user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 864_000_00)) // 1 jour
                    .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("access-token", jwt);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: " + e.getMessage()); // Log l'erreur
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage()); // Log d'autres erreurs
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            bankAccountServiceImpl.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

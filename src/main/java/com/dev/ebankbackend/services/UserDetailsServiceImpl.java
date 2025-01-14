package com.dev.ebankbackend.services;

import com.dev.ebankbackend.entities.Customer;
import com.dev.ebankbackend.repositories.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public UserDetailsServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword()) // Assurez-vous que le mot de passe est encodé
                .roles(customer.getRole().split(",")) // Assurez-vous que cela renvoie un tableau de rôles
                .build();
    }

}

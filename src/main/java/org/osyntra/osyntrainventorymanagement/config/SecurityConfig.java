package org.osyntra.osyntrainventorymanagement.config;

import org.osyntra.osyntrainventorymanagement.models.UserStatus;
import org.osyntra.osyntrainventorymanagement.repositories.EmployeeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(EmployeeRepository employeeRepository) {
        return email -> {
            var employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            return User.withUsername(employee.getEmail()).password(employee.getPasswordHash())
                    .roles(employee.getRole().name()).disabled(employee.getStatus() != UserStatus.ACTIVE).build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/employees/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**", "/api/audit/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "MANAGER", "SUPERVISOR", "STAFF")
                        .requestMatchers("/api/products/**", "/api/inventory/**", "/api/categories/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().denyAll())
                .build();
    }
}

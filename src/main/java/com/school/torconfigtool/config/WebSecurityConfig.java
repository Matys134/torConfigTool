package com.school.torconfigtool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests((requests) -> requests
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
        return username -> {
            Path path = Paths.get("shellScripts/user");
            String line;
            try {
                line = Files.readAllLines(path).getFirst();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] parts = line.split(":");
            String hashedUsernameFromFile = parts[0];
            String hashedPasswordFromFile = parts[1];

            if (!encoder.matches(username, hashedUsernameFromFile)) {
                throw new UsernameNotFoundException("Invalid username");
            }

            UserDetails user =
                    User.builder()
                            .username(username)
                            .password(hashedPasswordFromFile)
                            .roles("USER")
                            .build();

            return new InMemoryUserDetailsManager(user).loadUserByUsername(username);
        };
    }
}
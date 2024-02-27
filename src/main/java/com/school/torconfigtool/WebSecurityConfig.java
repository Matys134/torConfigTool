package com.school.torconfigtool;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll
                )
                .logout(LogoutConfigurer::permitAll)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
        return username -> {
            Path path = Paths.get("user/user");
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
}*/
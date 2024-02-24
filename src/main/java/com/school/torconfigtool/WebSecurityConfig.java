package com.school.torconfigtool;


/*
@Configuration
@EnableWebSecurity*/
public class WebSecurityConfig {

    /*
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
        String username = System.getenv("APP_USERNAME");
        String password = System.getenv("APP_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalArgumentException("APP_USERNAME and APP_PASSWORD environment variables are required");
        }

        UserDetails user =
                User.builder()
                        .username(username)
                        .password(encoder.encode(password))
                        .roles("USER")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }

     */
}
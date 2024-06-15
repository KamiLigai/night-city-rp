package ru.nightcityroleplay.backend.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(HttpSecurity http, Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer) {
        return http
            .cors(corsCustomizer)
            .csrf(AbstractHttpConfigurer::disable)      // todo: enable csrf
            .authorizeHttpRequests(it -> it
                .requestMatchers(POST, "users").permitAll()
                .requestMatchers(GET,"/actuator/health").permitAll()
                .anyRequest().authenticated()
            ).httpBasic(withDefaults())
            .sessionManagement(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    public Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer(ApplicationProperties props) {
        if (props.enableCors()) {
            return configurer -> configurer.configurationSource(new UrlBasedCorsConfigurationSource());
        } else {
            return configurer -> {
                var config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.addAllowedOriginPattern("*");
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                var source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                configurer.configurationSource(source);
            };
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

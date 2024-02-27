package com.hcmus.mentor.backend.security;

import com.hcmus.mentor.backend.security.filter.JwtAuthFilter;
import com.hcmus.mentor.backend.security.handler.FirebaseClearingLogoutHandler;
import com.hcmus.mentor.backend.security.handler.OAuth2AuthenticationFailureHandler;
import com.hcmus.mentor.backend.security.handler.OAuth2AuthenticationSuccessHandler;
import com.hcmus.mentor.backend.security.principal.oauth2.CustomOidcUserService;
import com.hcmus.mentor.backend.security.principal.oauth2.OAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
    private final OAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final FirebaseClearingLogoutHandler firebaseClearingLogoutHandler;
    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] AUTH_WHITELIST = {
            "/api/auth/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            // other public endpoints of your API may be appended to this array
            "**/oauth2/**",
            "/actuator/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager register(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(c -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers(AUTH_WHITELIST).permitAll();
                    requests.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(e -> {
                            e.baseUri("/oauth2/authorize");
                            e.authorizationRequestRepository(cookieAuthorizationRequestRepository);
                        })
                        .redirectionEndpoint(e -> e.baseUri("/oauth2/callback/*"))
                        .userInfoEndpoint(e -> e.oidcUserService(customOidcUserService))
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler))
                .logout(l -> {
                    l.logoutUrl("/api/auth/logout");
                    l.addLogoutHandler(firebaseClearingLogoutHandler);
                })
                .addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class)
                .build();
    }
}

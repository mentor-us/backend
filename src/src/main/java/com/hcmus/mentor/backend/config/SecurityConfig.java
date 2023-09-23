package com.hcmus.mentor.backend.config;

import com.hcmus.mentor.backend.middleware.CheckingActivateUser;
import com.hcmus.mentor.backend.security.CustomUserDetailsService;
import com.hcmus.mentor.backend.security.RestAuthenticationEntryPoint;
import com.hcmus.mentor.backend.security.TokenAuthenticationFilter;
import com.hcmus.mentor.backend.security.oauth2.CustomOidcUserService;
import com.hcmus.mentor.backend.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.hcmus.mentor.backend.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.hcmus.mentor.backend.security.oauth2.OAuth2AuthorizationRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomOidcUserService customOidcUserService;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    private final CheckingActivateUser checkingActivateUser;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private static final String[] AUTH_WHITELIST = {"/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js", "**/api/**", "/logout/**", "/auth/**",
            // -- Swagger UI v2
            "/v2/api-docs", "/swagger-resources", "/swagger-resources/**", "/configuration/ui", "/configuration/security", "/swagger-ui.html", "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**", "/swagger-ui/**"
            // other public endpoints of your API may be appended to this array
    };

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomOidcUserService customOidcUserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler, CheckingActivateUser checkingActivateUser, ClientRegistrationRepository clientRegistrationRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.checkingActivateUser = checkingActivateUser;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    public OAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new OAuth2AuthorizationRequestRepository();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "content-type", "Access-Control-Allow-Headers"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri("http://localhost:8080/test");
        return successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(s -> {
            s.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }).cors(c -> {

        }).csrf(c -> {
            c.disable();
        }).formLogin(f -> {
            f.disable();
        }).httpBasic(h -> {
            h.disable();
        }).exceptionHandling(eh -> {
            eh.authenticationEntryPoint(restAuthenticationEntryPoint());
        }).authorizeHttpRequests(ar -> {
            ar.requestMatchers(AUTH_WHITELIST).permitAll();
            ar.requestMatchers("**/oauth2/**", "/actuator").permitAll();
        }).logout(l -> {
            l.logoutUrl("/logout");
            l.logoutSuccessUrl("/test2");
            l.logoutSuccessHandler(oidcLogoutSuccessHandler());
            l.invalidateHttpSession(true);
            l.clearAuthentication(true);
            l.deleteCookies("JSESSIONID");
            l.permitAll();
        }).oauth2Login(o2 -> {
            o2.authorizationEndpoint(e -> {
                e.baseUri("/oauth2/authorize");
                e.authorizationRequestRepository(cookieAuthorizationRequestRepository());
            }).redirectionEndpoint(e -> {
                e.baseUri("/oauth2/callback/*");
            }).userInfoEndpoint(e -> {
                e.oidcUserService(customOidcUserService);
            }).successHandler(oAuth2AuthenticationSuccessHandler).failureHandler(oAuth2AuthenticationFailureHandler);
        });


//        http.cors().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf().disable().formLogin().disable().httpBasic().disable().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint()).and().authorizeRequests().dispatcherTypeMatchers(AUTH_WHITELIST).permitAll().dispatcherTypeMatchers("**/oauth2/**", "/actuator").permitAll().and().logout().logoutUrl("/logout").logoutSuccessUrl("/test2").logoutSuccessHandler(oidcLogoutSuccessHandler()).invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID").permitAll().and().oauth2Login().authorizationEndpoint().baseUri("/oauth2/authorize").authorizationRequestRepository(cookieAuthorizationRequestRepository()).and().redirectionEndpoint().baseUri("/oauth2/callback/*").and().userInfoEndpoint().oidcUserService(customOidcUserService).and().successHandler(oAuth2AuthenticationSuccessHandler).failureHandler(oAuth2AuthenticationFailureHandler);

        // Add our custom Token based authentication filter
        //http.addFilterBefore(checkingActivateUser, UsernamePasswordAuthenticationFilter.class);
        //http.addFilterBefore(tokenAuthenticationFilter(), CheckingActivateUser.class);
        return http.build();
    }
}

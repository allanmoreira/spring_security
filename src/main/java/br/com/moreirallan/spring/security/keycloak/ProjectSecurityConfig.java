package br.com.moreirallan.spring.security.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;


@Configuration
public class ProjectSecurityConfig {

    /*@Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
    String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
    String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
    String clientSecret;*/

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();

//        JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = JwtIssuerAuthenticationManagerResolver
//                .fromTrustedIssuers(
//                        "https://getsso-hom.getnet.com.br/auth/realms/external",
//                        "https://getsso-hom.getnet.com.br/auth/realms/getnet"
//                );

        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setExposedHeaders(List.of("Authorization"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/contact", "/register")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // Only HTTP
                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/api/**").hasAnyRole(
//                                EXTERNAL_USERS_ACCESS_ROLE,
//                                CRM_USERS_ACCESS_ROLE)
                        .requestMatchers("/api/**").hasRole("user")
                        .requestMatchers("/myLoad").hasAnyRole("user", "admin")
                        .requestMatchers("/user").authenticated()
                        .requestMatchers(
                                EndpointRequest.toAnyEndpoint(),
                                new AntPathRequestMatcher("/public/**"),
                                new AntPathRequestMatcher("/interno/**"),
                                new AntPathRequestMatcher("/error/**"),
                                new AntPathRequestMatcher("/actuator/**"),
                                new AntPathRequestMatcher("/v2/api-docs/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**")
                        ).permitAll()
                        //.requestMatchers(OPTIONS, "/**").denyAll()
                );
        http
                .oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer ->
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));

//        http.oauth2ResourceServer(oauth2 -> oauth2
//                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
//                .authenticationManagerResolver(authenticationManagerResolver)
//        );
        /*http.oauth2ResourceServer(rsc -> rsc.opaqueToken(otc -> otc.authenticationConverter(new KeycloakOpaqueRoleConverter())
                .introspectionUri(this.introspectionUri).introspectionClientCredentials(this.clientId,this.clientSecret)));*/
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

}

package br.com.moreirallan.spring.security.keycloak;

import br.com.moreirallan.spring.security.config.KeycloakProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class ProjectSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
    String[] urls;

    @Autowired
    KeycloakProperties keycloakProperties;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

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
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
//                        .ignoringRequestMatchers(
//                                "/contact",
//                                "/register")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // Only HTTP
                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/api/**").hasAnyRole(
//                                EXTERNAL_USERS_ACCESS_ROLE,
//                                CRM_USERS_ACCESS_ROLE)
//                        .requestMatchers(new AntPathRequestMatcher("/api/**")).hasRole("user")
                        .requestMatchers("/api/**").hasRole("user")
                        .requestMatchers("/myLoad").hasAnyRole("user", "admin")
                        .requestMatchers("/user").authenticated()
//                        .requestMatchers(
//                                EndpointRequest.toAnyEndpoint(),
//                                new AntPathRequestMatcher("/public/**"),
//                                new AntPathRequestMatcher("/interno/**"),
//                                new AntPathRequestMatcher("/error/**"),
//                                new AntPathRequestMatcher("/actuator/**"),
//                                new AntPathRequestMatcher("/v2/api-docs/**"),
//                                new AntPathRequestMatcher("/v3/api-docs/**")
//                        ).permitAll()
                        //.requestMatchers(OPTIONS, "/**").denyAll()
                );
//        http
//                .oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer ->
//                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        http.oauth2ResourceServer(oauth2 -> oauth2
//                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationManagerResolver(JwtIssuerAuthenticationManagerResolver
                        .fromTrustedIssuers(
                                keycloakProperties.getUrls()
                        ))
        );

        http.httpBasic(withDefaults());
        /*http.oauth2ResourceServer(rsc -> rsc.opaqueToken(otc -> otc.authenticationConverter(new KeycloakOpaqueRoleConverter())
                .introspectionUri(this.introspectionUri).introspectionClientCredentials(this.clientId,this.clientSecret)));*/
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

}

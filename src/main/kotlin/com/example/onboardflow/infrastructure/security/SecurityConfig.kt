package com.example.onboardflow.infrastructure.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val rateLimitingFilter: RateLimitingFilter
) {
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity.csrf { csrfConfigurer -> csrfConfigurer.disable() }.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }.authorizeHttpRequests { auth ->

            auth.
                /*ON DEFINIT ICI LA OU LES ROUTES QUI N'AURONT PAS BESOIN D'AUTHENTIFICATION (token)
                POUR ETRE UTILISE*/
            requestMatchers("/auth/**").permitAll().dispatcherTypeMatchers(
                DispatcherType.ERROR,
                DispatcherType.FORWARD,
            ).permitAll()
                //ENSUITE ON SECURISE TOUTES LES AUTRES ROUTES EN OBLIGEANT L'AUTHENTIFICATION
                .anyRequest().authenticated()
        }
            .exceptionHandling { configurer -> configurer.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            //To limit login attemps for a given time
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    };
}

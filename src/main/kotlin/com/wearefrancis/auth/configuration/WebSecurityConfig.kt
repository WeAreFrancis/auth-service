package com.wearefrancis.auth.configuration

import com.wearefrancis.auth.security.JwtAuthenticationEntryPoint
import com.wearefrancis.auth.security.JwtAuthenticationFilter
import com.wearefrancis.auth.security.JwtUserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
open class WebSecurityConfig(
        val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
        val jwtAuthenticationFilter: JwtAuthenticationFilter,
        val jwtUserService: JwtUserService
) : WebSecurityConfigurerAdapter() {
    override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder) {
        authenticationManagerBuilder
                .userDetailsService(jwtUserService)
                .passwordEncoder(passwordEncoder())
    }

    override fun configure(http: HttpSecurity?) {
        http!!
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                        .antMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                .headers().cacheControl()
    }

    @Bean
    open fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()
}
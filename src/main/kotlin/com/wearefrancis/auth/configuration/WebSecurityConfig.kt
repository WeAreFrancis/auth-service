package com.wearefrancis.auth.configuration

import com.wearefrancis.auth.repository.UserRepository
import com.wearefrancis.auth.security.JwtAuthenticationEntryPoint
import com.wearefrancis.auth.security.JwtAuthenticationFilter
import com.wearefrancis.auth.security.JwtUserService
import com.wearefrancis.auth.security.UserPermissionEvaluator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.PermissionEvaluator
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
        private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
        private val jwtAuthenticationFilter: JwtAuthenticationFilter,
        private val jwtUserService: JwtUserService,
        private val userRepository: UserRepository
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
                        .antMatchers(HttpMethod.GET, "/login").permitAll()
                        .antMatchers(HttpMethod.POST, "/users").permitAll()
                        .antMatchers(HttpMethod.GET, "/users/activate/**").permitAll()
                        .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                .headers().cacheControl()
    }

    @Bean
    open fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    open fun permissionEvaluator(): PermissionEvaluator = UserPermissionEvaluator(userRepository)
}
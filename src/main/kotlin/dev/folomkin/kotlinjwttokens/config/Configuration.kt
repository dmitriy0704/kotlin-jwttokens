package dev.folomkin.kotlinjwttokens.config

import dev.folomkin.kotlinjwttokens.repository.UserRepository
import dev.folomkin.kotlinjwttokens.service.CustomUserDetailsService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class Configuration {


    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        CustomUserDetailsService(userRepository)


    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Deprecated("Не используется в Spring Security 6")
    @Bean
    fun authenticationProvider(repository: UserRepository): AuthenticationProvider =
        DaoAuthenticationProvider()
            .also {
                it.setUserDetailsService(userDetailsService(repository))
                it.setPasswordEncoder(encoder())
            }

    @Deprecated("Не используется в Spring Security 6")
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager


// -> Вместо @Deprecated:
//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .csrf { it.disable() }
//            .authorizeHttpRequests { auth ->
//                auth.requestMatchers("/auth/**").permitAll()
//                auth.anyRequest().authenticated()
//            }
//            .httpBasic { } // или formLogin / JWT фильтр
//
//        return http.build()
//    }

}
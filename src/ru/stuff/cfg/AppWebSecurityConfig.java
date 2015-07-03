package ru.stuff.cfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by mark on 20.01.15.
 */
@Configuration
@EnableWebSecurity
@ComponentScan("ru.stuff")
public class AppWebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,UserDetailsService authService) throws Exception {
        auth.userDetailsService(authService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/auth**").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/**").access("hasRole('ROLE_USER')")

                .and()
                .formLogin()
                .loginProcessingUrl("/j_spring_security_check")
                .loginPage("/auth")
                .failureUrl("/auth?error")
                .defaultSuccessUrl("/chess", true)
                .and().rememberMe();
    }

}


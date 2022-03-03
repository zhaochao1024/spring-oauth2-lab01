package com.oauth2lab.labe01authcodeserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import sun.security.util.Debug;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private DataSource dataSource;

    /** * 开放/login和/oauth/authorize两个路径的匿名访问。
     * 前者用于登录，后者用于换授权码，这两个端点访问的时机都在登录之前。
     * * 设置/login使用表单验证进行登录。 * @param http * @throws Exception */
    /** * 开放/login和/oauth/authorize两个路径的匿名访问。前者用于登录，后者用于换授权码，这两个端点访问的时机都在登录之前。 * 设置/login使用表单验证进行登录。 * @param http * @throws Exception */
    @Override protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and().requestMatchers()
                .antMatchers("/oauth/**","/login")
                .and()
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/oauth/**").authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .logout().permitAll()
                .and()
                .csrf().disable()
                .httpBasic().disable();
//        http.authorizeRequests()
//                .antMatchers("/oauth/authorize/**","/login","/oauth/confirm_access/**").permitAll()
//                .and().formLogin()
//                .and().csrf().disable();
    }

    /** * 配置用户账户的认证方式。显然，我们把用户存在了数据库中希望配置JDBC的方式。
     * * @param auth * @throws Exception */
    @Override protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication() .dataSource(dataSource) .
                passwordEncoder(NoOpPasswordEncoder.getInstance()); }

}

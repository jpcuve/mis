package com.darts.mis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    public static class User implements UserDetails {
        private final String username;
        private final String password;
        private final String[] roles;
        private final boolean accountNonExpired;
        private final boolean accountNonLocked;
        private final boolean credentialsNonExpired;
        private final boolean enabled;
        private List<SimpleGrantedAuthority> authorities;

        @JsonCreator
        public User(
                @JsonProperty("username") String username,
                @JsonProperty("password") String password,
                @JsonProperty("roles") String[] roles,
                @JsonProperty("accountNonExpired") boolean accountNonExpired,
                @JsonProperty("accountNonLocked") boolean accountNonLocked,
                @JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
                @JsonProperty("enabled") boolean enabled
        ) {
            this.username = username;
            this.password = password;
            this.roles = roles;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.enabled = enabled;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (authorities == null){
                authorities = roles == null ? Collections.emptyList() : Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            }
            return authorities;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final Map<String, User> users = new HashMap<>();
    @Value("${app.realm}")
    private String realm;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
				.and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .headers().frameOptions().disable()
                .and()
                .csrf().disable()
        ;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder){
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        try{
            User[] miniUsers = mapper.readValue(getClass().getClassLoader().getSystemResourceAsStream("users.yaml"), User[].class);
            users.putAll(Arrays.stream(miniUsers).collect(Collectors.toMap(User::getUsername, Function.identity())));
        } catch(IOException e){
            LOGGER.error("Cannot read user resource", e);
        }
        return users::get;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10, new SecureRandom());
    }

    @Bean
    public BasicAuthenticationFilter basicAuthenticationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint){
        final BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint);
        return basicAuthenticationFilter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        final BasicAuthenticationEntryPoint authenticationEntryPoint = new BasicAuthenticationEntryPoint();
        authenticationEntryPoint.setRealmName(realm);
        return authenticationEntryPoint;
    }
}

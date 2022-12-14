package com.teosprint.flashcard.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.filter.CorsFilter;

// jwt 설정 securityconfig
@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록. 필터체인(묶음)에 필터등록
//@EnableGlobalMethodSecurity(
//        securedEnabled = true,  // Controller 에서 @Secured 어노테이션 사용가능. @Secured("roleName")
//        prePostEnabled = true   // preAuthorize 어노테이션 활성화
//)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // context-path 가져와서 설정하면 베스트
    private final String defaultUrl = "/teosp";
    private final CorsFilter corsFilter;
    private final HttpHeaders httpHeaders = new HttpHeaders();

//    private final JwsTokenProvider tokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    @Bean // 더블 슬래쉬 허용
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().disable();
        http.httpBasic().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않겠다.

                .and()
                    // exception advice class 설정
                    .exceptionHandling()
//                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .addFilter(corsFilter)
                // 위의 addFilter를 하면 모든 요청은 corsFilter를 거치게 돼있음.
                // @CrossOrigin는 인증이 필요한 상황에선 해결되지 않는다.
                // 인증이 있을때는 시큐리티 필터에 등록을 해줘야 한다.

//                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin().disable()
                .headers()
                .frameOptions().sameOrigin()
                .and()
                // 권한 인증 설정
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, defaultUrl).permitAll()
                            // hasRole 사용 시 체크 권한이 ROLE_ABC라면 ABC만 적는다.
                    .antMatchers(("/admin/**")).hasRole("ROOT")
                    .antMatchers("/**").permitAll() // 권한 처리를 각 서비스에서 한다.
                    .anyRequest().authenticated()
                .and()

                // security 기본 로그인 페이지 사용안함
                .formLogin().disable()

                // oauth 로그인 인증 권한 처리
//                .addFilterBefore(new JwtExceptionFilter(),
//                        OAuth2LoginAuthenticationFilter.class)
//                .oauth2Login().loginPage("/token/expired")
//                .successHandler(successHandler)
//                .userInfoEndpoint().userService(oAuth2UserService)
        ;

    }

    @Override // ignore check swagger resource
    public void configure(WebSecurity web) {
        web.httpFirewall(defaultHttpFirewall());
        // 인증하지 않을 주소 추가.

        web.ignoring().antMatchers("/html/**", "/css/**", "/js/**", "/image/**");

        web.ignoring().antMatchers(
                // -- Swagger UI v2
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
                // -- Swagger UI v3 (OpenAPI)
                "/v3/api-docs/**",
                "/swagger-ui/**"
                // other public endpoints of your API may be appended to this array
        );
    }

}
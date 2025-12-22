package com.mydays.backend.config;

import com.mydays.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;
    private final String jwtSecret;

    // ✅ CurrentMemberArgumentResolver를 "Bean 주입" 받는다 (new로 만들지 않음)
    private final CurrentMemberArgumentResolver currentMemberArgumentResolver;

    public WebConfig(
            MemberRepository memberRepository,
            @Value("${jwt.secret}") String jwtSecret,
            CurrentMemberArgumentResolver currentMemberArgumentResolver
    ) {
        this.memberRepository = memberRepository;
        this.jwtSecret = jwtSecret;
        this.currentMemberArgumentResolver = currentMemberArgumentResolver;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean =
                new FilterRegistrationBean<>(new JwtAuthFilter(memberRepository, jwtSecret));
        bean.setOrder(1);
        return bean;
    }

    // ✅ 핵심: @CurrentMember 동작하게 ArgumentResolver 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

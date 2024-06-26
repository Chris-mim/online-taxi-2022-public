package com.mashibing.apidriver.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 拦截器的bean是在Spring bean初始化之前创建的，之前不写这个报了stringRedisTemplate的空指针。。。
     * @return
     */
    @Bean
    public JwtInterceptor jwtInterceptor(){
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                // 拦截的路径
                .addPathPatterns("/**")
                // 不拦截的路径
                .excludePathPatterns("/noauthTest")
                .excludePathPatterns("/error") // 调用feign报错时，会跳转/error地址
                .excludePathPatterns("/verification-code")
                .excludePathPatterns("/order/grab")
                .excludePathPatterns("/verification-code-check");
    }
}

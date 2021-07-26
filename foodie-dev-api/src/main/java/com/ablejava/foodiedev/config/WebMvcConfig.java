package com.ablejava.foodiedev.config;

import com.ablejava.foodiedev.api.controller.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author mrcode
 * @date 2021/2/16 23:33
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 实现静态资源的映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                // 这里拦截之后，之前能访问的 swagger2 文档就无法访问到了
                // 因为它也是第三方 jar 包提供的 html 静态文件，所以这里也需要映射下
                .addResourceLocations("classpath:/META-INF/resources/")  // 映射 swagger2
                .addResourceLocations("file:/Users/mrcode/Documents/GitHub/foodie-dev/tempresource/images/");  // 映射本地静态资源
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTokenInterceptor())
        .addPathPatterns("/hello");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}

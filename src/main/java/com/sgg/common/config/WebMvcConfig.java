package com.sgg.common.config;

import com.sgg.common.security.CurrentUserResolver;
import com.sgg.common.security.DevUserInterceptor;
import com.sgg.common.security.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DevUserInterceptor devUserInterceptor;
    private final TenantInterceptor tenantInterceptor;
    private final CurrentUserResolver currentUserResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // DevUserInterceptor PRIMERO: setea userId desde header X-Dev-User-Id
        // TenantInterceptor SEGUNDO: usa el userId para validar membresía
        registry.addInterceptor(devUserInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(tenantInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}

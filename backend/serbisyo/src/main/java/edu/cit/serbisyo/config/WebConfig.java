package edu.cit.serbisyo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map the "/uploads/**" URL path to the "uploads/" directory on the server
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
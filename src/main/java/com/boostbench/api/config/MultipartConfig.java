package com.boostbench.api.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Maximum file size for individual files
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 10MB per file

        // Maximum total request size
        factory.setMaxRequestSize(DataSize.ofMegabytes(50)); // 50MB total

        // File size threshold after which files will be written to disk
        factory.setFileSizeThreshold(DataSize.ofKilobytes(512)); // 512KB

        return factory.createMultipartConfig();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true); // Resolve multipart requests lazily
        return resolver;
    }
}
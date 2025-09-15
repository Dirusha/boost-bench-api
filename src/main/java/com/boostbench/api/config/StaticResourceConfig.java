package com.boostbench.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            File uploadDirectory = uploadPath.toFile();
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                System.out.println("Upload directory created: " + created + " at " + uploadPath);
            }

            // Configure resource handler with multiple possible locations
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(
                            "file:" + uploadPath.toString() + "/",
                            "file:" + uploadPath.toString() + "\\",  // Windows compatibility
                            "classpath:/static/uploads/",
                            "file:uploads/",
                            "file:./uploads/"
                    )
                    .setCachePeriod(3600)
                    .resourceChain(true);

            System.out.println("=== Static Resource Handler Configuration ===");
            System.out.println("Upload directory exists: " + uploadDirectory.exists());
            System.out.println("Upload directory path: " + uploadPath);
            System.out.println("Upload directory absolute: " + uploadDirectory.getAbsolutePath());
            System.out.println("URL pattern: /uploads/**");
            System.out.println("Resource locations configured");
            System.out.println("============================================");

        } catch (Exception e) {
            System.err.println("Error configuring static resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
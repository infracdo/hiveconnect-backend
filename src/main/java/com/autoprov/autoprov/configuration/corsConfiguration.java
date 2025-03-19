// package com.autoprov.autoprov.configuration;

// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// public class corsConfiguration implements WebMvcConfigurer{
//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**") // Allow all endpoints
//                 .allowedOrigins("*") // Allow all origins or specify your frontend URL
//                 .allowedMethods("GET", "POST", "OPTIONS") // Allowed methods
//                 .allowedHeaders("*") // Allowed headers
//                 .allowCredentials(true) // Allow credentials if necessary
//                 .maxAge(3600); // Cache preflight response for 1 hour
//     }
// }

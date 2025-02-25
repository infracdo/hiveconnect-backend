package com.autoprov.autoprov.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class envConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(envConfiguration.class);
    @Bean
    public Dotenv dotenv() {
        Dotenv dotenv = Dotenv.load();
        
        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }

        return dotenv;
    }
    
}

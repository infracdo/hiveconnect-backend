package com.autoprov.autoprov.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class envConfiguration {
    @Bean
    public Dotenv dotenv() {
        Dotenv dotenv = Dotenv.load();
        
        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
        return dotenv;
    }
    
}

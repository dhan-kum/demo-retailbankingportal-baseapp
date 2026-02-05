package com.eviden.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.web.client.RestTemplate;
import com.eviden.app.repository.BankAccountRepository;
import com.eviden.app.entity.BankAccount;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@Configuration
public class Components {
    protected static final Logger logger = LoggerFactory.getLogger(Components.class);
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

   

}

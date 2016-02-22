package com.wizecore;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Configures jTwig in Spring Boot.
 * 
 * @author Ruslan
 */
@Configuration
@EnableWebMvc
public class WebTwigConfig {
	/*
    @Bean
    public ViewResolver viewResolver () {
        JtwigViewResolver viewResolver = new JtwigViewResolver();
        // NOT WORKING?! WTF? 
        // viewResolver.setPrefix("classpath:/templates/");
        viewResolver.setPrefix("file://c:/ruslan/devel/calcbpm/src/main/resources/templates/");
        viewResolver.setSuffix(".html");
        viewResolver.setEncoding("UTF-8");
        viewResolver.setCache(false);
        return viewResolver;
    }
    */
}
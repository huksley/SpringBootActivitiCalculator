package com.wizecore.test;

import static org.mockito.Mockito.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.wizecore.CalcService;

/**
 * Spy on global CalcService so we cna ensure starting() and calculate() were called.
 * 
 * @author Ruslan
 */
@Component
public class CalcServicePostProcessor implements BeanPostProcessor, Ordered {
    @Override
    public int getOrder() {
    	return Ordered.LOWEST_PRECEDENCE;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    	if (bean instanceof CalcService) {
    		bean = spy(bean);
    		
    	}
    	return bean;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    	return bean;
    }
}
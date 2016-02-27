package com.wizecore;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Overrides default Spring Boot security.
 * 
 * @author Ruslan
 */
@Configuration
@EnableWebSecurity
// @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	public final static String TEST_USERNAME = "user";
	public final static String TEST_PASSWORD = "123";
	public final static String TEST_ROLE = "user";
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/greeting").permitAll();
        http.authorizeRequests().antMatchers("/*.js").permitAll();
        http.authorizeRequests().antMatchers("/*.css").permitAll();
        http.authorizeRequests().antMatchers("/webjars/**").permitAll();
        
        http
        .authorizeRequests()
            .antMatchers("/", "/home").permitAll()
            .anyRequest().authenticated()
            .and()
        .formLogin()
            .loginPage("/login")
            .permitAll()
            .and()
        .logout()
            .permitAll();
    }
    
    @Bean
    InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {
        return new InitializingBean() {
            public void afterPropertiesSet() throws Exception {
            	// Add group
            	Group group = identityService.createGroupQuery().groupName(TEST_ROLE).singleResult();
                if (group == null) {
	            	group = identityService.newGroup(TEST_ROLE);
	                group.setName(TEST_ROLE);
	                group.setType("security-role");
	                identityService.saveGroup(group);
                }

                // Add user
                User user = identityService.createUserQuery().userId(TEST_USERNAME).singleResult();
                if (user == null) {
	                user = identityService.newUser(TEST_USERNAME);
	                user.setPassword(TEST_PASSWORD);
	                identityService.saveUser(user);
                }

                // Add membership
                if (identityService.createUserQuery().memberOfGroup(TEST_ROLE).userId(TEST_USERNAME).singleResult() == null) {
                	identityService.createMembership(user.getId(), group.getId());
                }
            }
        };
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(TEST_USERNAME).password(TEST_PASSWORD).roles(new String[] { TEST_ROLE });
    }
}
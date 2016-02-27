package com.wizecore.test;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.TestRestTemplate.HttpClientOption;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Issue;
import ru.yandex.qatools.allure.annotations.Stories;
import ru.yandex.qatools.allure.annotations.Title;

import com.wizecore.Application;
import com.wizecore.SecurityConfig;

/**
 * Test web application interface.
 * 
 * @author Ruslan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Features("web")
public class WebApplicationTest {
	  
	@Value("${local.server.port}")
    private int port;
	
	private URL base;
	private RestTemplate rest = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES, HttpClientOption.ENABLE_REDIRECTS);
	private StatefulRestTemplate http = new StatefulRestTemplate(rest);

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/");
	}

	@Test
	@Title("Test REST simple text/plain greeting")
	@Stories("REST testing")
	@Issue("1")
	public void testGreeting() {
		String body = http.get(base + "greeting", String.class);
		Assert.assertThat(body, Matchers.containsString("Greetings"));
	}
	
	@Test
	@Stories("Auth testing")
	@Title("Test AUTH and navigation to main page")
	public void testLogin() {
		String body = http.get(base + "login" , String.class);
		Pattern p = Pattern.compile(".*name=\\\"_csrf\\\" value=\\\"([^\\\"]*)\\\".*", Pattern.MULTILINE);
		body = body.replace("\r", "\n").replace("\n", "");
		Assert.assertNotNull("Body must be not null", body);
		System.out.println("Got body " + body);
		Matcher matcher = p.matcher(body);
		Assert.assertTrue("CSRF token found", matcher.matches());
		String csrf = matcher.group(1);
		Assert.assertTrue("CSRF token set", csrf != null && !csrf.trim().equals(""));
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("username", SecurityConfig.TEST_USERNAME);
		map.add("password", SecurityConfig.TEST_PASSWORD);
		map.add("_csrf", csrf);
		body = http.post(base + "login", map, String.class);
		System.out.println("Got body " + body);
		Assert.assertNotNull("Body must be not null", body);
		Assert.assertThat(body, Matchers.containsString("Welcome"));
		
//		FIXME: somehow changes cookie and goes to login again
//		body = http.get(base + "hello", String.class);
//		Assert.assertNotNull("Body must be not null", body);
//		Assert.assertThat(body, Matchers.containsString("Hello " + WebSecurityConfig.TEST_USERNAME));
	}
}

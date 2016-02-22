package com.wizecore;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class ApplicationTests {

	  private static final Logger LOG = Logger.getLogger(StatefulRestTemplate.class.getName());
	  
	 class StatefulRestTemplate {
		 
		 
		  private final Map<String, String> cookies;
		  private String referrer = null;
		 
		  public StatefulRestTemplate() {
		    cookies = new HashMap<>();
		  }
		 
		  /**
		   * Get the all cookies.
		   *
		   * @return
		   */
		  public Map<String, String> getCookies() {
		    return cookies;
		  }
		 
		  /**
		   * Remove all stored cookies
		   */
		  public void clearAllCookies() {
		    cookies.clear();
		  }
		 
		  /**
		   * Set one cookie. Will overight if there is already one with the same key
		   *
		   * @param key
		   * @param value
		   */
		  public void setCookie(String key, String value) {
		    cookies.put(key, value);
		  }
		 
		  /**
		   * Remove one cookie. Do nothing if the cookie does not exist.
		   *
		   * @param key
		   */
		  public void removeCookie(String key) {
		    cookies.remove(key);
		  }
		 
		  /**
		   * execute a POST call.
		   *
		   * @param <T> return type of this call
		   * @param url url to call
		   * @param data request data
		   * @param clazz return type class
		   * @return the returned object of this request
		   */
		  public <T> T post(String url, Object data, Class<T> clazz) {
		    return getObject(HttpMethod.POST, url, data, clazz);
		  }
		 
		  /**
		   * execute a PUT call.
		   *
		   * @param <T> return type of this call
		   * @param url url to call
		   * @param data request data
		   * @param clazz return type class
		   * @return the returned object of this request
		   */
		  public <T> T put(String url, Object data, Class<T> clazz) {
		    return getObject(HttpMethod.PUT, url, data, clazz);
		  }
		 
		  /**
		   * execute a DELETE call.
		   *
		   * @param <T> return type of this call
		   * @param url url to call
		   * @param data request data
		   * @param clazz return type class
		   * @return the returned object of this request
		   */
		  public <T> T delete(String url, Object data, Class<T> clazz) {
		    return getObject(HttpMethod.DELETE, url, data, clazz);
		  }
		 
		  /**
		   * execute a GET call.
		   *
		   * @param <T> return type of this call
		   * @param url url to call
		   * @param clazz return type class
		   * @return the returned object of this request
		   */
		  public <T> T get(String url, Class<T> clazz) {
		    return getObject(HttpMethod.GET, url, clazz);
		  }
		 
		  /**
		   * Execute a stateful call.
		   *
		   * @param <T> return object type
		   * @param method http method
		   * @param url call url
		   * @param clazz return object type
		   * @return result object
		   */
		  public <T> T getObject(HttpMethod method, String url, Class<T> clazz) {
			  return getObject(method, url, null, clazz);
//		    LOG.info("Execute " + method + " call to " + url);
//		    ResponseEntity<T> response = rest.exchange(getRequestEntity(method, url), clazz);
//		    referrer = url;
//		    LOG.info("Got response code: " + response.getStatusCode() + ", " + response.getHeaders().getLocation());
//		    while (response.getStatusCode().is3xxRedirection()) {
//			    LOG.info("Following redirect to " + response.getHeaders().getLocation());
//			    referrer = response.getHeaders().getLocation().toString();
//		    	response = rest.exchange(getRequestEntity(HttpMethod.GET, response.getHeaders().getLocation().toString()), clazz);
//		    }
//		    Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
//		    extractCookies(response.getHeaders());
//		    return response.getBody();
		  }
		 
		  /**
		   * Execute a stateful call with request data.
		   *
		   * @param <T> return object type
		   * @param method http method
		   * @param url call url
		   * @param data input data
		   * @param clazz return object type
		   * @return result object
		   */
		  public <T> T getObject(HttpMethod method, String url, Object data, Class<T> clazz) {
		    LOG.info("Execute " + method + " call to " + url + " with data=" + data);
		    HttpEntity request = data == null ? getRequestEntity(method, url) : new HttpEntity(data, getHeaders());
		    referrer = url;
		    ResponseEntity<T> response = rest.exchange(url, method, request, clazz);
		    LOG.info("Got response code: " + response.getStatusCode() + ", " + response.getHeaders().getLocation());
		    while (response.getStatusCode().is3xxRedirection()) {
			    LOG.info("Following redirect to " + response.getHeaders().getLocation());
			    referrer = response.getHeaders().getLocation().toString();
		    	response = rest.exchange(getRequestEntity(HttpMethod.GET, response.getHeaders().getLocation().toString()), clazz);
		    }
		    Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		    extractCookies(response.getHeaders());
		    return response.getBody();
		  }
		 
		  /**
		   * Extract cookies from a response header. Will overight previous cookies if new one arrive with
		   * the same key.
		   *
		   * @param headers response header
		   */
		  protected void extractCookies(HttpHeaders headers) {
		    List<String> cookiesList = headers.get("Set-Cookie");
		    if (cookiesList != null && !cookiesList.isEmpty()) {
		      String cookiesStr = cookiesList.get(0);
		     
		        //LOG.info("Cookies read from response : " + cookiesStr);
		      
		      String[] cookiesSplit = cookiesStr.split(";");
		      for (String cookieStr : cookiesSplit) {
		        String[] kv = cookieStr.split("=");
		        if (kv.length > 1 && !kv[0].trim().equals("Path")) {
		        	LOG.info("Got cookie " + kv[0] + "=" + kv[1]);
		        	cookies.put(kv[0], kv[1]);
		        }
		      }
		    }
		  }
		 
		  /**
		   * Get the cookies string to write in the request header.
		   *
		   * @return cookies as string
		   */
		  protected String getCookiesString() {
		    StringBuilder sb = new StringBuilder();
		    if (!cookies.isEmpty()) {
		      cookies.entrySet().forEach(entry -> {
		        sb.append(entry.getKey());
		        sb.append("=");
		        sb.append(entry.getValue());
		        sb.append(";");
		      });
		      sb.deleteCharAt(sb.length() - 1);
		    }
		   
		      LOG.info("Cookies added to request : " + sb.toString());
		    
		    return sb.toString();
		  }
		 
		  /**
		   * Create a request entity for call
		   *
		   * @param method method of the call
		   * @param url url of the call
		   * @return new request entity
		   */
		  protected RequestEntity getRequestEntity(HttpMethod method, String url) {
		    RequestEntity request = new RequestEntity(getHeaders(), method, URI.create(url));
			return request;
		  }
		 
		  /**
		   * Create the header for the request.
		   *
		   * @return new header
		   */
		  protected MultiValueMap getHeaders() {
		    MultiValueMap headers = new HttpHeaders();
		    headers.put("Cookie", Arrays.asList(new String[]{getCookiesString()}));
		    if (referrer != null) {
		    	headers.put("Referer", Arrays.asList(new String[]{ referrer }));
		    }
		    return headers;
		  }
		}
	
	@Value("${local.server.port}")
    private int port;
	
	private URL base;
	private RestTemplate rest;
	private StatefulRestTemplate http = new StatefulRestTemplate();

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/");
		rest = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES, HttpClientOption.ENABLE_REDIRECTS);
	}

	@Test
	public void testGreeting() {
		String body = http.get(base + "greeting", String.class);
		Assert.assertThat(body, Matchers.containsString("Greetings"));
	}
	
	@Test
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
		map.add("username", WebSecurityConfig.TEST_USERNAME);
		map.add("password", WebSecurityConfig.TEST_PASSWORD);
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

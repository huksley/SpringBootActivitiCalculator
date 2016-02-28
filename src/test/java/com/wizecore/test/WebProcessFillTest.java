package com.wizecore.test;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.annotations.Title;

import com.wizecore.Application;
import com.wizecore.SecurityConfig;

/**
 * Test business processes through web interface.
 * 
 * @author Ruslan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Features({ "bpm", "web" })
@Title("Business Process test via web")
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@IntegrationTest({"server.port=0"})
public class WebProcessFillTest extends Assert {
	Logger log = Logger.getLogger(getClass().getName());
	
	@Value("${local.server.port}")
    private int port;

	private WebDriver driver;
	private WebDriverBackedSelenium selenium;
	private TakesScreenshot screen;
	
	@Before
	public void init() {
		driver = new PhantomJSDriver(new DesiredCapabilities());
		driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
		driver.manage().timeouts().pageLoadTimeout(5000, TimeUnit.MILLISECONDS);
		driver.manage().window().setSize(new Dimension(1024, 768));
		selenium = new WebDriverBackedSelenium(driver, "http://localhost:" + port);
		screen = (TakesScreenshot) driver;
	}
	
	@Test
	@Step
	@Title("Login to application")
	public void s1login() {
		selenium.open("http://localhost:" + port + "/login");
		makeScreenshot("Initial login form");
		selenium.type("//input[@name='username']", SecurityConfig.TEST_USERNAME);
		selenium.type("//input[@name='password']", SecurityConfig.TEST_PASSWORD);
		makeScreenshot("After fill inputs");
		selenium.click("//input[@type='submit']");
		byte[] im = makeScreenshot("After submit login form");
		assertNotNull("Image is not null", im);
		assertTrue("Image byte array is not empty", im.length > 0);
	}
	
	@Attachment(value = "{0}", type = "image/png")
	public byte[] makeScreenshot(String title) {
		return screen.getScreenshotAs(OutputType.BYTES);
	}
}

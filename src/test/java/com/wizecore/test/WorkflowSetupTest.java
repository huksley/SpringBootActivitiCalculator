package com.wizecore.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.activiti.engine.IdentityService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;
import ru.yandex.qatools.allure.annotations.Title;

import com.wizecore.Application;
import com.wizecore.SecurityConfig;

/**
 * Test business processes initial setup.
 * 
 * @author Ruslan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Features("bpm")
@Title("Business Process initial setup tests")
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WorkflowSetupTest extends Assert {
	Logger log = Logger.getLogger(getClass().getName());
	
	@Autowired
	IdentityService identityService;
	
	@Attachment(value = "BPM process", type = "image/png")
	public byte[] getProcessImage() throws IOException {
		URL resource = getClass().getResource("/processes/calc.png");
		if (resource == null) {
			return null;
		}
		try (InputStream is = resource.openStream()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[2048];
			int c = 0;
			while ((c = is.read(buf)) >= 0) {
				bos.write(buf, 0, c);
			}
			return bos.toByteArray();
		}
	}
	
	@Test
	@Stories("Get BPM process image")
	@Attachment(value = "BPM process")
	public void testProcessImage() throws IOException {
		byte[] im = getProcessImage();
		assertNotNull("BPM process image byte array is not null", im);
		assertTrue("Image byte array is not empty", im.length > 0);
	}
	
	/**
	 * Check identity groups and test user is set.
	 */
	@Title("Check identity groups and test user is set")
	@Test
	public void testIdentitiesSet() {
		assertTrue("Password must be valid", identityService.checkPassword(SecurityConfig.TEST_USERNAME, SecurityConfig.TEST_PASSWORD));
		assertEquals("Test having role", 1L, identityService.createGroupQuery().groupName(SecurityConfig.TEST_ROLE).count());
		assertEquals("Test user exists", 1L, identityService.createUserQuery().userId(SecurityConfig.TEST_USERNAME).count());
		assertEquals("User must be in role", 1L, identityService.createUserQuery().userId(SecurityConfig.TEST_USERNAME).memberOfGroup(SecurityConfig.TEST_ROLE).count());
	}
}

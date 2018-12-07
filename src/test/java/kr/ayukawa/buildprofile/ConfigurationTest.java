package kr.ayukawa.buildprofile;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationTest {
	private final String RESOURCE_FILENAME = "resources.properties";

	/**
	 * 설정 파일이 있는지 검사한다
	 * @throws URISyntaxException
	 */
	@Test
	public void isExistsConfigurationFile() throws URISyntaxException {
		URL configFileLocation = this.getClass().getResource(this.RESOURCE_FILENAME);
		Path path = Paths.get(configFileLocation.toURI());
		boolean exists = Files.exists(path);
		assertEquals(true, exists);
	}

	/**
	 * 설정 파일이 유효한지(=지정된 프로퍼티가 모두 존재하는지) 검사한다
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Test
	public void isValidConfigurationFile() throws URISyntaxException, IOException {
		URL configurationFileLocation = this.getClass().getResource(this.RESOURCE_FILENAME);
		try(InputStream is = configurationFileLocation.openStream()) {
			Properties prop = new Properties();
			prop.load(is);
			String msg = prop.getProperty("msg");
			assertNotNull(msg);
		}
	}
}

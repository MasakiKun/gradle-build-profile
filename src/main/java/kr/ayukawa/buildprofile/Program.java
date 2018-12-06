package kr.ayukawa.buildprofile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Program {
	public String getResource() throws IOException {
		InputStream is = this.getClass().getResourceAsStream("resources.properties");
		Properties prop = new Properties();
		prop.load(is);

		return prop.getProperty("msg");
	}
}

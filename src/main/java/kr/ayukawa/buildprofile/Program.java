package kr.ayukawa.buildprofile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * resources.properties 파일의 값을 읽어들여서 출력하는 간단한 프로그램
 */
public class Program {
	public String getResource() throws IOException {
		String msg = "";

		try(InputStream is = this.getClass().getResourceAsStream("resources.properties")) {
			Properties prop = new Properties();
			prop.load(is);
			msg = prop.getProperty("msg");
		}

		return msg;
	}
}

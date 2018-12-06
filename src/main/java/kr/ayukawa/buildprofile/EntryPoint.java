package kr.ayukawa.buildprofile;

import java.io.IOException;

public class EntryPoint {
	public static void main(String[] args) {
		String msg = "";
		try {
			Program program = new Program();
			msg = program.getResource();
		} catch(IOException e) {
			msg = "An error occurred while read";
		}

		System.out.println(msg);
	}
}

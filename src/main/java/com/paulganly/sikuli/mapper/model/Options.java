package com.paulganly.sikuli.mapper.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Options {

	private static String guiCreateFolder;

	private static Properties prop = null;

	public Options() {

		prop = new Properties();

		try (FileInputStream in = new FileInputStream("src/test/resources/screenmapper.properties")) {
			prop.load(in);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		setOptionsValuesFromProperties();

	}

	private static void setOptionsValuesFromProperties() {
		setGuiCreateFolder(prop.getProperty("GuiCreateFolder"));

	}

	public static void saveOptions(Properties properties) {
		prop.clear();
		prop = properties;
		setOptionsValuesFromProperties();

		try (FileOutputStream out = new FileOutputStream("src/test/resources/lptautotest.properties")) {
			prop.store(out, null);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public static String getGuiCreateFolder() {
		return guiCreateFolder;
	}

	public static void setGuiCreateFolder(String guiCreateFolder) {
		Options.guiCreateFolder = guiCreateFolder;
	}

}

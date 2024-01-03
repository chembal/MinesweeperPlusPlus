package com.chembal.minesweeper.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class GameProperties {

	private int helpLevel = 0;
	private int autoSpeed = 3;
	private int left = 0;
	private int top = 0;
	private boolean cds = false;
	private char difficulty = 'E';
	
	private int customWidth = 30;
	private int customHeight = 20;
	private int customMines = 20;


	public GameProperties() {
		try {
			InputStream input = new FileInputStream("config.properties");
			Properties p = new Properties();
			p.load(input);

			helpLevel = getConstrainedIntProperty(p,"game.help-level",1,3);
			autoSpeed = getConstrainedIntProperty(p,"ui.auto-speed",1,4);
			left = Integer.parseInt(p.getProperty("ui.left"));
			top = Integer.parseInt(p.getProperty("ui.top"));
			cds = "true".equalsIgnoreCase(p.getProperty("game.cds"));
			difficulty = p.getProperty("game.difficulty").charAt(0);
			customWidth = getConstrainedIntProperty(p, "minefield.width", 1, 100);
			customHeight = getConstrainedIntProperty(p, "minefield.height", 1, 100);
			customMines = getConstrainedIntProperty(p, "minefield.mine-count", 1, 100);
		} catch (Exception e) {
			// Use defaults
		}
	}

	// Enforce min and max values for constrained properties
	private int getConstrainedIntProperty(Properties properties, String propertyName, int min, int max) {
		int val = Integer.parseInt(properties.getProperty(propertyName));
		if (val < min) val = min;
		if (val > max) val = max;
		return val;
	}

	public void store() {
		try {
			Properties p = new Properties();
			p.setProperty("game.help-level", "" + helpLevel);
			p.setProperty("ui.auto-speed", "" + autoSpeed);
			p.setProperty("ui.left", "" + left);
			p.setProperty("ui.top", "" + top);
			p.setProperty("game.cds", cds ? "true" : "false");
			p.setProperty("game.difficulty", "" + difficulty);
			p.setProperty("minefield.width", "" + customWidth);
			p.setProperty("minefield.height", "" + customHeight);
			p.setProperty("minefield.mine-count", "" + customMines);
			FileOutputStream out = new FileOutputStream("config.properties");
			p.store(out," --- Minesweeper++ ---");
			out.close();
		} catch (Exception e) {
			// Ignore problem - defaults will be used next time, and that's fine.
		}
	}

	/**
	 * return empty string for directory urls (that is, urls ending with a
	 * slash) e.g. http://www.host.org/path/path/ --> empty string return file
	 * name (that is, last path entry) for all other urls e.g.
	 * http://www.host.org/path/path/filename --> filename
	 */
	private String getUrlFileName(URL url) {
		String path = url.getPath();

		if (path.endsWith("/"))
			// it's a directory; no file name available
			return "";

		String name = "";
		int index = path.lastIndexOf('/');
		if (index != -1)
			name = path.substring(index + 1);

		return name;
	}

	private int parseInt(String val, int defaultValue, int min, int max) {
		int retVal = parseInt(val, defaultValue);
		if (retVal < min) {
			return min;
		} else if (retVal > max) {
			return max;
		} else {
			return retVal;
		}
	}
	private int parseInt(String val, int defaultValue) {
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public int getHelpLevel() {
		return helpLevel;
	}

	public void setHelpLevel(int helpLevel) {
		this.helpLevel = helpLevel;
	}

	public int getAutoSpeed() {
		return autoSpeed;
	}

	public void setAutoSpeed(int autoSpeed) {
		this.autoSpeed = autoSpeed;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public boolean isCds() {
		return cds;
	}

	public void setCds(boolean cds) {
		this.cds = cds;
	}

	public char getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(char difficulty) {
		this.difficulty = difficulty;
	}

	public int getCustomWidth() {
		return customWidth;
	}

	public void setCustomWidth(int customWidth) {
		this.customWidth = customWidth;
	}

	public int getCustomHeight() {
		return customHeight;
	}

	public void setCustomHeight(int customHeight) {
		this.customHeight = customHeight;
	}

	public int getCustomMines() {
		return customMines;
	}

	public void setCustomMines(int customMines) {
		this.customMines = customMines;
	}
}

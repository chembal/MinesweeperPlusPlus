package com.chembal.minesweeper.ui.gui;

import java.awt.Color;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class GameSquare extends JLabel {
	
	public static final int GAMESQUARE_UNKNOWN = -1;
	public static final int GAMESQUARE_MINED = -2;
	public static final int GAMESQUARE_MARKED = -3;
	public static final int GAMESQUARE_MARKED_WRONGLY = -4;

	public Point location = null;

	public GameSquare() { this(GAMESQUARE_UNKNOWN); }
	public GameSquare(int value) {
		super();
		setValue(value);
	}

	public void setValue(int value) {
		switch (value) {
			case GAMESQUARE_MINED:
				setIcon(getIcon(9));
				setForeground(Color.red);
				break;
			case GAMESQUARE_UNKNOWN:
				setIcon(getIcon(10));
				setForeground(Color.black);
				break;
			case GAMESQUARE_MARKED:
				setIcon(getIcon(11));
				setForeground(Color.orange);
				break;
			case GAMESQUARE_MARKED_WRONGLY:
				setIcon(getIcon(12));
				setForeground(Color.orange);
				break;
			default:
				setIcon(getIcon(value));
				setForeground(Color.black);
		}
	}

	private ImageIcon getIcon(int value) {
	    return new ImageIcon(getClass().getResource("j" + value + ".gif"));
	}
}

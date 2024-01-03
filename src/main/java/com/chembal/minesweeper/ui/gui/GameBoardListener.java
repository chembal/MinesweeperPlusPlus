package com.chembal.minesweeper.ui.gui;

import java.awt.Point;

public interface GameBoardListener {
	public void requestGuess(Point p);
	public void requestMark(Point p);
	public void requestCheck(Point p);
}

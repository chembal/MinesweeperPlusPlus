package com.chembal.minesweeper.core;
public class NoSuchSquareException extends Exception {
	public NoSuchSquareException() { super("Square does not exist."); }
}

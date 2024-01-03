package com.chembal.minesweeper.core;
public class ValueUnknownException extends Exception {
	public ValueUnknownException() { super("The value for this location is unknown.  Unable to perform requested action."); }
}

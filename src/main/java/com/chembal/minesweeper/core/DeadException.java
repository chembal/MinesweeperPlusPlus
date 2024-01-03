package com.chembal.minesweeper.core;
public class DeadException extends Exception {
	public DeadException() { super("Field is dead.  Unable to perform action."); }
}

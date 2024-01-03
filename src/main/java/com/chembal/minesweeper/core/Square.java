package com.chembal.minesweeper.core;
public class Square {
	private boolean mined = false;
	private boolean known = false;
	private boolean marked = false;
	
	public boolean isKnown() {
		return known;
	}

	public boolean isMined() {
		return mined;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public void setMined(boolean mined) {
		this.mined = mined;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

}

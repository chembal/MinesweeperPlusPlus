package com.chembal.minesweeper.ai;
public interface UserHelper {
	
	public double[][] getProbabilities();
	
	public Suggestion getSuggestion();
	
	public boolean doObvious();
	
	public void autoPlay();
	
	public void autoPlayAll();
}

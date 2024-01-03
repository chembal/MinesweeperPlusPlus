package com.chembal.minesweeper.ai;
import java.awt.Point;

public class Suggestion {
	
	public static final int RECOMMEND_NOTHING = 0;
	public static final int RECOMMEND_GUESS = 1;
	public static final int RECOMMEND_MARK = 2;
	
	public Point point;
	public int recommendation;
	public double certainty;
	
	public Suggestion() { this(null,RECOMMEND_NOTHING,0); }
	public Suggestion(int x, int y, int recommend, double certainty) { this(new Point(x,y),recommend, certainty); }
	public Suggestion(Point point, int recommendation, double certainty) {
		this.point = point;
		this.recommendation = recommendation;
		this.certainty = certainty;
	}
}

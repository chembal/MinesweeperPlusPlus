package com.chembal.minesweeper.ai;

import com.chembal.minesweeper.core.Field;

import java.util.Random;

public abstract class AI {
	protected Field field;
	protected Random random = new Random();

	public AI(Field field) {
		this.field = field;
	}	

	public void play() {
		while (!finished()) makeRandomGuess();
	}
	public void play(boolean noGuessing) {
		if (!noGuessing) play();
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	protected boolean finished() {
		return !(field.isAlive() && !field.isWon());
	}

	protected void makeRandomGuess() {
		int x;
		int y;
		
		try {
			do {
				x = random.nextInt(field.getWidth());
				y = random.nextInt(field.getHeight());
			} while (!field.squareExists(x,y) || field.isMarked(x,y) || field.isKnown(x,y));
			field.guess(x,y);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

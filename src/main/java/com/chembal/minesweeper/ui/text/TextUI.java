package com.chembal.minesweeper.ui.text;

import com.chembal.minesweeper.core.Field;
import com.chembal.minesweeper.ai.LogicalAI;
import com.chembal.minesweeper.ai.Suggestion;
import com.chembal.minesweeper.ai.UserHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class TextUI {

	public static void main(String[] args) {
		Field field = new Field(30,16,99);
		UserHelper uh = new LogicalAI(field);
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("00.00");
		int x;
		int y;
		boolean autoplay = false;
		
		BufferedReader is = new BufferedReader(new InputStreamReader(new BufferedInputStream(System.in)));

		System.out.println(field.toString());

		try {
			System.out.println(" > ");
			do {
				String cmd = is.readLine().toUpperCase();
				if (cmd.startsWith("S")) {
					Suggestion s = uh.getSuggestion();
					System.out.print("\n\rSUGGESTION: ");
					switch (s.recommendation) {
						case Suggestion.RECOMMEND_NOTHING:
							System.out.println("No suggestion available.");
							break;
						case Suggestion.RECOMMEND_GUESS:
							System.out.println("Guess point [" + s.point.x + "," + s.point.y + "]. (" + df.format(s.certainty) + "% certainty)");
							break;
						case Suggestion.RECOMMEND_MARK:
							System.out.println("Mark point [" + s.point.x + "," + s.point.y + "]. (" + df.format(s.certainty) + "% certainty)");
							break;
					}
				}
				if (cmd.startsWith("P")) {
					double[][] probs = uh.getProbabilities();
					showProbabilities(probs);
				}
				if (cmd.startsWith("A")) {
					autoplay = !autoplay;
					if (autoplay) {
						System.out.println("Advanced autoplay on.");
						uh.autoPlay();
						System.out.println("\n\r" + field.toString());
					} else {
						System.out.println("Advanced autoplay off.");
					}
				}
				if (cmd.startsWith("G")) {
					try {
						x = Integer.parseInt(cmd.substring(1,3));
						y = Integer.parseInt(cmd.substring(3,5));
						field.guess(x,y);
						if (field.isAlive()) {
							uh.doObvious();
							if (autoplay) uh.autoPlay();
						}
						System.out.println("Guessing " + x + "," + y + ": \n\r" + field.toString());
						if (!field.isAlive()) {
							System.out.println("You died.");
							field.resetField();
							System.out.println(field.toString());
						}
					} catch (Exception e) {
						System.out.println("Invalid parameters.");
					}
				}
				if (cmd.startsWith("M")) {
					try {
						x = Integer.parseInt(cmd.substring(1,3));
						y = Integer.parseInt(cmd.substring(3,5));
						field.mark(x,y);
						if (field.isAlive()) {
							uh.doObvious();
							if (autoplay) uh.autoPlay();
						}
						System.out.println("Marking " + x + "," + y + ": \n\r" + field.toString());
						if (!field.isAlive()) {
							System.out.println("You died.");
							field.resetField();
							System.out.println(field.toString());
						}
					} catch (Exception e) {
						System.out.println("Invalid parameters.");
					}
				}
				if (cmd.startsWith("Z")) {
					field.setAutoDumpField(true);
					field.setForcedDelay(100);
					uh.autoPlayAll();
					System.out.println("Autoplayed entire game:\n\r" + field.toString());
					if (field.isWon()) {
						System.out.println("You won.");
					} else {
						System.out.println("You lost.");
					}
					field.resetField();
					System.out.println(field.toString());
				}
				if (cmd.startsWith("C")) {
					field.setAutoDumpField(true);
					field.setForcedDelay(10);
					while (true) {
						uh.autoPlayAll();
						if (field.isWon()) {
							System.out.println("Autoplayed entire game, you won.\n\r\n\r\n\r");
						} else {
							System.out.println("Autoplayed entire game, you lost.\n\r\n\r\n\r");
						}
						field.resetField();
					}
				}				
				if (cmd.startsWith("E")) {
					System.out.println("Goodbye.");
					System.exit(0);
				}
			} while (true);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void showProbabilities(double[][] probs) {
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("00");
		
		for (int y = 0; y < probs[0].length; y++) {
			for (int x = 0; x < probs.length; x++) {
				if (probs[x][y] < 0)
					System.out.print("XX ");
				else
					System.out.print(df.format(probs[x][y]) + " ");
			}
			System.out.println();
		}
	}
}

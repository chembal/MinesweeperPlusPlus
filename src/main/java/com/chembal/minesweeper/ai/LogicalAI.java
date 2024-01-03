package com.chembal.minesweeper.ai;

import com.chembal.minesweeper.core.Field;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class LogicalAI extends AI implements UserHelper {

	private boolean noGuessing = false;
	
	public LogicalAI(Field field) {
		super(field);
	}

	public void autoPlay() {
		play(true);
	}

	public void autoPlayAll() {
		play();
	}

	public void play() {
		play(false);
	}
	public void play(boolean noGuessing) {
		this.noGuessing = noGuessing;
		
		boolean changed;
		int noChangeCount = 0;

		do {
			changed = false;

			if (!finished()) {
				if (doObvious()) changed = true;
			}

			if (!finished()) {
				if (doTricky()) changed = true;
			}
			
			if (!changed) {
				noChangeCount++;
				if (noChangeCount > 5) break;
			}
		} while (!finished());			
	}

	private boolean doTricky() {
		// Get known facts
		List<Fact> facts = getKnownFacts();
		
		// Loop through every square.
		for (int x = 0; x < field.getWidth(); x++)
			for (int y = 0; y < field.getHeight(); y++) {
				// If any of them help, use the info.
				List<Fact> relFacts = getRelevantFacts(x,y,facts);
				if (!relFacts.isEmpty())
					if (useFacts(x,y,relFacts)) {
						return true;
					}
			}
		
		if (noGuessing) {
			return false;
		} else {
			if (!doObvious()) makeProbabilityBasedGuess();
			return true;
		}
	}

	private boolean useFacts(int x, int y, List<Fact> facts) {
		boolean changed = false;
		
		try {
			if (!field.isKnown(x,y) || field.isMarked(x,y)) return false;
		} catch (Exception ex) { ex.printStackTrace(); }
		
		for (Fact f : facts) {
			// If a fact explains all remaining mines, guess the rest.
			if (factExplainsRemainingMines(x,y,f)) {
				if (guessUnexplained(x,y,f)) {
					doObvious();
					changed = true;
				}
			}
			
			// If unexplained squares matches number of mines unexplained, mark all unexplained.
			if (unexplainedMatchUnexplainedMines(x,y,f)) {
				if (markUnexplained(x,y,f)) {
					doObvious();
					changed = true;
				}
			}
		}
		
		return changed;
	}

	private boolean factExplainsRemainingMines(int x, int y, Fact f) {
		try {
			if (!field.isKnown(x,y))
				return false;
			else
				return (f.numInPoints == (field.getNumberMinedAboutSquare(x,y) - field.getNumberMarkedAboutSquare(x,y)));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean unexplainedMatchUnexplainedMines(int x, int y, Fact f) {
		try {
			if (!field.isKnown(x,y))
				return false;
			else {
				int minesUnexplained = field.getNumberMinedAboutSquare(x,y) - field.getNumberMarkedAboutSquare(x,y);
				int unexplainedSquaresNotInFact = getUnexplainedSquaresNotInFact(x,y,f);
				if (minesUnexplained == (unexplainedSquaresNotInFact + f.numInPoints)) {
					if (minesUnexplained > f.numInPoints) {
						return true;
					} else return false;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private int getUnexplainedSquaresNotInFact(int x, int y, Fact f) {
		int numOfSquares = 0;
		
		try {
			for (int xoffset = -1; xoffset <= 1; xoffset++)
				for (int yoffset = -1; yoffset <= 1; yoffset++) {
					if (field.squareExists((x + xoffset),(y + yoffset))) {
						if (!field.isKnown((x + xoffset),(y + yoffset)) && !field.isMarked((x + xoffset),(y + yoffset)) && !explainedInFact((x + xoffset),(y + yoffset),f)) {
							numOfSquares++;
						}
					}
				}
		} catch (Exception e) { e.printStackTrace(); }
		return numOfSquares;
	}

	private boolean markUnexplained(int x, int y, Fact f) {
		boolean changed = false;
		
		try {
			for (int xoffset = -1; xoffset <= 1; xoffset++)
				for (int yoffset = -1; yoffset <= 1; yoffset++) {
					if (field.squareExists((x + xoffset),(y + yoffset))) {
						if (!field.isKnown((x + xoffset),(y + yoffset)) && !explainedInFact((x + xoffset),(y + yoffset),f) && !field.isMarked((x + xoffset),(y + yoffset))) {
							field.mark((x + xoffset),(y + yoffset));
							changed = true;
						}
					}
				}
		} catch (Exception e) { e.printStackTrace(); }

		return changed;
	}

	private boolean explainedInFact(int x, int y, Fact f) {
		Point pointA = new Point(x,y);
		
		for (Point pointB: f.points) {
			if (pointA.equals(pointB)) return true;
		}
		
		return false;
	}

	private boolean guessUnexplained(int x, int y, Fact f) {
		boolean changed = false;

		try {
			for (int xoffset = -1; xoffset <= 1; xoffset++)
				for (int yoffset = -1; yoffset <= 1; yoffset++) {
					if (field.squareExists((x + xoffset),(y + yoffset))) {
						if (!field.isKnown((x + xoffset),(y + yoffset)) && !explainedInFact((x + xoffset),(y + yoffset),f) && !field.isMarked((x + xoffset),(y + yoffset))) {
							field.guess((x + xoffset),(y + yoffset));
							changed = true;
						}
					}
				}
		} catch (Exception e) { e.printStackTrace(); }

		return changed;
	}

	private void makeProbabilityBasedGuess() {
		Point p = getBestGuess();
		
		try {
			if (p == null) {
				makeRandomGuess();
			} else {
				field.guess(p.x,p.y);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Fact> getRelevantFacts(int x, int y, List<Fact> facts) {
		// Return facts which are entirely within the domain of point (x,y)
		List<Fact> relevantFacts = new ArrayList<Fact>();
		
		for (Fact f : facts) {
			if (isFactInDomain(x,y,f)) {
				relevantFacts.add(f);
			}
		}
		
		return relevantFacts;
	}

	private boolean isFactInDomain(int x, int y, Fact fact) {
		for (Point p : fact.points) {
			if (!isPointNear(x,y,p)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isPointNear(int x, int y, Point p) {
		return (Math.abs(Point.distance(x,y,p.x,p.y)) < 1.5);
	}

	private List<Fact> getKnownFacts() {
		List<Fact> facts = new ArrayList<Fact>();
		
		// Add obvious facts.
		try {
			for (int x = 0; x < field.getWidth(); x++)
				for (int y = 0; y < field.getHeight(); y++)
					if (field.isKnown(x,y)) {
						addFactForSquare(facts,x,y);
					}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		// Add derived facts.
		return facts;
	}

	private void addFactForSquare(List<Fact> facts, int x, int y) {
		Fact fact = new Fact();

		try {
			fact.numInPoints = field.getNumberMinedAboutSquare(x,y) - field.getNumberMarkedAboutSquare(x,y);
			for (int xoffset = -1; xoffset <= 1; xoffset++)
				for (int yoffset = -1; yoffset <= 1; yoffset++) {
					if (field.squareExists((x + xoffset),(y + yoffset))) {
						if (!field.isKnown((x + xoffset),(y + yoffset)) && !field.isMarked((x + xoffset),(y + yoffset))) {
							fact.points.add(new Point((x + xoffset),(y + yoffset)));
						}
					}
				}
		} catch (Exception e) { e.printStackTrace(); }
		if (fact.numInPoints > 0) facts.add(fact);
	}

	public boolean doObvious() {
		boolean changed;
		boolean changedAtAll = false;
		
		try {
			do {
				changed = false;
				
				// Mark obvious
				for (int x = 0; x < field.getWidth(); x++)
					for (int y = 0; y < field.getHeight(); y++)
						if (field.isKnown(x,y)) {
							if (field.getNumberMinedAboutSquare(x,y) == (field.getNumberUnknownAboutSquare(x,y))) {
								if (field.markAllAroundSquare(x,y)) changed = true;
							}
						}
						
				// Clear obvious
				for (int x = 0; x < field.getWidth(); x++)
					for (int y = 0; y < field.getHeight(); y++)
						if (field.isKnown(x,y)) {
							if (field.getNumberMinedAboutSquare(x,y) == (field.getNumberMarkedAboutSquare(x,y))) {
								if (field.testAssumptions(x,y)) changed = true;
							}
						}

				if (changed) changedAtAll = true;
			} while (changed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return changedAtAll;
	}
	
	public double[][] getProbabilities() { return getProbabilities(false); }
	private double[][] getProbabilities(boolean forInternalUse) {
		// Return 2-d array of probabilities that a mine exists.
		// Set value to be negative if it is already marked or known.
		
		List<Fact> facts = getKnownFacts();
		
		double[][] probs = new double[field.getWidth()][field.getHeight()];
		
		// Use facts to determine probabilities.
		try {
			for (Fact f : facts) {
				double prob = (double) f.numInPoints / (double) f.points.size() * 100.0d;

				for (Point p : f.points) {
					if (field.isMarked(p.x,p.y) || field.isKnown(p.x,p.y)) {
						probs[p.x][p.y] = -1;
					} else {
						if (prob > probs[p.x][p.y])
							probs[p.x][p.y] = prob;
					}
				}
			}
		
			// If any squares are still unknown, set to the default probability.

			double defaultProbability = (double) field.getRemainingMines() / (double) field.getRemainingUnknownUnmarked() * 100.0d;
			
			for (int x = 0; x < field.getWidth(); x++) {
				for (int y = 0; y < field.getHeight(); y++) {
					if (field.isMarked(x,y) || field.isKnown(x,y)) {
						probs[x][y] = -1;
					} else {
						if (!(probs[x][y] > 0)) probs[x][y] = defaultProbability;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (forInternalUse)
			return probs;
		else
			return augmentProbs(probs);
	}

	private double[][] augmentProbs(double[][] probs) {
		return probs;
	}

	public Suggestion getSuggestion() {
		double[][] probs = getProbabilities(false);
		double lowest = 100;
		Point lowPoint = null;
		
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = 0; y < field.getHeight(); y++) {
				if (probs[x][y] == 0) {
					return new Suggestion(x,y,Suggestion.RECOMMEND_GUESS,100.0d);
				} else if (probs[x][y] == 100) {
					return new Suggestion(x,y,Suggestion.RECOMMEND_MARK,100.0d);					
				} else if ((probs[x][y] > 0) && (probs[x][y] < lowest)) {
					lowest = probs[x][y];
					lowPoint = new Point(x,y);
				}
			}
		}

		if (lowPoint != null)
			return new Suggestion(getBestGuess(),Suggestion.RECOMMEND_GUESS,lowest);
		else
			return new Suggestion();
	}

	private Point getBestGuess() {
		double lowest = 100;
		double[][] probabilities = getProbabilities(true);
		
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = 0; y < field.getHeight(); y++) {
				if ((probabilities[x][y] >= 0) && probabilities[x][y] < lowest) {
					lowest = probabilities[x][y];
				}
			}
		}
		
		Vector<Point> v = new Vector<Point>();
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = 0; y < field.getHeight(); y++) {
				if (probabilities[x][y] == lowest) {
					v.add(new Point(x,y));
				}
			}
		}
		
		if (lowest == 100) {
			return null;
		} else {
			// Return the point with the best probability of no hit.
			// If multiple entries with lowest probability, pick one
			// of them at random.
			return (Point) v.elementAt(random.nextInt(v.size()));
		}
	}	
	
	private class Fact {
		public List<Point> points = new ArrayList<Point>();   // Vector of Point objects.
		public int numInPoints;
		
		public String toString() {
			StringBuffer sb = new StringBuffer("" + numInPoints + " in (");
			boolean firstElement = true;
			
			for (Point p : points) {
				if (firstElement) 
					firstElement = false;
				else
					sb.append(",");
				sb.append("[" + p.x + "," + p.y + "]");
			}
			
			return sb.toString() + ")";
		}
	}
}

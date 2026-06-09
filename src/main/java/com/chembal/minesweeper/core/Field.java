package com.chembal.minesweeper.core;
import java.util.Random;

public class Field {

	private Square field[][] = null;
	
	private FieldListener listener = null;
	
	private int width = 10;
	private int height = 10;
	private int mines = 10;
	private boolean coastalDefenseSystem = false;
	private boolean alive = true;
	private boolean started = false;
	private boolean autoDumpField = false;
	private long forcedDelay = 0;

	// --- Functional interfaces for neighbor iteration ---

	@FunctionalInterface
	public interface SquareAction {
		void apply(int x, int y);
	}

	@FunctionalInterface
	public interface SquarePredicate {
		boolean test(int x, int y);
	}

	// --- Shared utility methods ---

	public void forEachNeighbor(int x, int y, SquareAction action) {
		for (int xoffset = -1; xoffset <= 1; xoffset++)
			for (int yoffset = -1; yoffset <= 1; yoffset++) {
				int nx = x + xoffset;
				int ny = y + yoffset;
				if (squareExists(nx, ny))
					action.apply(nx, ny);
			}
	}

	public int countNeighbors(int x, int y, SquarePredicate predicate) {
		int[] count = {0};
		forEachNeighbor(x, y, (nx, ny) -> {
			if (predicate.test(nx, ny)) count[0]++;
		});
		return count[0];
	}

	private void validateSquare(int x, int y) throws NoSuchSquareException {
		if (!squareExists(x, y)) throw new NoSuchSquareException();
	}

	private void validateAlive() throws DeadException {
		if (!alive) throw new DeadException();
	}

	private void notifyListener() {
		if (listener != null) listener.boardChanged();
	}

	// --- Constructors ---

	public Field() { resetField(); }
	public Field(int width, int height) { this(width, height, false); }
	public Field(int width, int height, boolean coastalDefenseSystem) { 
		this.width = width;
		this.height = height;
		this.coastalDefenseSystem = coastalDefenseSystem;
		resetField();
    }
	public Field(int width, int height, int mines) { this(width, height, mines, false); }
	public Field(int width, int height, int mines, boolean coastalDefenseSystem) {
		this.width = width;
		this.height = height;
		this.mines = mines;
		this.coastalDefenseSystem = coastalDefenseSystem;
		resetField();
	}
	public Field(boolean[][] fieldMap) {
		this.width = fieldMap.length;
		this.height = fieldMap[0].length;
		resetField(fieldMap);
	}

	public void addFieldListener(FieldListener fl) {
		listener = fl;
	}

	public void resetField() {
		// Reset field
		alive = true;
		started = false;
		field = new Square[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				field[x][y] = new Square();
				
		// Randomize mine placement
		for (int i = 0; i < mines; i++) placeMine();

		// Coastal defense
		if (coastalDefenseSystem) {
			for (int x = 0; x < width; x++) {
				if (x == 0 || x == (width - 1)) {
					for (int y = 0; y < height; y++) {
						field[x][y].setKnown(true);
					}
				} else {
					field[x][0].setKnown(true);
					field[x][height - 1].setKnown(true);
				}
			}
		}
		
		notifyListener();
	}
	public void resetField(boolean[][] fieldMap) {
		// Reset field, using the supplied map.
		alive = true;
		started = false;
		field = new Square[width][height];
		mines = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				field[x][y] = new Square();
				if (fieldMap[x][y]) {
					field[x][y].setMined(true);
					mines++;
				}
			}

		notifyListener();
	}

	public boolean isStarted() { return started; }

	private void placeMine() {
		// Don't collide with other randomizers.
		Random r = new Random((long) (System.currentTimeMillis() / 37));
		boolean placed = false;
		int x;
		int y;
		
		do {
			if (coastalDefenseSystem) {
				x = r.nextInt(width - 2) + 1;
				y = r.nextInt(height - 2) + 1;
			} else {
				x = r.nextInt(width);
				y = r.nextInt(height);
			}
			if (!field[x][y].isMined()) {
				field[x][y].setMined(true);
				placed = true;
			}
		} while (!placed);
	}

	public void guess(int x, int y) throws NoSuchSquareException, DeadException {
		validateAlive();
		started = true;
		validateSquare(x, y);
		field[x][y].setKnown(true);
		if (field[x][y].isMined()) {
			alive = false;
		} else {
			sweepField();
		}
		if (autoDumpField) printDebug();

		notifyListener();

		if (isWon() || !isAlive()) started = false;
	}
	
	public void mark(int x, int y) throws NoSuchSquareException, DeadException { mark(x,y,true); }
	public void unmark(int x, int y) throws NoSuchSquareException, DeadException { mark(x,y,false); }
	private void mark(int x, int y, boolean value) throws NoSuchSquareException, DeadException {
		validateAlive();
		started = true;
		validateSquare(x, y);
		field[x][y].setMarked(value);
		if (autoDumpField) printDebug();
		if (forcedDelay > 0) { try { Thread.sleep(forcedDelay); } catch (Exception e) {} }

		notifyListener();

		if (isWon() || !isAlive()) started = false;
	}
	
	private void printDebug() {
		System.out.println(toString() + "\n\r\n\r");
	}
	
	public int getRemainingMines() {
		int count = mines;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (field[x][y].isMarked()) count--;
			}
		}
		
		return count;
	}
	
	public int getRemainingUnknownUnmarked() {
		int count = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!field[x][y].isMarked() && !field[x][y].isKnown()) count++;
			}
		}
		
		return count;
	}
	
	public boolean squareExists(int x, int y) {
		return ((x >= 0) && (x < width) && (y >= 0) && (y < height));
	}
	
	public boolean isMarked(int x, int y) throws NoSuchSquareException {
		validateSquare(x, y);
		return field[x][y].isMarked();
	}
	
	public boolean isKnown(int x, int y) throws NoSuchSquareException {
		validateSquare(x, y);
		return field[x][y].isKnown();
	}
	
	public boolean isWon() {
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if ((!field[x][y].isMarked() && !field[x][y].isKnown()))
					return false;

		return true;
	}
	
	public boolean isMined(int x, int y) throws NoSuchSquareException, ValueUnknownException {
		validateSquare(x, y);
		if (field[x][y].isKnown() || !alive)
			return field[x][y].isMined();
		else
			throw new ValueUnknownException();
	}
	
	public int getNumberMinedAboutSquare(int x, int y) throws NoSuchSquareException, ValueUnknownException {
		return getNumberMinedAboutSquare(x,y,false);
	}
	private int getNumberMinedAboutSquare(int x, int y, boolean bypassSecurity) throws NoSuchSquareException, ValueUnknownException { 
		validateSquare(x, y);
		if (!field[x][y].isKnown() && !bypassSecurity && isAlive())
			throw new ValueUnknownException();
		return countNeighbors(x, y, (nx, ny) -> field[nx][ny].isMined());
	}
	
	public int getNumberMarkedAboutSquare(int x, int y) throws NoSuchSquareException { 
		validateSquare(x, y);
		return countNeighbors(x, y, (nx, ny) -> field[nx][ny].isMarked());
	}
	
	public boolean markAllAroundSquare(int x, int y) throws NoSuchSquareException, DeadException {
		validateSquare(x, y);
		boolean changed = false;
		for (int xoffset = -1; xoffset <= 1; xoffset++)
			for (int yoffset = -1; yoffset <= 1; yoffset++) {
				int nx = x + xoffset;
				int ny = y + yoffset;
				if (squareExists(nx, ny) && !field[nx][ny].isKnown()) {
					if (!field[nx][ny].isMarked()) {
						mark(nx, ny);
						changed = true;
					}
				}
			}
		return changed;
	}

	@SuppressWarnings("finally")
	public boolean testAssumptions(int x, int y) throws NoSuchSquareException {
		boolean changed = false;
		
		try {
			if (getNumberMinedAboutSquare(x,y) == (getNumberMarkedAboutSquare(x,y))) {
				for (int xoffset = -1; xoffset <= 1; xoffset++)
					for (int yoffset = -1; yoffset <= 1; yoffset++) {
						int nx = x + xoffset;
						int ny = y + yoffset;
						if (squareExists(nx, ny) && !field[nx][ny].isKnown() && !field[nx][ny].isMarked()) {
							guess(nx, ny);
							changed = true;
						}
					}
			}
		} catch (ValueUnknownException e) {
		} finally {
			return changed;
		}
	}

	public int getNumberUnknownAboutSquare(int x, int y) throws NoSuchSquareException { 
		validateSquare(x, y);
		return countNeighbors(x, y, (nx, ny) -> !field[nx][ny].isKnown());
	}

	private boolean setKnownAboutSquare(int x, int y) {
		boolean[] changed = {false};
		forEachNeighbor(x, y, (nx, ny) -> {
			if (!field[nx][ny].isKnown()) {
				field[nx][ny].setKnown(true);
				changed[0] = true;
			}
		});
		return changed[0];
	}

	public void sweepField() { 
		boolean changed;
		
		try {
			do {
				changed = false;
				for (int x = 0; x < width; x++)
					for (int y = 0; y < height; y++)
						if (field[x][y].isKnown() && getNumberMinedAboutSquare(x,y) == 0)
							if (setKnownAboutSquare(x,y)) changed = true;
			} while (changed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String toString() { return toString(false); }
	public String toString(boolean revealAll) {
		StringBuffer out = new StringBuffer();
		
		try {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (field[x][y].isKnown() || revealAll) {
						if (field[x][y].isMined()) {
							out.append("* ");
						} else {
							out.append("" + getNumberMinedAboutSquare(x,y,true) + " ");
						}
					} else {
						if (field[x][y].isMarked())
							out.append("M ");
						else
							out.append(". ");
					}
				}
				out.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (revealAll) alive = false; // No cheaters!
		return out.toString();
	}

	public boolean isAlive() {
		return alive;
	}

	public int getHeight() {
		return height;
	}

	public int getMines() {
		return mines;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setMines(int mines) {
		this.mines = mines;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isAutoDumpField() {
		return autoDumpField;
	}

	public void setAutoDumpField(boolean autoDumpField) {
		this.autoDumpField = autoDumpField;
	}

	public long getForcedDelay() {
		return forcedDelay;
	}

	public void setForcedDelay(long forcedDelay) {
		this.forcedDelay = forcedDelay;
	}

}

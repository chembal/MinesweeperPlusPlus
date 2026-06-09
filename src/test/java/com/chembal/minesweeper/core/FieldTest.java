package com.chembal.minesweeper.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    // Helper: build a deterministic field from a boolean map.
    // true = mine, false = safe.
    private Field fieldFrom(boolean[][] map) {
        return new Field(map);
    }

    // ---- Constructor / dimension tests ----

    @Test
    void defaultFieldDimensions() {
        Field f = new Field();
        assertEquals(10, f.getWidth());
        assertEquals(10, f.getHeight());
        assertEquals(10, f.getMines());
    }

    @Test
    void customDimensionConstructor() {
        Field f = new Field(5, 6);
        assertEquals(5, f.getWidth());
        assertEquals(6, f.getHeight());
    }

    @Test
    void customDimensionAndMinesConstructor() {
        Field f = new Field(8, 8, 3);
        assertEquals(8, f.getWidth());
        assertEquals(8, f.getHeight());
        assertEquals(3, f.getMines());
    }

    @Test
    void fieldFromBooleanMap() {
        boolean[][] map = {
            {false, false},
            {true,  false}
        };
        Field f = fieldFrom(map);
        assertEquals(2, f.getWidth());
        assertEquals(2, f.getHeight());
        assertEquals(1, f.getMines());
    }

    // ---- squareExists ----

    @Test
    void squareExistsInBounds() {
        Field f = new Field(3, 3, 0);
        assertTrue(f.squareExists(0, 0));
        assertTrue(f.squareExists(2, 2));
    }

    @Test
    void squareExistsOutOfBounds() {
        Field f = new Field(3, 3, 0);
        assertFalse(f.squareExists(-1, 0));
        assertFalse(f.squareExists(0, -1));
        assertFalse(f.squareExists(3, 0));
        assertFalse(f.squareExists(0, 3));
    }

    // ---- isAlive / isWon / isStarted ----

    @Test
    void newFieldIsAlive() {
        Field f = new Field(3, 3, 0);
        assertTrue(f.isAlive());
    }

    @Test
    void fieldNotStartedInitially() {
        Field f = new Field(3, 3, 0);
        assertFalse(f.isStarted());
    }

    @Test
    void winConditionNoMines() throws Exception {
        // A 2x2 field with no mines; guess all squares to win
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        // sweepField reveals all neighbours of zero-count squares
        f.guess(0, 0);
        assertTrue(f.isWon());
        assertTrue(f.isAlive());
    }

    @Test
    void guessMineKillsField() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // mine
        assertFalse(f.isAlive());
    }

    // ---- guess / mark ----

    @Test
    void guessNonMineRevealsSquare() throws Exception {
        boolean[][] map = {
            {false, true},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0);
        assertTrue(f.isKnown(0, 0));
        assertTrue(f.isAlive());
    }

    @Test
    void guessOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class, () -> f.guess(5, 5));
    }

    @Test
    void guessOnDeadFieldThrows() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // dies
        assertThrows(DeadException.class, () -> f.guess(1, 0));
    }

    @Test
    void markAndUnmark() throws Exception {
        boolean[][] map = {
            {false, false},
            {true,  false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // start the game with a safe square
        f.mark(1, 0);
        assertTrue(f.isMarked(1, 0));
        f.unmark(1, 0);
        assertFalse(f.isMarked(1, 0));
    }

    @Test
    void markOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class, () -> f.mark(10, 10));
    }

    @Test
    void markOnDeadFieldThrows() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // dies
        assertThrows(DeadException.class, () -> f.mark(1, 0));
    }

    // ---- getRemainingMines / getRemainingUnknownUnmarked ----

    @Test
    void remainingMinesDecreasesAfterMark() throws Exception {
        boolean[][] map = {
            {false, false},
            {true,  false}
        };
        Field f = fieldFrom(map);
        assertEquals(1, f.getRemainingMines());
        f.guess(0, 0); // safe, starts the game
        f.mark(1, 0);
        assertEquals(0, f.getRemainingMines());
    }

    @Test
    void remainingUnknownUnmarkedDecreasesAfterGuess() throws Exception {
        boolean[][] map = {
            {false, false},
            {true,  false}
        };
        Field f = fieldFrom(map);
        int beforeGuess = f.getRemainingUnknownUnmarked();
        f.guess(0, 1); // safe
        int afterGuess = f.getRemainingUnknownUnmarked();
        assertTrue(afterGuess < beforeGuess);
    }

    // ---- isKnown / isMarked with invalid coords ----

    @Test
    void isKnownOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class, () -> f.isKnown(5, 5));
    }

    @Test
    void isMarkedOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class, () -> f.isMarked(5, 5));
    }

    // ---- isMined ----

    @Test
    void isMinedOnKnownSquare() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(1, 0); // safe, reveals (1,0)
        assertFalse(f.isMined(1, 0));
    }

    @Test
    void isMinedOnUnknownSquareThrows() {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        // (0,0) is unknown and field is alive
        assertThrows(ValueUnknownException.class, () -> f.isMined(0, 0));
    }

    @Test
    void isMinedOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class, () -> f.isMined(5, 5));
    }

    @Test
    void isMinedAfterDeathRevealsAll() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // mine -> dies
        // After death, isMined should work even on unknown squares
        assertTrue(f.isMined(0, 0));
        assertFalse(f.isMined(1, 1));
    }

    // ---- getNumberMinedAboutSquare ----

    @Test
    void numberMinedAboutCornerSquare() throws Exception {
        // Mines at (0,1) and (1,0)
        boolean[][] map = {
            {false, true,  false},
            {true,  false, false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0);
        assertEquals(2, f.getNumberMinedAboutSquare(0, 0));
    }

    @Test
    void numberMinedAboutSquareUnknownThrows() {
        boolean[][] map = {
            {false, true},
            {true,  false}
        };
        Field f = fieldFrom(map);
        assertThrows(ValueUnknownException.class,
            () -> f.getNumberMinedAboutSquare(0, 0));
    }

    @Test
    void numberMinedAboutSquareOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class,
            () -> f.getNumberMinedAboutSquare(5, 5));
    }

    // ---- getNumberMarkedAboutSquare ----

    @Test
    void numberMarkedAboutSquare() throws Exception {
        boolean[][] map = {
            {false, false, false},
            {false, false, false},
            {true,  true,  false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // reveals the safe area
        f.mark(2, 0);
        assertEquals(1, f.getNumberMarkedAboutSquare(1, 0));
    }

    @Test
    void numberMarkedAboutSquareOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class,
            () -> f.getNumberMarkedAboutSquare(5, 5));
    }

    // ---- getNumberUnknownAboutSquare ----

    @Test
    void numberUnknownAboutSquare() throws Exception {
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        // Before any guesses, all 4 squares unknown; square (0,0) has 3 neighbours+self
        // After guessing (0,0) on a zero-mine field, sweep reveals all
        f.guess(0, 0);
        assertEquals(0, f.getNumberUnknownAboutSquare(0, 0));
    }

    @Test
    void numberUnknownAboutSquareOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class,
            () -> f.getNumberUnknownAboutSquare(5, 5));
    }

    // ---- sweepField (flood fill on zero-count squares) ----

    @Test
    void sweepFieldRevealsZeroCountArea() throws Exception {
        // Mine in bottom-right corner only
        boolean[][] map = {
            {false, false, false},
            {false, false, false},
            {false, false, true}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // triggers sweep
        // All squares except the mine and its direct neighbours with >0 count
        // should be revealed. (0,0) through the safe area gets revealed.
        assertTrue(f.isKnown(0, 0));
        assertTrue(f.isKnown(0, 2));
        assertTrue(f.isKnown(2, 0));
    }

    // ---- markAllAroundSquare ----

    @Test
    void markAllAroundSquare() throws Exception {
        boolean[][] map = {
            {false, false, false},
            {false, true,  false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // reveals (0,0), mine count = 1
        // markAllAroundSquare should mark all unknown neighbours
        assertTrue(f.markAllAroundSquare(0, 0));
    }

    @Test
    void markAllAroundSquareOutOfBoundsThrows() {
        Field f = new Field(3, 3, 0);
        assertThrows(NoSuchSquareException.class,
            () -> f.markAllAroundSquare(5, 5));
    }

    // ---- testAssumptions ----

    @Test
    void testAssumptionsGuessesWhenMarksMatchMineCount() throws Exception {
        boolean[][] map = {
            {false, true,  false},
            {false, false, false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // reveals (0,0), mine count around = 1
        f.mark(0, 1);  // mark the mine
        // Now mine count == marked count, testAssumptions should guess remaining unknowns
        boolean changed = f.testAssumptions(0, 0);
        assertTrue(changed);
    }

    @Test
    void testAssumptionsOutOfBoundsReturnsFalse() throws Exception {
        Field f = new Field(3, 3, 0);
        // testAssumptions suppresses exceptions via finally block
        assertFalse(f.testAssumptions(5, 5));
    }

    // ---- resetField ----

    @Test
    void resetFieldRestoresAliveState() throws Exception {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(0, 0); // dies
        assertFalse(f.isAlive());
        f.resetField();
        assertTrue(f.isAlive());
    }

    // ---- toString ----

    @Test
    void toStringContainsDotsForUnknown() {
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        String str = f.toString();
        assertTrue(str.contains("."));
    }

    @Test
    void toStringRevealAllShowsMines() {
        boolean[][] map = {
            {true,  false},
            {false, false}
        };
        Field f = fieldFrom(map);
        String str = f.toString(true);
        assertTrue(str.contains("*"));
    }

    // ---- coastalDefenseSystem ----

    @Test
    void coastalDefenseSystemRevealsEdges() throws Exception {
        Field f = new Field(8, 8, 5, true);
        // Edge squares should be known
        assertTrue(f.isKnown(0, 0));
        assertTrue(f.isKnown(0, 4));
        assertTrue(f.isKnown(7, 4));
        assertTrue(f.isKnown(4, 0));
        assertTrue(f.isKnown(4, 7));
    }

    // ---- FieldListener ----

    @Test
    void fieldListenerCalledOnGuess() throws Exception {
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        int[] callCount = {0};
        f.addFieldListener(() -> callCount[0]++);
        f.guess(0, 0);
        assertTrue(callCount[0] > 0);
    }

    @Test
    void fieldListenerCalledOnReset() {
        Field f = new Field(3, 3, 0);
        int[] callCount = {0};
        f.addFieldListener(() -> callCount[0]++);
        f.resetField();
        assertEquals(1, callCount[0]);
    }

    // ---- win by marking all mines ----

    @Test
    void winByMarkingAllMines() throws Exception {
        // Mine at (0,1)
        boolean[][] map = {
            {false, true},
            {false, false}
        };
        Field f = fieldFrom(map);
        f.guess(1, 0); // safe
        f.guess(1, 1); // safe
        f.guess(0, 0); // safe
        f.mark(0, 1);  // mark the mine
        assertTrue(f.isWon());
        assertTrue(f.isAlive());
    }

    // ---- setters / getters ----

    @Test
    void setAndGetDimensions() {
        Field f = new Field();
        f.setWidth(20);
        f.setHeight(15);
        f.setMines(5);
        assertEquals(20, f.getWidth());
        assertEquals(15, f.getHeight());
        assertEquals(5, f.getMines());
    }

    @Test
    void autoDumpFieldAccessor() {
        Field f = new Field();
        assertFalse(f.isAutoDumpField());
        f.setAutoDumpField(true);
        assertTrue(f.isAutoDumpField());
    }

    @Test
    void forcedDelayAccessor() {
        Field f = new Field();
        assertEquals(0, f.getForcedDelay());
        f.setForcedDelay(100);
        assertEquals(100, f.getForcedDelay());
    }
}

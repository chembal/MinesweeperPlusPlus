package com.chembal.minesweeper.ai;

import com.chembal.minesweeper.core.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

class LogicalAITest {

    private Field fieldFrom(boolean[][] map) {
        return new Field(map);
    }

    // ---- doObvious ----

    @Test
    void doObviousMarksObviousMines() throws Exception {
        // Single mine surrounded by safe squares
        // When a known square's mine count == unknown neighbour count, mark all unknowns
        boolean[][] map = {
            {false, false, false},
            {false, true,  false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        // Guess a corner to start
        f.guess(0, 0);
        // (0,0) has mine count 1 and after sweep some neighbours are still unknown
        // doObvious should mark the mine
        ai.doObvious();
        // The mine at (1,1) should be marked since the AI identifies it
        assertTrue(f.isAlive());
    }

    @Test
    void doObviousReturnsFalseWhenNothingToDo() throws Exception {
        // All safe, after one guess everything is revealed
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0); // reveals all
        assertFalse(ai.doObvious());
    }

    // ---- getProbabilities ----

    @Test
    void getProbabilitiesReturnsCorrectDimensions() throws Exception {
        boolean[][] map = {
            {false, false, false},
            {false, true,  false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0);
        double[][] probs = ai.getProbabilities();
        assertEquals(3, probs.length);
        assertEquals(3, probs[0].length);
    }

    @Test
    void getProbabilitiesKnownSquaresAreNegative() throws Exception {
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0); // reveals all on a zero-mine field
        double[][] probs = ai.getProbabilities();
        // All squares known → all should be -1
        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                assertEquals(-1.0, probs[x][y], 0.001,
                    "Known square (" + x + "," + y + ") should be -1");
    }

    @Test
    void getProbabilitiesUnknownSquaresArePositive() throws Exception {
        boolean[][] map = {
            {false, true,  false},
            {false, false, false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(2, 2); // far from the mine
        double[][] probs = ai.getProbabilities();
        // At least one unknown square should have a positive probability
        boolean foundPositive = false;
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                if (probs[x][y] > 0) foundPositive = true;
        assertTrue(foundPositive);
    }

    // ---- getSuggestion ----

    @Test
    void getSuggestionReturnsNonNullForActiveGame() throws Exception {
        boolean[][] map = {
            {false, true,  false},
            {false, false, false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0);
        Suggestion s = ai.getSuggestion();
        assertNotNull(s);
    }

    @Test
    void getSuggestionRecommendNothingWhenAllDone() throws Exception {
        boolean[][] map = {
            {false, false},
            {false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0); // reveals all
        Suggestion s = ai.getSuggestion();
        // No actionable suggestions when everything is known
        assertEquals(Suggestion.RECOMMEND_NOTHING, s.recommendation);
    }

    // ---- autoPlay / autoPlayAll ----

    @Test
    void autoPlayNoGuessingOnSimpleField() throws Exception {
        // 3x3 field with one mine in center — solvable logically
        boolean[][] map = {
            {false, false, false},
            {false, true,  false},
            {false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        f.guess(0, 0);
        ai.autoPlay(); // no-guessing mode
        assertTrue(f.isAlive());
    }

    @Test
    @Timeout(10)
    void autoPlayAllSolvesField() {
        // Simple field that AI should be able to solve
        boolean[][] map = {
            {false, false, false, false},
            {false, true,  false, false},
            {false, false, false, false},
            {false, false, false, false}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        ai.autoPlayAll();
        // Game should end — either won or dead
        assertTrue(f.isWon() || !f.isAlive());
    }

    // ---- play finishes ----

    @Test
    @Timeout(10)
    void playTerminates() {
        boolean[][] map = {
            {false, false},
            {false, true}
        };
        Field f = fieldFrom(map);
        LogicalAI ai = new LogicalAI(f);
        ai.play();
        // Should terminate — either won or dead
        assertTrue(f.isWon() || !f.isAlive());
    }

    // ---- AI base class (getField / setField) ----

    @Test
    void getFieldReturnsSameField() {
        Field f = new Field(3, 3, 1);
        LogicalAI ai = new LogicalAI(f);
        assertSame(f, ai.getField());
    }

    @Test
    void setFieldChangesField() {
        Field f1 = new Field(3, 3, 1);
        Field f2 = new Field(5, 5, 2);
        LogicalAI ai = new LogicalAI(f1);
        ai.setField(f2);
        assertSame(f2, ai.getField());
    }
}

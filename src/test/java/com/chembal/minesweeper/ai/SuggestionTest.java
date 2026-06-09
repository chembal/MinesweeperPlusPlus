package com.chembal.minesweeper.ai;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;

class SuggestionTest {

    @Test
    void defaultConstructor() {
        Suggestion s = new Suggestion();
        assertNull(s.point);
        assertEquals(Suggestion.RECOMMEND_NOTHING, s.recommendation);
        assertEquals(0, s.certainty, 0.001);
    }

    @Test
    void coordinateConstructor() {
        Suggestion s = new Suggestion(3, 4, Suggestion.RECOMMEND_GUESS, 75.5);
        assertEquals(3, s.point.x);
        assertEquals(4, s.point.y);
        assertEquals(Suggestion.RECOMMEND_GUESS, s.recommendation);
        assertEquals(75.5, s.certainty, 0.001);
    }

    @Test
    void pointConstructor() {
        Point p = new Point(5, 6);
        Suggestion s = new Suggestion(p, Suggestion.RECOMMEND_MARK, 100.0);
        assertSame(p, s.point);
        assertEquals(Suggestion.RECOMMEND_MARK, s.recommendation);
        assertEquals(100.0, s.certainty, 0.001);
    }

    @Test
    void constantValues() {
        assertEquals(0, Suggestion.RECOMMEND_NOTHING);
        assertEquals(1, Suggestion.RECOMMEND_GUESS);
        assertEquals(2, Suggestion.RECOMMEND_MARK);
    }
}

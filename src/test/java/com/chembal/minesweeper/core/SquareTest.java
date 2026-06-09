package com.chembal.minesweeper.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SquareTest {

    @Test
    void defaultSquareIsNotKnown() {
        Square sq = new Square();
        assertFalse(sq.isKnown());
    }

    @Test
    void defaultSquareIsNotMined() {
        Square sq = new Square();
        assertFalse(sq.isMined());
    }

    @Test
    void defaultSquareIsNotMarked() {
        Square sq = new Square();
        assertFalse(sq.isMarked());
    }

    @Test
    void setKnownTrue() {
        Square sq = new Square();
        sq.setKnown(true);
        assertTrue(sq.isKnown());
    }

    @Test
    void setMinedTrue() {
        Square sq = new Square();
        sq.setMined(true);
        assertTrue(sq.isMined());
    }

    @Test
    void setMarkedTrue() {
        Square sq = new Square();
        sq.setMarked(true);
        assertTrue(sq.isMarked());
    }

    @Test
    void setKnownCanBeToggled() {
        Square sq = new Square();
        sq.setKnown(true);
        assertTrue(sq.isKnown());
        sq.setKnown(false);
        assertFalse(sq.isKnown());
    }

    @Test
    void setMinedCanBeToggled() {
        Square sq = new Square();
        sq.setMined(true);
        assertTrue(sq.isMined());
        sq.setMined(false);
        assertFalse(sq.isMined());
    }

    @Test
    void setMarkedCanBeToggled() {
        Square sq = new Square();
        sq.setMarked(true);
        assertTrue(sq.isMarked());
        sq.setMarked(false);
        assertFalse(sq.isMarked());
    }
}

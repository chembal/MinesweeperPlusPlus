package com.chembal.minesweeper.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GamePropertiesTest {

    @Test
    void constructorDoesNotThrow() {
        assertDoesNotThrow(GameProperties::new);
    }

    @Test
    void setAndGetHelpLevel() {
        GameProperties gp = new GameProperties();
        gp.setHelpLevel(2);
        assertEquals(2, gp.getHelpLevel());
    }

    @Test
    void setAndGetAutoSpeed() {
        GameProperties gp = new GameProperties();
        gp.setAutoSpeed(1);
        assertEquals(1, gp.getAutoSpeed());
    }

    @Test
    void setAndGetDifficulty() {
        GameProperties gp = new GameProperties();
        gp.setDifficulty('H');
        assertEquals('H', gp.getDifficulty());
    }

    @Test
    void setAndGetCds() {
        GameProperties gp = new GameProperties();
        gp.setCds(true);
        assertTrue(gp.isCds());
        gp.setCds(false);
        assertFalse(gp.isCds());
    }

    @Test
    void setAndGetPosition() {
        GameProperties gp = new GameProperties();
        gp.setLeft(100);
        gp.setTop(200);
        assertEquals(100, gp.getLeft());
        assertEquals(200, gp.getTop());
    }

    @Test
    void setAndGetCustomDimensions() {
        GameProperties gp = new GameProperties();
        gp.setCustomWidth(50);
        gp.setCustomHeight(40);
        gp.setCustomMines(30);
        assertEquals(50, gp.getCustomWidth());
        assertEquals(40, gp.getCustomHeight());
        assertEquals(30, gp.getCustomMines());
    }

    @Test
    void difficultyDefaultIsE() {
        GameProperties gp = new GameProperties();
        assertEquals('E', gp.getDifficulty());
    }

    @Test
    void autoSpeedDefault() {
        GameProperties gp = new GameProperties();
        // config.properties has auto-speed=3
        assertEquals(3, gp.getAutoSpeed());
    }

    @Test
    void customDimensionsFromConfig() {
        GameProperties gp = new GameProperties();
        assertEquals(30, gp.getCustomWidth());
        assertEquals(20, gp.getCustomHeight());
        assertEquals(20, gp.getCustomMines());
    }
}

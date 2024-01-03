package com.chembal.minesweeper.ui.gui;

import com.chembal.minesweeper.core.Field;
import com.chembal.minesweeper.core.NoSuchSquareException;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class GameBoard extends JPanel implements MouseListener {
	
	private GameSquare[][] field;
	private GameBoardListener listener = null;
	private Field f = null;
	
	private GameBoard(int rows, int cols) {
		initBoard(rows,cols);
		setVisible(true);
	}

	public GameBoard(Field f) {
		this(f.getHeight(),f.getWidth());
		updateBoard(f);
	}
	
	private void initBoard(int rows, int cols) {
		field = new GameSquare[cols][rows];
		removeAll();
		GridLayout gl = new GridLayout(rows,cols);
		setLayout(gl);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				field[x][y] = new GameSquare();
				field[x][y].location = new Point(x,y);
				field[x][y].addMouseListener(this);
				add(field[x][y]);
			}
		}

		setPreferredSize(new Dimension((cols * 15),(rows * 15)));
	}
	
	public void updateBoard(Field f) {
		setVisible(false);
		
		if (this.f != null && f != null) {
			if ((this.f.getHeight() != f.getHeight()) || (this.f.getWidth() != f.getWidth())) {
				initBoard(f.getHeight(),f.getWidth());
			}
		}
		
		this.f = f;
		try {
			for (int x = 0; x < field.length; x++) {
				for (int y = 0; y < field[0].length; y++) {
					if (f.isKnown(x,y) || !f.isAlive()) {
						if (!f.isMined(x,y) && f.isMarked(x,y)) {
							field[x][y].setValue(GameSquare.GAMESQUARE_MARKED_WRONGLY);
						} else if (f.isMarked(x,y)) {
							field[x][y].setValue(GameSquare.GAMESQUARE_MARKED);
						} else if (f.isMined(x,y)) {
							field[x][y].setValue(GameSquare.GAMESQUARE_MINED);
						} else {
							field[x][y].setValue(f.getNumberMinedAboutSquare(x,y));
						}
					} else {
						if (f.isMarked(x,y)) {
							field[x][y].setValue(GameSquare.GAMESQUARE_MARKED);
						} else {	
							field[x][y].setValue(GameSquare.GAMESQUARE_UNKNOWN);
						}
					}
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		setVisible(true);
	}

	public void addGameBoardListener(GameBoardListener listener) {
		this.listener = listener;
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		boolean leftButton = false;

		if (e.getModifiers() == MouseEvent.BUTTON1_MASK) leftButton = true;
			
		try {
			if (listener != null) {
				Point p = ((GameSquare) e.getComponent()).location;
				if (leftButton) {
					listener.requestGuess(p);
				} else {
					if (f.isKnown(p.x,p.y)) {
						listener.requestCheck(p);
					} else {
						listener.requestMark(p);
					}
				}
			}
		} catch (NoSuchSquareException ex) {
		}	
	}
	public void mouseReleased(MouseEvent e) {}
}

package com.chembal.minesweeper.ui.gui;

import com.chembal.minesweeper.core.Field;
import com.chembal.minesweeper.core.FieldListener;
import com.chembal.minesweeper.ai.AI;
import com.chembal.minesweeper.ai.LogicalAI;
import com.chembal.minesweeper.ui.GameProperties;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame implements ActionListener, FieldListener, GameBoardListener {

	JPanel   	infoBar			= new JPanel();
	JLabel   	infoMines		= new JLabel("99 Mines");
	JLabel   	infoTime		= new JLabel("000");
	JButton  	infoReset		= new JButton("Reset");
	JButton  	infoZoom		= new JButton("Auto-Play");
	JPanel    	infoButtons		= new JPanel();

	JMenuBar 	menuBar 		= new JMenuBar();
	JMenu	 	menuFile 		= new JMenu("Game");
	JMenu 	 	menuAI 			= new JMenu("AI");
	JMenu 	 	menuAutohelp 	= new JMenu("Auto-Assist");
	JMenu 	 	menuAutoplay 	= new JMenu("Auto-Play");
	JMenu 	 	menuDifficulty	= new JMenu("Difficulty");
	JMenu		menuCustom		= new JMenu("Custom Board");
	JMenu		menuCustomW		= new JMenu("Width");
	JMenu		menuCustomH		= new JMenu("Height");
	JMenu		menuCustomM		= new JMenu("Mine Coverage");
	JMenu		menuHelp		= new JMenu("Help");

	ButtonGroup groupDifficulty = new ButtonGroup();
	ButtonGroup groupAutoHelp 	= new ButtonGroup();
	ButtonGroup groupSpeed 		= new ButtonGroup();
	
	ButtonGroup groupWidth		= new ButtonGroup();
	ButtonGroup groupHeight		= new ButtonGroup();
	ButtonGroup groupMines		= new ButtonGroup();

	JPanel		boardPanel		= new JPanel();
	JPanel		boardContainer	= null;
	GameBoard	board 			= null;
	Field field 			= null;
	LogicalAI 	ai 				= null;
	AutoPlay 	autoPilot		= null;

	GameProperties props		= new GameProperties();
	
	TimerUpdate	timerThread		= null;

	boolean	noUpdateBoard 	= false;
	boolean	autoRunning 	= false;

	public static void main(String[] args) { new GUI(); }

	public GUI() {
		// Set up window
		super("Minesweeper++");
		setJMenuBar(menuBar);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		boardPanel.setLayout(new FlowLayout());

		// Add game menu
		menuBar.add(menuFile);
		menuFile.setMnemonic('G');
		addMenuItemToBar("New","new",menuFile,'N');
		menuFile.addSeparator();
		addMenuItemToBar("Exit","exit",menuFile,'x');

		// Add difficulty menu
		menuBar.add(menuDifficulty);
		menuDifficulty.setMnemonic('D');
			addRadioButtonToBar("Beginner","diffB",groupDifficulty,menuDifficulty,'B',props.getDifficulty() == 'B');
			addRadioButtonToBar("Intermediate","diffI",groupDifficulty,menuDifficulty,'I',props.getDifficulty() == 'I');
			addRadioButtonToBar("Expert","diffE",groupDifficulty,menuDifficulty,'E',props.getDifficulty() != 'B' && props.getDifficulty() != 'I' && props.getDifficulty() != 'U');
			addRadioButtonToBar("Custom","diffU",groupDifficulty,menuDifficulty,'U',props.getDifficulty() == 'U');
			menuDifficulty.addSeparator();
			menuDifficulty.add(menuCustom);
			for (int width = 5; width <= 100; width+= 5) {
				addRadioButtonToBar("" + width, "width", groupWidth, menuCustomW, ' ', width == props.getCustomWidth());
			}
			menuCustom.add(menuCustomW);
			for (int height = 5; height <= 50; height+= 5) {
				addRadioButtonToBar("" + height, "height", groupHeight, menuCustomH, ' ', height == props.getCustomHeight());
			}
			menuCustom.add(menuCustomH);
			for (int coverage = 5; coverage <= 35; coverage++) {
				addRadioButtonToBar("" + coverage + "%", "coverage", groupMines, menuCustomM, ' ', coverage == props.getCustomMines());
			}
			menuCustom.add(menuCustomM);
			menuDifficulty.addSeparator();
			addCheckBoxToBar("Coastal Defense System", "diffC", menuDifficulty, 'C', props.isCds());

		// Add AI menu
		menuBar.add(menuAI);
		menuAI.setMnemonic('A');
		menuAI.add(menuAutohelp);
			addRadioButtonToBar("None","helpNone",groupAutoHelp,menuAutohelp,'N',props.getHelpLevel() <= 0 || props.getHelpLevel() > 3);
			addRadioButtonToBar("Automatically Check Guesses","helpCheck",groupAutoHelp,menuAutohelp,'C',props.getHelpLevel() == 1);
			addRadioButtonToBar("Automatically Make Obvious Moves","helpObvious",groupAutoHelp,menuAutohelp,'O',props.getHelpLevel() == 2);
			addRadioButtonToBar("Automatically Make Most Logical Moves","helpLogical",groupAutoHelp,menuAutohelp,'L',props.getHelpLevel() == 3);

		menuAI.add(menuAutoplay);
			addRadioButtonToBar("Slow","autoSlow",groupSpeed,menuAutoplay,'S',props.getAutoSpeed() == 1);
			addRadioButtonToBar("Medium","autoMedium",groupSpeed,menuAutoplay,'M',props.getAutoSpeed() == 2);
			addRadioButtonToBar("Fast","autoFast",groupSpeed,menuAutoplay,'F',props.getAutoSpeed() == 3 || props.getAutoSpeed() < 1 || props.getAutoSpeed() > 4);
			addRadioButtonToBar("Instant","autoInstant",groupSpeed,menuAutoplay,'I',props.getAutoSpeed() == 4);
			menuAI.addSeparator();
			addMenuItemToBar("Play Continuously","autoContinuous",menuAI,'C');

		menuBar.add(menuHelp);
		addMenuItemToBar("About...","about",menuHelp,'A');
		
		// Set up game board
		initBoard();
		
		boardContainer = new JPanel(true);
		boardContainer.setBorder(new EmptyBorder(0,0,0,0));
		boardContainer.add(board);
		cp.add(boardContainer, BorderLayout.CENTER);

		// Info bar
		infoBar.setLayout(new BorderLayout());
		cp.add(infoBar,BorderLayout.SOUTH);
		infoBar.add(infoMines,BorderLayout.WEST);
		infoReset.setActionCommand("buttonReset");
		infoReset.addActionListener(this);
		infoZoom.setActionCommand("buttonZoom");
		infoZoom.addActionListener(this);
		infoButtons.setLayout(new FlowLayout());
		infoButtons.add(infoReset);
		infoButtons.add(infoZoom);
		infoBar.add(infoButtons);
		infoBar.add(infoTime,BorderLayout.EAST);
		
		timerThread = new TimerUpdate(infoTime,field);

		// Set up event handlers
		board.addGameBoardListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitGracefully();
			}
		});
		
		// Make the window visible
		setResizable(false);
		setLocation(props.getLeft(),props.getTop());
		pack();
		setVisible(true);
		setState(JFrame.NORMAL);
	}

	private void exitGracefully() {
		Point p = getLocation();
		props.setLeft(p.x);
		props.setTop(p.y);
		props.store();
		System.exit(0);
	}

	private void initBoard() {
		switch (props.getDifficulty()) {
			case 'B':
				initBoard(10,10,10,props.isCds());
				break;
			case 'I':
				initBoard(15,15,50,props.isCds());
				break;
			case 'E':
				initBoard(30,16,99,props.isCds());
				break;
			case 'U':
				int width = props.getCustomWidth();
				int height = props.getCustomHeight();
				int mines = (width * height * props.getCustomMines()) / 100;
				initBoard(width, height, mines, props.isCds());
		}
	}
	
	private void initBoard(int width, int height, int mines, boolean cds) {	
		field = new Field(width, height, mines, cds);
		if (timerThread != null) timerThread.setField(field);
		field.addFieldListener(this);
		
		if (ai == null)
			ai = new LogicalAI(field);
		else
			ai.setField(field);

		if (board == null) {
			board = new GameBoard(field);
			setSize(((15 * width) + 15),((15 * height) + 120));
			pack();
		} else {
			board.updateBoard(field);
			setSize(getWidth(),((15 * height) + 120));
			pack();
			validate();
		}
		
		updateMineCount();
	}

	private void addMenuItemToBar(String menuText, String actionCommand, JMenu parent, char accel) { addMenuItemToBar(menuText,actionCommand,parent,accel,true); }	
	private void addMenuItemToBar(String menuText, String actionCommand, JMenu parent, char accel, boolean enabled) {
		JMenuItem menu = new JMenuItem(menuText);
		if (parent == null) {
			menuBar.add(menu);
		} else {
			parent.add(menu);
		}

		menu.setMnemonic(accel);
		menu.setEnabled(enabled);
		menu.setActionCommand(actionCommand);
		menu.addActionListener(this);
	}

	private JCheckBoxMenuItem addCheckBoxToBar(String menuText, String actionCommand, JMenuItem parent, char accel, boolean defaultValue) { return addCheckBoxToBar(menuText,actionCommand,parent,accel,defaultValue,true); }
	private JCheckBoxMenuItem addCheckBoxToBar(String menuText, String actionCommand, JMenuItem parent, char accel, boolean defaultValue, boolean enabled) {
		JCheckBoxMenuItem menu = new JCheckBoxMenuItem(menuText, defaultValue);
		if (parent == null) {
			menuBar.add(menu);
		} else {
			parent.add(menu);
		}
		
		menu.setMnemonic(accel);
		menu.setEnabled(enabled);
		menu.setActionCommand(actionCommand);
		menu.addActionListener(this);
		
		return menu;
	}
	
	private JRadioButtonMenuItem addRadioButtonToBar(String menuText, String actionCommand, ButtonGroup bg, JMenuItem parent, char accel, boolean defaultValue) { return addRadioButtonToBar(menuText,actionCommand,bg,parent,accel,defaultValue,true); }
	private JRadioButtonMenuItem addRadioButtonToBar(String menuText, String actionCommand, ButtonGroup bg, JMenuItem parent, char accel, boolean defaultValue, boolean enabled) {
		JRadioButtonMenuItem menu = new JRadioButtonMenuItem(menuText,defaultValue);
		if (parent == null) {
			menuBar.add(menu);
		} else {
			parent.add(menu);
		}
		
		menu.setMnemonic(accel);
		menu.setEnabled(enabled);
		menu.setActionCommand(actionCommand);
		menu.addActionListener(this);
		bg.add(menu);
		
		return menu;
	}
	
	public void requestGuess(Point p) {
		if (!(field.isWon() || !field.isAlive())) {
			try { if (field.isAlive()) guess(p.x,p.y); } catch (Exception e) {}
			commentOnResult();
		}
	}
	
	public void requestMark(Point p) {
		if (!(field.isWon() || !field.isAlive())) {
			try { 
				if (field.isAlive()) {
					noUpdateBoard = true;
					if (field.isMarked(p.x,p.y)) {
						field.unmark(p.x,p.y);
					} else {
						field.mark(p.x,p.y);
					}
					int helpLevel = props.getHelpLevel();
					if (helpLevel == 1) testAllAssumptions();
					if (helpLevel == 2) ai.doObvious();
					if (helpLevel == 3) { field.setForcedDelay(0); ai.play(true); }
					noUpdateBoard = false;
					boardChanged();
				}
			} catch (Exception e) {}
			commentOnResult();
		}
	}
	
	public void requestCheck(Point p) {
		if (!(field.isWon() || !field.isAlive())) {
			noUpdateBoard = true;
			try { if (field.isAlive()) field.testAssumptions(p.x,p.y); } catch (Exception e) {}
			noUpdateBoard = false;
			boardChanged();
		}
	}
	
	private void guess(int x, int y) {
		try {
			noUpdateBoard = true;
			field.guess(x,y);
			int helpLevel = props.getHelpLevel();
			if (helpLevel == 1) testAllAssumptions();
			if (helpLevel == 2) ai.doObvious();
			if (helpLevel == 3) { field.setForcedDelay(0); ai.play(true); }
			noUpdateBoard = false;
			boardChanged();
		} catch (Exception e) {}
	}
	
	private void testAllAssumptions() {
		boolean changed;
		
		try {
			do {
				changed = false;
				for (int x = 0; x < field.getWidth(); x++) {
					for (int y = 0; y < field.getHeight(); y++) {
						if (field.isKnown(x,y) && !field.isMined(x,y)) {
							if (field.testAssumptions(x,y)) {
								changed = true;
							}
						}
					}
				}
			} while (changed);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (autoRunning) {
			if (command.equals("buttonZoom")) {
				autoPilot.requestStop();
				enableUI();
				JOptionPane.showMessageDialog(this,autoPilot.toString(),"Game Results",JOptionPane.INFORMATION_MESSAGE);
				autoPilot = null;
			}
		} else {
			// Buttons
			if (command.startsWith("button")) {
				switch (command.charAt(6)) {
					case 'R':
						field.resetField();
						boardChanged();
						break;
					case 'Z':
						if (field.isWon() || !field.isAlive()) field.resetField();
						long delay = 0;
						switch (props.getAutoSpeed()) {
							case 4:
								noUpdateBoard = true;
								break;
							case 1:
								delay += 150;
							case 2:
								delay += 150;
							case 3:
						}
						field.setForcedDelay(delay);
						ai.play();
						noUpdateBoard = false;
						boardChanged();
						commentOnResult();
				}
			} else {		
				if (command.equals("exit")) exitGracefully();

				if (command.equals("new")) field.resetField();

				if (command.equals("width")) {
					int newWidth = parseInt(((JMenuItem) e.getSource()).getText());
					if (newWidth > 0) {
						props.setCustomWidth(newWidth);
						if (props.getDifficulty() == 'U') initBoard();
					}
				}
				
				if (command.equals("height")) {
					int newHeight = parseInt(((JMenuItem) e.getSource()).getText());
					if (newHeight > 0) {
						props.setCustomHeight(newHeight);
						if (props.getDifficulty() == 'U') initBoard();
					}
				}
				
				if (command.equals("coverage")) {
					String coverage = ((JMenuItem) e.getSource()).getText();
					int newCoverage = parseInt(coverage.substring(0, coverage.length() - 1));
					if (newCoverage > 0) {
						props.setCustomMines(newCoverage);
						if (props.getDifficulty() == 'U') initBoard();
					}
				}
				
				if (command.startsWith("diff")) {
					switch (command.charAt(4)) {
						case 'U':
						case 'B':
						case 'I':
						case 'E':
							props.setDifficulty(command.charAt(4));
							initBoard();
							break;
						case 'C':
							if (e.getSource() instanceof JCheckBoxMenuItem) {
								JCheckBoxMenuItem menu = (JCheckBoxMenuItem) e.getSource();
								props.setCds(menu.isSelected());
							}
							initBoard();
							break;
					}
				}

				if (command.startsWith("help")) {
					switch (command.charAt(4)) {
						case 'N':
							props.setHelpLevel(0);
							break;
						case 'C':
							props.setHelpLevel(1);
							break;
						case 'O':
							props.setHelpLevel(2);
							break;
						case 'L':
							props.setHelpLevel(3);
							break;				
					}
				}

				if (command.startsWith("auto")) {
					if (command.charAt(4) == 'C') {
						disableUI();
						autoPilot = new AutoPlay(board, field, ai, this);
					} else {
						switch (command.charAt(4)) {
							case 'S':
								props.setAutoSpeed(1);
								break;
							case 'M':
								props.setAutoSpeed(2);
								break;
							case 'F':
								props.setAutoSpeed(3);
								break;
							case 'I':
								props.setAutoSpeed(4);
								break;
						}
						
					}
				}
				
				if (command.equals("about")) {
					JOptionPane.showMessageDialog(this,"Minesweeper++ by Brad Hanken\n\nVersion 1.1 - 06/06/2008" , "About Minesweeper++", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}
	
	private int parseInt(String val) {
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private void disableUI() {
		infoZoom.setText("STOP");
		infoReset.setEnabled(false);
		menuFile.setEnabled(false);
		menuAI.setEnabled(false);
		infoMines.setVisible(false);
		infoTime.setVisible(false);
		autoRunning = true;
		noUpdateBoard = true;
	}
	
	private void enableUI() {
		if (autoPilot != null) { autoPilot.requestStop(); }
		infoZoom.setText("Auto-Play");
		infoReset.setEnabled(true);
		menuFile.setEnabled(true);
		menuAI.setEnabled(true);
		infoMines.setVisible(true);		
		infoTime.setVisible(true);
		autoRunning = false;
		noUpdateBoard = false;
		setTitle("Minesweeper++");
		field.resetField();
	}

	public void boardChanged() {
		if (!noUpdateBoard) {
			board.updateBoard(field);
			boardContainer.paintImmediately(boardContainer.getBounds());
			updateMineCount();
			forceRepaint(infoTime);
		}
	}
	
	private void updateMineCount() {
		int mines = field.getRemainingMines();
		if (field.isWon())
			infoMines.setText("Win!     ");
		else
			infoMines.setText("" + mines + " Mines");
		infoMines.paintImmediately(infoMines.getBounds());
	}
	
	private void forceRepaint(JComponent c) {
		Graphics g = c.getGraphics();
		g.clearRect(0, 0, c.getWidth(), c.getHeight());
		c.paint(g);
		c.paintAll(g);
	}

	private void commentOnResult() {
		if (field.isWon()) {
			JOptionPane.showMessageDialog(this,"We won!  Yay!","Game Result",JOptionPane.INFORMATION_MESSAGE);
		} else if (!field.isAlive()) {
			JOptionPane.showMessageDialog(this,"We lost.  Darn.","Game Result",JOptionPane.INFORMATION_MESSAGE);			
		}
	}
	
	class AutoPlay extends Thread {
		private GameBoard board;
		private Field field;
		private AI ai;
		private JFrame frame;
		
		private boolean running = false;
		private boolean stopRequest = false;
		
		private long played = 0;
		private long won = 0;
		
		private DecimalFormat df = new DecimalFormat();
		
		public AutoPlay (GameBoard board, Field field, AI ai, JFrame frame) {
			this.board = board;
			this.field = field;
			this.ai = ai;
			this.frame = frame;
			df.applyPattern("00.00");
			start();
		}
		
		public void requestStop() {
			stopRequest = true;
			while (running) Thread.yield();
			updateStats();
		}
		
		public void run() {
			running = true;
			field.setForcedDelay(0);
			while (!stopRequest) {
				// Play game!
				if (playGame()) won++;
				played++;
				
				// Update stats.
				if ((played % 25) == 0) updateStats();
			}
			running = false;
		}
		
		private boolean playGame() {
			field.resetField();
			ai.play();
			return field.isWon();
		}
		
		private void updateStats() {
			double perc = (double) won / (double) played * 100.0d;
			frame.setTitle("Auto-play: won " + won + " out of " + played + " games. [" + df.format(perc) + "%]");
			board.updateBoard(field);
			board.paintImmediately(board.getBounds());
		}
		
		public String toString() {
			double perc = (double) won / (double) played * 100.0d;
			return "Won " + won + " out of " + played + " games. [" + df.format(perc) + "%]";
		}

		public long getPlayed() { return played; }
		public long getWon() { return won; }

	}
	
	class TimerUpdate extends Thread {
		private JLabel timeLabel;
		private Field field;
		private long time;
		private DecimalFormat df;
		
		private static final String TIME_PREFIX = "Time: ";
		
		public TimerUpdate (JLabel timeLabel, Field field) {
			this.timeLabel = timeLabel;
			this.field = field;
			start();
		}
		
		public void run() {
			time = System.currentTimeMillis();
			timeLabel.setText("Time: 000");
			df = new DecimalFormat();
			df.applyPattern("000");
			while (true) update();
		}

		private void update() {
			try {
				if (field.isStarted()) {
					timeLabel.setText(TIME_PREFIX + df.format((System.currentTimeMillis() - time) / 1000));
					Thread.sleep(100);
				} else {
					time = System.currentTimeMillis();
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public Field getField() {
			return field;
		}

		public void setField(Field field) {
			this.field = field;
		}
	}
}

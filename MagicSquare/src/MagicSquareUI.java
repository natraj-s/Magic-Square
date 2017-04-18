/**
 * MagicSquareUI.java
 * 
 * @author Natraj
 * @version June 6th, 2010
 * 
 * 
 */

/* Include preprocessor directories */
import java.io.*;
import java.util.Scanner;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

public class MagicSquareUI implements ActionListener, MouseListener{
	
	// used for storing the dimensions of the board
	public static int SIZE;
	// an instance of the MagicSquare class
	public MagicSquare newSq;
	// the canvas on which the UI will be drawn
	public JFrame canvas;
	// a 2D array of Tile class instances
	private Tile board[][];
	// an array of Tile instances used for denoting the rowTotals on the right side
	private Tile rowTotal[];
	// an array of Tile instances used for denoting the colTotals on the top
	private Tile colTotal[];
	// an array of Tile instances used for denoting  the diagTotal buttons 
	private Tile diagTotal[];
	// the start, hint and quit buttons
	private JButton startButton, hintButton, quitButton;
	// the timer
	private JLabel timerLabel;
	// the seconds value of the timer
	private Integer secs = 0;
	// the minutes value of the timer
	private Integer mins = 0;
	private boolean gameStart = false;
	private boolean bestTimeDisplay = false;
	private Timer timedown;
	// to maintain the format of an actual digital clock
	private DecimalFormat clockFormat = new DecimalFormat("00");
	private Random randomNumberGen = new Random();
	// a writer instance to write to character streams
	private Writer output = null;
	// to write the character streams to a new file.
	private FileWriter scoreFile;
	
	// the main() class for MagicSquareUI
	public static void main(String[] args) throws IOException {
		
		MagicSquareUI newUI = new MagicSquareUI();
	}
	
	/* The MagicSquareUI constructor
	 *  Functions:
	 *  	- Set up the canvas and the look and feel of the UI
	 *  	- Get the size input from the user for the dimensions of the magic square
	 *  	- Create a completed magic square for reference purposes.
	 *  	- Build a grid of SIZE x SIZE Tiles and display it to the user 
	 */
	public MagicSquareUI() throws IOException
	{
		setLookAndFeel();		
		getSize();
		
		newSq = new MagicSquare(SIZE);
		buildSquare();		
	}
	
	/* actionPerformed(ActionEvent e)
	 * Functions:
	 * 	- If source of action is the start button, start the game, and reset all the required values.
	 * 	- If source of action is a hint button, then display a random "correct answer" on the board
	 * 	- If source of action is quit button, then display the solution to the magic square
	 * 	- If the source is the Timer instance which is triggered every 1 second, then update the 
	 * 		timer label on the board by incrementing the seconds or the minutes value accordingly.
	 * 	- Else means that the user has entered a value into one of the Tiles and pressed return. 
	 * 		In which case, the board updates its row, column and diagonal total values
	 * 		and then checks if the game is complete by comparing it with the the solution.  		
	 */
	
	public void actionPerformed(ActionEvent e) {
		/* If start button was clicked */
		if(e.getSource() == startButton)
		{
			enableDisableBoard(0);
			resetValues();
		}
		/* If hint button was clicked */
		else if(e.getSource() == hintButton)
		{
			if(gameStart == true)
			{
				// Display the hint only if the user has already started the game
				showHint();
				
				// If checkForFinish() returns true, that is the current state of the board
				// matches with the solution state of the board.
				if(checkForFinish() == true)
				{
					// Stop the timer
					timedown.stop();
					// Write the current time to the appropriate file
					try {
						storeTime();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// Display to the user via a dialog box what the user's best time on this
					// grid was. 
					// bestTimeDisplay is needed so that the dialog box doesn't show up twice
					// once when the user enters a value and hits return, and then again when the 
					// user clicks somewhere on a tile. 
					if(bestTimeDisplay == false)
					{
						try {
							getBestTime();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					// Pass a value of 1 to enableDisableBoard(int) which signifies that the 
					// user mostly if not completely beat the game by himself and did not quit. 
					enableDisableBoard(1);
				}
			}
		}
		/* If the quit button is pressed */
		else if(e.getSource() == quitButton)
		{
			// Only show the solution if the user has already started playing the game 
			if(gameStart == true)
			{
				// Stop the timer and don't store the time
				timedown.stop();
				// Passing a value of 2 to enableDisableBoard() signifies that the user 
				// quit the game.
				enableDisableBoard(2);
			}
		}
		/* If its the timer that triggered the actionEvent */
		else if(e.getSource() == timedown)
		{
			// Increment the seconds
			secs++;
			// If the seconds value equals 60, then increment the minutes counter
			// and reset seconds back to 0.
			if(secs == 60)
			{
				mins++;
				secs = 0;
			}
			// Set the timerLabel in the appropriate decimalFormat
			timerLabel.setText(clockFormat.format(mins) + " : " + clockFormat.format(secs));
			
		}
		/* If its none of the above that triggered the action, then it must
		 * be triggered by the user pressing return on a tile	 */
		else
		{
			/* The rest of this function acts the same as in the case of the hint button pressed
			 * except there is no hint displayed in this case.
			 */
			updateTotals();
			if(checkForFinish() == true)
			{
				timedown.stop();
				try {
					storeTime();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(bestTimeDisplay == false)
				{
					try {
						getBestTime();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				enableDisableBoard(1);
			}
		}		
	}
	
	/* buildSquare()
	 * Functions:
	 * 	- Set up the respective parts of the board i.e. the info, the different buttons and the timer. 
	 * 	- Form the SIZE x SIZE grid of Tiles and also add Tiles displaying the row, column and diagonal
	 * 		totals. 
	 */
	public void buildSquare()
	{
		long clock;
		// the sumLabel displays the info and the directions on how to play the game on the top
		// of the board
		JTextPane sumLabel;
		// Set up the different aspects of the canvas
		setupCanvas();
		// Initialize the timer instance to trigger every 1000 milliseconds i.e. 1 second. 
		timedown = new Timer(1000, this);
		
		// theStyle maintains attributes for regular font and alignment attributes
		SimpleAttributeSet theStyle = new SimpleAttributeSet();
		// theStyle2 simply makes the font bold.
		SimpleAttributeSet theStyle2 = new SimpleAttributeSet();
		String intro;
		// Center alignment for the text
		StyleConstants.setAlignment(theStyle, StyleConstants.ALIGN_CENTER);
		// Font family for the text
		StyleConstants.setFontFamily(theStyle, "Helvetica");
		// Font size for the text
		StyleConstants.setFontSize(theStyle, 11);
		// font displayed under theStyle2 set of attributes makes it bold
		StyleConstants.setBold(theStyle2, true);
		
		// infoPane contains the text pane displaying the info and directions 
		// and also the timerPanel which basically contains all the buttons such
		// as start, hint and quit, alongwith the timer label. 
		JPanel infoPanel = new JPanel();
		infoPanel.setPreferredSize(new Dimension(100, 150));
		infoPanel.setLayout(new GridLayout(2,0,0, 0));
		
		// the intro msg which is added to the JTextPane.
		intro = "Click on START to begin the game. You will be timed.\n" +
		"Clicking on HINT will show you one correct answer on your grid. It will be highlighted in white.\n" +
		"The sum of each row, column and diagonal of your magic square is going to be: " + newSq.getSum() + "\n"
		+ "HAVE FUN!";
		
		sumLabel = new JTextPane();
		sumLabel.setText(intro);
		// set the background color to match the rest of the canvas. 
		sumLabel.setBackground(new Color(230, 230, 230));
		// foreground color i.e. text color is black
		sumLabel.setForeground(Color.black);
		// Document that can contain character and paragraph attributes in a similar fashion
		// to the Rich Text Format. The style attributes however are logical and break at paragraph
		// boundaries. 
		StyledDocument sumDoc = sumLabel.getStyledDocument();
		// setParagraphAttributes(offset, length, AttributeSet, replace)
		sumDoc.setParagraphAttributes(0, sumDoc.getLength(), theStyle, false);
		// setCharacterAttributes(offset, length, AttributeSet, replace)
		sumDoc.setCharacterAttributes(intro.indexOf("START"), 5, theStyle2, false);
		sumDoc.setCharacterAttributes(intro.indexOf("HINT"), 4, theStyle2, false);
		// The text pane should not be editable
		sumLabel.setEditable(false);
		// Add the text pane to the infoPanel
		infoPanel.add(sumLabel);	
		
		// The timer panel contains the start button, the hint button and the timer label
		JPanel timerPanel = new JPanel();
		timerPanel.setPreferredSize(new Dimension(100, 100));
		// The buttons and the label are laid out in a horizontal fashion. 
		timerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		
		/* Set the text, attribute and action listeners for each button
		 *  The timer label does not have an actionListener however.
		 */
		startButton = new JButton("START");
		startButton.setFont(new Font("Helvetica", Font.BOLD, 11));
		startButton.addActionListener(this);
		timerPanel.add(startButton);
		hintButton = new JButton("HINT");
		hintButton.setFont(new Font("Helvetica", Font.BOLD, 11));
		hintButton.addActionListener(this);
		timerPanel.add(hintButton);
		quitButton = new JButton("QUIT");
		quitButton.setFont(new Font("Helvetica", Font.BOLD, 11));
		quitButton.addActionListener(this);
		timerPanel.add(quitButton);
		timerLabel = new JLabel("00 : 00");
		timerLabel.setFont(new Font("Helvetica", Font.BOLD, 16));
		timerPanel.add(timerLabel);
		
		// Add a border to this panel.
		timerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), 
				BorderFactory.createEmptyBorder(10, 5, 10, 5)));
		
		// Add the timerPanel to the infoPanel
		infoPanel.add(timerPanel);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
		infoPanel.setVisible(true);
		canvas.getContentPane().add(infoPanel, BorderLayout.NORTH);
		
		// The board panel contains the grid of playing Tiles as well as two extra
		// rows for displaying the different diagonal totals as well as the total
		// value in each column and finally row totals at the end of every row. 
		JPanel boardPanel = new JPanel();
		boardPanel.setSize(600, 450);
		boardPanel.setLayout(new GridLayout(SIZE + 2, SIZE + 1, 3, 3));
		// The array of rowTotal Tiles at the end of each row
		rowTotal = new Tile[SIZE];
		// The array of colTotal Tiles on the top
		colTotal = new Tile[SIZE];
		// The array of diagTotal Tiles 
		diagTotal = new Tile[2];
		
		// Initialize the board of SIZE x SIZE tiles. 
		board = new Tile[SIZE][SIZE];
		clock = System.currentTimeMillis();
		
		// Draw the colTotal tiles first and set the appropriate foreground
		// and background colors, as well as the text and add it to the boardPanel
		for(int i = 0; i < SIZE; i++)
		{
			colTotal[i] = new Tile(1);
			colTotal[i].setProperties(Color.black, Color.gray, false, false);
			colTotal[i].setText("Col " + i + " Total");
			boardPanel.add(colTotal[i]);
		}
		// Draw the first diagTotal tile at the end of the first row
		diagTotal[0] = new Tile(1);
		diagTotal[0].setProperties(Color.black, Color.gray, false, false);
		diagTotal[0].setText("Diag 1 Total");
		boardPanel.add(diagTotal[0]);
		
		// Draw the board in two loops
		for(int i = 0; i < SIZE; i++)
		{
			for(int j = 0; j < SIZE; j++)
			{
				board[i][j] = new Tile(1);
				board[i][j].setRow(i);
				board[i][j].setCol(j);
				board[i][j].setProperties(Color.black, Color.GRAY, false, false);
				board[i][j].setText(".");
				boardPanel.add(board[i][j]);				
			}
			
			// At the end of each row, add a rowTotal Tile.
			rowTotal[i] = new Tile(1);
			rowTotal[i].setProperties(Color.black, Color.gray, false, false);
			rowTotal[i].setText("Row " + i + " Total");
			boardPanel.add(rowTotal[i]);
		}
		
		for(int i = 0; i < SIZE; i++)
		{
			JTextField temp = new JTextField();
			temp.setVisible(false);
			boardPanel.add(temp);
		}
		// Add the second diagTotal tile.
		diagTotal[1] = new Tile(1);
		diagTotal[1].setProperties(Color.black, Color.gray, false, false);
		diagTotal[1].setText("Diag 2 Total");
		boardPanel.add(diagTotal[1]);
		
		System.out.println((System.currentTimeMillis() - clock)/ 1000.0);
		// Set a compound border consisting of an outer border with text, and an
		// empty inner border
		boardPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Welcome to the Magic Square Game"),
																	BorderFactory.createEmptyBorder(10, 5, 10, 5)));
		
		// add this board to center position of the layout
		canvas.getContentPane().add(boardPanel, BorderLayout.CENTER);
		canvas.setVisible(true);
	}
	
	/* checkForFinish()
	 * Functions:
	 * 	- Traverse the current board in row-major order and compare
	 * 		the value of each tile against that of the value in the tile at
	 * 		the same position in the solution. 		
	 */
	public boolean checkForFinish()
	{
		for(int i = 0; i < SIZE; i++)
		{
			for(int j = 0; j < SIZE; j++)
			{
				/* If any one of these tiles have values that do not match up
				 * with the value at the same tile in the solution, then stop
				 * traversing the board further and return a boolean value of
				 * false meaning the board is not complete. 
				 */
				if(board[i][j].getText().equals(""))
				{
					return false;
				}
				else if(Integer.parseInt(board[i][j].getText()) != newSq.getNumber(i, j))
				{
					return false;
				}
			}
		}
		
		/* If the traversal actually makes it without returning
		 * any boolean value, then it means that all the values 
		 * are the same as the solution, therefore return boolean
		 * true.
		 */
		return true;
	}
	
	/* enableDisableBoard(int value)
	 *	 Functions:
	 *		- Based on value passed to it, 0, 1 or 2, either enable the board or disable it.
	 *		- 0 means the user has clicked the start button, so enable the board and change
	 *			the background and foreground colors of the board
	 * 	- 1 means the user has beat the game, and hence disable all the tiles on the board.
	 * 	- 2 means the user decided to quit, therefore disable the board change the background
	 * 		color of all tiles to reflect this.
	 */
	public void enableDisableBoard(int value)
	{
		Integer theNumber;
		for(int i = 0; i < SIZE; i++)
		{
			for(int j = 0; j < SIZE; j++)
			{
				// enable Board, change foreground color and background color
				if(value == 0)
				{
					board[i][j].setProperties(Color.DARK_GRAY, Color.LIGHT_GRAY, true, true);
					board[i][j].setText("");					
					board[i][j].addActionListener(this);
					board[i][j].addMouseListener(this);
				}
				// disable board and change background color
				else if(value == 1)
				{
					board[i][j].setBackground(Color.GRAY);
					board[i][j].setEnabled(false);
					board[i][j].setEditable(false);
					gameStart = false;
				}
				// disable board and change background color
				else
				{
					board[i][j].setBackground(Color.black);
					theNumber = newSq.getNumber(i, j); 
					board[i][j].setText(theNumber.toString());
					board[i][j].setEnabled(false);
					board[i][j].setEditable(false);
					gameStart = false;
				}
				
			}
			// Only  change the text on the rowTotal and colTotal columns
			// if the board was to be enabled. 
			if(value == 0)
			{
				rowTotal[i].setText("0");
				colTotal[i].setText("0");
			}
		}
		// If the user decided to quit, then updateTotals is called from here
		if(value > 1)
		{
			updateTotals();
		}
	}
	
	/* getBestTime()
	 * Functions:
	 * 	- Read in times stored in file name SIZExSIZE.ms
	 * 	- Add them to a temporary array list and sort them in increasing order
	 * 	- Get the fastest time, i.e. the value at the top of the array list and display
	 * 		it to the user.
	 */
	public void getBestTime() throws FileNotFoundException
	{
		ArrayList<Integer> times = new ArrayList<Integer>();
		Integer fastestMin, fastestSec;
		fastestMin = fastestSec = 0;
		// File represents a system independent view of hierarchical pathnames
		// Read in the file stored in the same directory. There is never a case that 
		// the FileNotFound Exception will be thrown because the file is created 
		// in the storeBestTime() function which is called before the call is made
		// to this method. 
		File readInFile = new File(SIZE + "x" + SIZE + ".ms");
		Scanner readIn = new Scanner(readInFile);
		// while there is a next line in the file, read it, parse it to an integer value
		// and add it to the array list.
		while(readIn.hasNext())
		{
			try {
			times.add(Integer.parseInt(readIn.nextLine()));
			} catch(NumberFormatException nf1){ }
		}
		// Sort it in increasing order
		Collections.sort(times);
		// Adjust the appropriate parameters to display the time finally in true
		// digital clock fashion
		if(times.get(0) > 60)
		{
			fastestMin = (times.get(0)/60);
			fastestSec = (times.get(0))%60;
		}
		else
		{
			fastestMin = 0;
			fastestSec = times.get(0);
		}
		// Open a dialog box and show the fastest time
		JOptionPane.showMessageDialog(null, "  Your fastest time is " + clockFormat.format(fastestMin) + " : " + clockFormat.format(fastestSec), "Your fastest time is", JOptionPane.INFORMATION_MESSAGE);
		// Set the boolean flag to true so that the dialog box isn't displayed twice
		// accidentally
		bestTimeDisplay = true;
		
	}
	
	/* getSize()
	 * Functions:
	 * 	- Open a dialog box printing basic info such as version info and author name
	 * 	- Take input on the dimensions of the magic square that the user wants to play
	 * 		on. 
	 */
	public void getSize()
	{
		// success determines if the input was valid and if it was, then it goes on
		// to build the board.
		boolean success = false;
		// validInput is used to determine if the input from the user was a parsable
		// integer and not random garbage values. 
		boolean validInput = true;
		
		while(success == false)
		{
			// create a panel which will contain the string and add this panel to 
			// a dialog box
			JPanel pane = new JPanel();
			pane.setLayout(new GridLayout(8,0));		
			String intro = "";
			JLabel introMsg;
			// The Intro msg.
			intro = "     Welcome to the Magic Square Game! June 1, 2010\n	";
			introMsg = new JLabel(intro);
			introMsg.setFont(new Font("Verdana", Font.PLAIN, 11));
			pane.add(introMsg);
			intro = "                            Final version for now	    \n";
			introMsg = new JLabel(intro);
			introMsg.setFont(new Font("Verdana", Font.PLAIN, 11));
			pane.add(introMsg);
			intro = "                       Author: Natraj Subramanian	\n\n";
			introMsg = new JLabel(intro);
			introMsg.setFont(new Font("Verdana", Font.PLAIN, 11));
			pane.add(introMsg);
			introMsg = new JLabel("");
			pane.add(introMsg);
			
			intro = "  \nPlease enter the size of the magic square you want to play. ";
			introMsg = new JLabel(intro);
			introMsg.setFont(new Font("Verdana", Font.PLAIN, 11));
			pane.add(introMsg);
			intro = "  \n                         (  ODD NUMBERS ONLY!  )   ";
			introMsg = new JLabel(intro);
			introMsg.setFont(new Font("Verdana", Font.PLAIN, 11));
			pane.add(introMsg);
			introMsg = new JLabel("");
			pane.add(introMsg);
			
			// the input text field
			JTextField input = new JTextField(10);
			input.setHorizontalAlignment(JTextField.CENTER);
			pane.add(input);
			
			// display the dialog box
			JOptionPane.showMessageDialog(null, pane, "Welcome to the Magic Square Builder", JOptionPane.INFORMATION_MESSAGE);	
			
			// If the input from the user is not parsable, then don't build the board
			// and ask the user again for valid input
			try {
				SIZE = Integer.parseInt((input.getText()));
			} catch(NumberFormatException nf1)
			{
				validInput = false;
				success = false;
			}
			
			// For now, the game is restricted to odd numbered dimensions only
			// Therefore do not attempt to build the board with an even numbered
			// dimension
			if(SIZE%2 == 0)
			{
				JOptionPane.showMessageDialog(null, "Please enter an odd number <= 25", "Odd numbers only!", JOptionPane.ERROR_MESSAGE);
				success = false;
				
			}
			else if(SIZE % 2	!= 0)
			{
				success = true;
			}
		}
		
	}
	
	/*resetValues()
	 * Functions:
	 * 	- reset values that need to be treated as fresh values at the beginning of each new
	 * 		game 
	 */
	public void resetValues()
	{
		// Boolean used to keep track that user has started the game
		gameStart = true;
		// Integer used to keep track of minutes elapsed 
		mins = 0;
		// Integer used to keep track of seconds elapsed
		secs = 0;
		// Reset the timer label so it reads 00 : 00
		timerLabel.setText(clockFormat.format(mins) + " : " + clockFormat.format(secs));
		// Boolean to keep track that the best time has been only displayed once
		bestTimeDisplay = false;
		// Since the timer is stopped when the game is done, or when the user quit, restart the
		// timer.
		timedown.start();
	}
	
	/* setLookAndFeel()
	 * Functions:
	 * 	- Set the look and feel of the Java swing interface.
	 * 	- In this case, it attempts to get the look and feel of the system at 
	 * 		the point of execution and makes the UI look the same. 
	 */
	public void setLookAndFeel()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* setupCanvas()
	 * Functions:
	 * 	- Set up the canvas properties 
	 */
	public void setupCanvas()
	{
		canvas = new JFrame();
		// The title of the window
		canvas.setTitle("Welcome to the Magic Square Game");

		// Default size for all grids of dimension < 19
		if(SIZE < 19)
		{
			canvas.setSize(600, 700);
		}
		// To help the user play better, the maximum size of a magic square that the player
		// can build is 25. Any squares > 25 get reset to 25 since it becomes illegible to play
		// anymore if the square is > 25. Also resizes the canvas to giver a broader playing board.
		else if(SIZE > 25)
		{
			SIZE = 25;
			canvas.setSize(1024, 780);
		}
		else
		{
			canvas.setSize(1024, 780);
		}
			
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		canvas.setResizable(false);
		canvas.setLayout(new BorderLayout());
	}
	
	/* showHint()
	 * Functions:
	 * 	- Generate a random row number and column number
	 * 	- Get the value at this row and column from the solution square
	 * 	- Set the value at this row and column on the board to the correct value
	 *		- So that the user doesn't change it again, disable this tile 
	 */
	public void showHint()
	{
		int randRow, randCol;
		Integer value;
		// Random is in fact pseudo random. So if two instances of Random are
		// created with the same seed, and the same sequence of method calls
		// are made for each, then the sequence of numbers returned will be 
		// identical for both.
		randRow = randomNumberGen.nextInt(SIZE);
		randCol = randomNumberGen.nextInt(SIZE);
		value = newSq.getNumber(randRow, randCol);
		// To differentiate it from user input values, set the foreground of this tile
		// to a different color
		board[randRow][randCol].setForeground(Color.white);
		board[randRow][randCol].setText(value.toString());
		// Do not allow the user to edit this tile.
		board[randRow][randCol].setEditable(false);
		updateTotals();
	}
	
	/* storeTime()
	 * Functions:
	 * 	- Create a new file with the name "SIZExSIZE.ms", if one doesn't
	 * 		already exist, where SIZE is simply the dimension of the square 
	 * 		the user was playing and convert the value in the timer label to
	 * 		its equivalent seconds value. Append this value to the file. 
	 */
	public void storeTime() throws IOException
	{
		Integer time;
		// Create a new File if the file doesn't already exist.
		scoreFile = new FileWriter(SIZE + "x" + SIZE + ".ms", true);
		// a new instance of BufferedWriter which writes text
		// to a character output stream
		output = new BufferedWriter(scoreFile);
		// convert to equivalent seconds value
		time = (mins * 60) + secs;
		// append this value to the file
		output.append(time.toString());
		// add a new line after each append
		((BufferedWriter)output).newLine();
		output.close();
	}
	
	/* updateTotals()
	 * Functions:
	 * 	- Update totals on each row, column and diagonal
	 * 	- Set the appropriate tiles to reflect these totals. 
	 */
	public void updateTotals()
	{
		// rTotal = row Total
		// cTotal = column Total
		// d1Total = diagonal 1 total (bottom left to top right)
		// d2Total = diagonal 2 total (top left to bottom right)
		// hValue = horizontal value (goes in row major order)
		// vValue = vertical value (goes in column major order)
		// r1 = Decremented before every increment of i. Used for diagonal 1 total
		// r2 = Incremented before every increment of i. Used for diagonal 2 total
		Integer rTotal, cTotal, d1Total, d2Total, hValue, vValue, r1, r2;
		hValue = vValue  =  rTotal = cTotal = d1Total = d2Total = r2 = 0;
		
		// Since diagonal 1 goes from bottom left to top right, r1 starts at
		// the SIZE - 1 row and 0 column
		r1 = SIZE - 1;
		
		for(int i = 0; i < SIZE; i++)
		{
			for(int j = 0; j < SIZE; j++)
			{
				// hValue gets the value from the tile at board[i][j]
				// as the loop traverses in row major order i.e. j traverses
				// through all the columns first and then the row at i is incremented.
				
				// If the tile is blank
				if(board[i][j].getText().equals("")) {
					hValue = 0;
				}
				// else, try to parse the value to an integer
				else	{
					hValue = Integer.parseInt(board[i][j].getText());
				}
				
				// vValue gets the value from the tile at board[j][i]
				// meaning it traverses the board in column major order.
				// j traverses through all the rows first, and then the column
				// at i is incremented
				
				// If the tile is blank
				if(board[j][i].getText().equals("")) {
					vValue = 0;
				}
				// else try to parse the value to an integer
				else	{
					vValue = Integer.parseInt(board[j][i].getText());
				}
				
				// Update rowTotal for this row at the i value
				rTotal = rTotal  + hValue;
				// Update colTotal for this column at the i value
				cTotal = cTotal + vValue;	
				
				// r2 is incremented after every completion of the j loop
				// Diagonal 2 is the diagonal extending from the top left
				// to the bottom right. Hence it takes values from board[0][0],
				// board[1][1] i.e. basically when the row and column have the
				// values. Hence, this checks if the j value is equal to the r2 value
				// and if it is, add the value at this tile to d2Total.
				if(j == r2)	{
					d2Total = d2Total + hValue;
				}				
			} // end for(j)
			
			// r1 starts at SIZE - 1 row and is decremented with every completion
			// the j loop. r2 is incremented. Diagonal 1 is that which extends from
			// the bottom left to the top right. For a board of size 5, it would take values
			// from board[4][0], board[3][1] i.e. row and column are inversely proportional. 
			
			// If the tile at board[r1][r2] is not blank, then add the value of the tile
			// to d1Total, else add 0. 
			if(!(board[r1][r2].getText().equals("")))
			{
				d1Total = d1Total + Integer.parseInt(board[r1][r2].getText());
			}
			else
			{
				d1Total = d1Total + 0;
			}
			
			// Update the total of each row, column and diagonal tile
			rowTotal[i].setText(rTotal.toString());
			colTotal[i].setText(cTotal.toString());
			diagTotal[0].setText(d1Total.toString());
			diagTotal[1].setText(d2Total.toString());
			// reset row and column totals after each j loop. 
			rTotal = cTotal = 0;
			r1--;
			r2++;
			
		} // end for(i) loop
		
	}

	/* mouseClicked(MouseEvent e)
	 *  Functions:
	 *  	- Basically do everything that the program does for a click of return.
	 *  	- Update the totals, check if game has been beat.
	 *  	- If game has been beat, update the totals, store the time.
	 *  	- If best time hasn't been displayed already, then show it. 
	 *  	- Disable the board. 
	 */
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(gameStart == true)
		{
			updateTotals();
			if(checkForFinish() == true)
			{
				timedown.stop();
				try {
					storeTime();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(bestTimeDisplay == false)
				{
					try {
						getBestTime();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				enableDisableBoard(1);				
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}

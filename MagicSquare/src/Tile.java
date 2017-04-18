/**
 * Tile.java
 * 
 * A modified and more specialized version of JTextField for
 * the Magic Square game.
 * 
 * @author Natraj
 * @version June 6th, 2010
 * 
 */
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

import java.awt.*;


public class Tile extends JTextField {
	
	public JTextField input;
	private int row;
	private int col;
	
	public Tile(int textfieldSize)
	{
		input = new JTextField(textfieldSize);
	}
	
	/**
	 * 
	 *	setProperties
	 * @param fgColor
	 * @param bgColor
	 * @param isEnabled
	 * @param isEditable
	 */
	public void setProperties(Color fgColor, Color bgColor, boolean isEnabled, boolean isEditable)
	{
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY));
		this.setForeground(fgColor);
		this.setBackground(bgColor);
		this.setHorizontalAlignment(JTextField.CENTER);
		this.setFont(new Font("Helvetica", Font.BOLD, 11));
		this.setEnabled(isEnabled);
		this.setEditable(isEditable);
	}
	
	/**
	 * 
	 *	setRow
	 * @param value
	 */
	public void setRow(int value)
	{
		row = value;
	}
	
	/**
	 * 
	 *	getRow
	 * @return
	 */
	public int getRow()
	{
		return row;
	}
	
	/**
	 * 
	 *	setCol
	 * @param value
	 */
	public void setCol(int value)
	{
		col = value;
	}
	
	/**
	 * 
	 *	getCol
	 * @return
	 */
	public int getCol()
	{
		return col;
	}

}

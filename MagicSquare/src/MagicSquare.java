/**
 * MagicSquare.java
 * 
 * A preliminary magic square generator for odd numbered dimensions.
 * 
 * @author Natraj
 * @version May 31, 2010
 * 
 */

import java.io.*;
import java.util.*;


public class MagicSquare {
	
	public static int SIZE;
	public static long clock;	
	
	public int magicSquare[][];
	public int timeExec;
	
	public MagicSquare(int theSize)
	{		
		SIZE = theSize;
		magicSquare = new int[SIZE][SIZE];
		
		makeSquare();
		
	}
	
	public void initSquare()
	{		
		for(int i = 0; i < SIZE; i++)
		{
			for(int j = 0; j < SIZE; j++)
			{
				magicSquare[i][j] =0;
			}			
		}
	}
	
	public void makeSquare()
	{
		int fillCounter = 0;
		int theNumber = 1;
		
		int startRow = 0;
		int startCol = SIZE/2;
		magicSquare[startRow][startCol] = theNumber;
		fillCounter++;
		
		while(fillCounter != (SIZE * SIZE))
		{
			if(startRow == 0)
			{
				startRow = SIZE - 1;
			}
			else
			{
				startRow = startRow - 1;
			}
			
			if(magicSquare[startRow][(startCol + 1) % SIZE] != 0)
			{
				theNumber++;
				startRow = (startRow + 1) % SIZE;
				magicSquare[(startRow + 1) % SIZE][startCol] = theNumber;
				startRow = (startRow + 1) % SIZE;
			}
			else
			{
				theNumber++;		
				startCol = (startCol + 1) % SIZE;
				magicSquare[startRow][startCol] = theNumber;
			}
			
			fillCounter++;
		}
		
	}
	
	public int getSum()
	{
		return (SIZE * ((SIZE*SIZE) + 1))/2;
	}
	
	public void printTotals()
	{
		int rowTotal = 0;
		int colTotal = 0;
		int diagTotal = 0;
		
		String temp = " ";
		Integer value = 0;
		int rowNum = 0;
		int colNum = 0;
		
		for(int k = 0; k < 2; k++)
		{	
			for(int i = 0; i < SIZE; i++)
			{
				for(int j = 0; j < SIZE; j++)
				{
					if(k == 0)	{
						rowTotal = rowTotal + magicSquare[i][j];
						value = magicSquare[i][j];
						temp = temp + value.toString();
					}
					else	{
						colTotal = colTotal + magicSquare[j][i];
						value = magicSquare[j][i];
						temp = temp + value.toString();
					}
					
					if(j != SIZE - 1)
					{
						temp = temp + " + ";
					}
				}
				
				/*if(k == 0)	{
					System.out.println("Row " + i + " Total:     " + temp + " = " + rowTotal);
				}
				else	{
					System.out.println("Col " + i + " Total:     " + temp + " = " + colTotal);
				}*/
				
				temp = "";
				rowTotal = 0;
				colTotal = 0;
			}		
		}
		
		while(rowNum < SIZE)
		{
			diagTotal = diagTotal + magicSquare[rowNum][colNum];
			value = magicSquare[rowNum][colNum];
			temp = temp + value.toString();
			if(rowNum != SIZE - 1){
				temp = temp + " + ";
			}
			
			rowNum++;
			colNum++;
		}
		
		//System.out.println("Diagonal 1 Total:     " + temp + " = " + diagTotal);
		temp = "";		
		rowNum = SIZE - 1;
		colNum = 0;
		diagTotal = 0;
		
		while(rowNum >= 0)
		{
			diagTotal = diagTotal + magicSquare[rowNum][colNum];
			value = magicSquare[rowNum][colNum];
			temp = temp + value.toString();
			if(rowNum != 0)	{
				temp = temp + " + ";
			}
			
			rowNum--;
			colNum++;
		}
		
		//System.out.println("Diagonal 2 Total:     " + temp + " = " + diagTotal);
		temp = "";
		
	}
	
	public int getNumber(int row, int col)
	{
		return magicSquare[row][col];
	}
	

}

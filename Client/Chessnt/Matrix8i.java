package Chessnt;

public class Matrix8i
{
	private int[][] matrix = new int[8][8];
	
	public Matrix8i()
	{
		for(int i = 0; i < 8; i++)
		{
			if(i == 0 || i == 1)
			{
				matrix[i] = new int[]{1, 1, 1, 1, 1, 1, 1, 1};
			}
			else if(i == 6 || i == 7)
			{
				matrix[i] = new int[]{2, 2, 2, 2, 2, 2, 2, 2};
			}
			else
			{
				matrix[i] = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			}
		}
	}
	
	public int[][] getMatrix()
	{
		return matrix;
	}
	public int[] getRow(int i)
	{
		return matrix[i];
	}
	public int[] getColumn(int i)
	{
		int[] newMatrix = new int[8];
		for(int j = 0; j < 8; i++)
		{
			newMatrix[j] = matrix[j][i];
		}
		return newMatrix;
	}
	public int getIndividual(int i, int j)
	{
		return matrix[i][j];
	}
	
	public void setMatrix(int[][] newMatrix)
	{
		matrix = newMatrix;
	}
	public void setRow(int i, int[] row)
	{
		matrix[i] = row;
	}
	public void setColumn(int i, int[] column)
	{
		for(int j = 0; j < 8; j++)
		{
			matrix[j][i] = column[j];
		}
	}
	public void setIndividual(int i, int j, int value)
	{
		matrix[i][j] = value;
	}
	
	public void chessMove(int oldPos[], int newPos[], int type)
	{
		matrix[oldPos[0]][oldPos[1]] = 0;
		matrix[newPos[0]][newPos[1]] = type;
	}
	public int[] flipCoord(int oldPos[])
	{
		int[] newPos = new int[2];
		newPos[0] = (7-oldPos[0]);
		newPos[1] = (7-oldPos[1]);
		
		return newPos;
	}
	public void displayMatrix()
	{
		for(int i = 0; i < 8; i++)
		{
			System.out.print("[ ");
			for(int j = 0; j < 8; j++)
			{
				System.out.print(matrix[7-i][j] + " ");
			}
			System.out.print("]\n");
		}
	}
}
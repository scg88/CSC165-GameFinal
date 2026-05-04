package a2;

import tage.*;
import org.joml.*;
import java.util.ArrayList;
import java.util.List;

public class Board
{
	private Matrix8i boardState = new Matrix8i();
	private Tile[] tiles = new Tile[64];
	
	public Board(ObjShape s, TextureImage t)
	{
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				tiles[(i*8)+j] = new Tile((new int[] {i, j}), s, t);
				tiles[(i*8)+j].setLocalTranslation((new Matrix4f()).translation((float)(17.5-5*j), 0.2f, (float)(-17.5+5*i)));
				tiles[(i*8)+j].setLocalScale((new Matrix4f()).scaling(2.5f));
				tiles[(i*8)+j].getRenderStates().disableRendering();
			}
		}
	}
	
	public Matrix8i getBoard()
	{
		return boardState;
	}
	public void setBoard(Matrix8i newBoard)
	{
		boardState = newBoard;
	}
	public void displayBoard()
	{
		boardState.displayMatrix();
	}
	
	public int[][] validMoves(ChessPiece piece)
	{
		String type = piece.getType();
		int[] position = decode(piece.getCNPos());
		//System.out.println("Notation: " + piece.getCNPos() + "; Array Position: " + position[0] + ", " + position[1]);
		List<int[]> listOfSpots = new ArrayList<>();
		int i = 0;
		
		//Determines which spaces are valid moves
		switch(type)
		{
			case "Pawn":
				//Can move forwards?
				if(boardState.getIndividual(position[0]+1, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]});
				}
				//Can capture up+left?
				if(position[1] != 0 && boardState.getIndividual(position[0]+1, position[1]-1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]-1});
				}
				//Can capture up+right?
				if(position[1] != 7 && boardState.getIndividual(position[0]+1, position[1]+1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]+1});
				}
				//Can move 2 up?
				if(position[0] == 1 && boardState.getIndividual(position[0]+2, position[1]) == 0 && boardState.getIndividual(position[0]+2, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]+2, position[1]});
				}
				break;
			case "Rook":
				//How much can move forwards?
				i = 0;
				while(position[0]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]});
					i++;
				}
				if(position[0]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]});
				}
				//How much can move backwards?
				i = 0;
				while(position[0]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]});
					i++;
				}
				if(position[0]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]});
				}
				//How much can move left?
				i = 0;
				while(position[1]-i-1>-1 && boardState.getIndividual(position[0], position[1]-i-1) == 0)
				{
					listOfSpots.add(new int[]{position[0], position[1]-i-1});
					i++;
				}
				if(position[1]-i-1>-1 && boardState.getIndividual(position[0], position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0], position[1]-i-1});
				}
				//How much can move right?
				i = 0;
				while(position[1]+i+1<8 && boardState.getIndividual(position[0], position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0], position[1]+i+1});
					i++;
				}
				if(position[1]+i+1<8 && boardState.getIndividual(position[0], position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0], position[1]+i+1});
				}
				break;
			case "Knight":
				//Can L down-right-right?
				if(position[0]-1>-1 && position[1]+2<8 && boardState.getIndividual(position[0]-1, position[1]+2) != 1)
				{
					listOfSpots.add(new int[]{position[0]-1, position[1]+2});
				}
				//Can L up-right-right?
				if(position[0]+1<8 && position[1]+2<8 && boardState.getIndividual(position[0]+1, position[1]+2) != 1)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]+2});
				}
				//Can L down-down-right?
				if(position[0]-2>-1 && position[1]+1<8 && boardState.getIndividual(position[0]-2, position[1]+1) != 1)
				{
					listOfSpots.add(new int[]{position[0]-2, position[1]+1});
				}
				//Can L up-up-right?
				if(position[0]+2<8 && position[1]+1<8 && boardState.getIndividual(position[0]+2, position[1]+1) != 1)
				{
					listOfSpots.add(new int[]{position[0]+2, position[1]+1});
				}
				//Can L down-left-left?
				if(position[0]-1>-1 && position[1]-2>-1 && boardState.getIndividual(position[0]-1, position[1]-2) != 1)
				{
					listOfSpots.add(new int[]{position[0]-1, position[1]-2});
				}
				//Can L up-left-left?
				if(position[0]+1<8 && position[1]-2>-1 && boardState.getIndividual(position[0]+1, position[1]-2) != 1)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]-2});
				}
				//Can L down-down-left?
				if(position[0]-2>-1 && position[1]-1>-1 && boardState.getIndividual(position[0]-2, position[1]-1) != 1)
				{
					listOfSpots.add(new int[]{position[0]-2, position[1]-1});
				}
				//Can L up-up-left?
				if(position[0]+2<8 && position[1]-1>-1 && boardState.getIndividual(position[0]+2, position[1]-1) != 1)
				{
					listOfSpots.add(new int[]{position[0]+2, position[1]-1});
				}
				break;
			case "Bishop":
				//How much can move down-right?
				i = 0;
				while(position[0]-i-1>-1 && position[1]+i+1<8 && boardState.getIndividual(position[0]-i-1, position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]+i+1});
					i++;
				}
				if(position[0]-i-1>-1 && position[1]+i+1<8 && boardState.getIndividual(position[0]-i-1, position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]+i+1});
				}
				//How much can move up-right?
				i = 0;
				while(position[0]+i+1<8 && position[1]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]+i+1});
					i++;
				}
				if(position[0]+i+1<8 && position[1]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]+i+1});
				}
				//How much can move down-left?
				i = 0;
				while(position[0]-i-1>-1 && position[1]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]-i) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]-i-1});
					i++;
				}
				if(position[0]-i-1>-1 && position[1]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]-i-1});
				}
				//How much can move up-left?
				i = 0;
				while(position[0]+i+1<8 && position[1]-i-1>-1 && boardState.getIndividual(position[0]+i+1, position[1]-i-1) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]-i-1});
					i++;
				}
				if(position[0]+i+1<8 && position[1]-i-1>-1 && boardState.getIndividual(position[0]+i+1, position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]-i-1});
				}
				break;
			case "Queen":
				//How much can move forwards?
				i = 0;
				while(position[0]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]});
					i++;
				}
				if(position[0]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]});
				}
				//How much can move backwards?
				i = 0;
				while(position[0]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]});
					i++;
				}
				if(position[0]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]});
				}
				//How much can move left?
				i = 0;
				while(position[1]-i-1>-1 && boardState.getIndividual(position[0], position[1]-i-1) == 0)
				{
					listOfSpots.add(new int[]{position[0], position[1]-i-1});
					i++;
				}
				if(position[1]-i-1>-1 && boardState.getIndividual(position[0], position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0], position[1]-i-1});
				}
				//How much can move right?
				i = 0;
				while(position[1]+i+1<8 && boardState.getIndividual(position[0], position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0], position[1]+i+1});
					i++;
				}
				if(position[1]+i+1<8 && boardState.getIndividual(position[0], position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0], position[1]+i+1});
				}
				//How much can move down-right?
				i = 0;
				while(position[0]-i-1>-1 && position[1]+i+1<8 && boardState.getIndividual(position[0]-i-1, position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]+i+1});
					i++;
				}
				if(position[0]-i-1>-1 && position[1]+i+1<8 && boardState.getIndividual(position[0]-i-1, position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]+i+1});
				}
				//How much can move up-right?
				i = 0;
				while(position[0]+i+1<8 && position[1]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]+i+1) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]+i+1});
					i++;
				}
				if(position[0]+i+1<8 && position[1]+i+1<8 && boardState.getIndividual(position[0]+i+1, position[1]+i+1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]+i+1});
				}
				//How much can move down-left?
				i = 0;
				while(position[0]-i-1>-1 && position[1]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]-i) == 0)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]-i-1});
					i++;
				}
				if(position[0]-i-1>-1 && position[1]-i-1>-1 && boardState.getIndividual(position[0]-i-1, position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0]-i-1, position[1]-i-1});
				}
				//How much can move up-left?
				i = 0;
				while(position[0]+i+1<8 && position[1]-i-1>-1 && boardState.getIndividual(position[0]+i+1, position[1]-i-1) == 0)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]-i-1});
					i++;
				}
				if(position[0]+i+1<8 && position[1]-i-1>-1 && boardState.getIndividual(position[0]+i+1, position[1]-i-1) == 2)
				{
					listOfSpots.add(new int[]{position[0]+i+1, position[1]-i-1});
				}
				break;
			case "King":
				//Can move up?
				if(position[0]+1<8 && boardState.getIndividual(position[0]+1, position[1]) != 1)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]});
				}
				//Can move down?
				if(position[0]-1>-1 && boardState.getIndividual(position[0]-1, position[1]) != 1)
				{
					listOfSpots.add(new int[]{position[0]-1, position[1]});
				}
				//Can move left?
				if(position[1]-1>-1 && boardState.getIndividual(position[0], position[1]-1) != 1)
				{
					listOfSpots.add(new int[]{position[0], position[1]-1});
				}
				//Can move right?
				if(position[1]+1<8 && boardState.getIndividual(position[0], position[1]+1) != 1)
				{
					listOfSpots.add(new int[]{position[0], position[1]+1});
				}
				//Can move up-left?
				if(position[1]-1>-1 && position[0]+1<8 && boardState.getIndividual(position[0]+1, position[1]-1) != 1)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]-1});
				}
				//Can move up-right?
				if(position[1]+1<8 && position[0]+1<8 && boardState.getIndividual(position[0]+1, position[1]+1) != 1)
				{
					listOfSpots.add(new int[]{position[0]+1, position[1]+1});
				}
				//Can move down-left?
				if(position[1]-1>-1 && position[0]-1>-1 && boardState.getIndividual(position[0]-1, position[1]-1) != 1)
				{
					listOfSpots.add(new int[]{position[0]-1, position[1]-1});
				}
				//Can move down-right?
				if(position[1]+1<8 && position[0]-1>-1 && boardState.getIndividual(position[0]-1, position[1]+1) != 1)
				{
					listOfSpots.add(new int[]{position[0]-1, position[1]+1});
				}
				break;
			default:
				System.out.println("Error");
				break;
		}
		int[][] validSpots = listOfSpots.toArray(new int[listOfSpots.size()][2]);
		return validSpots;
	}
	public void showMoves(int[][] validSpots)
	{	
		boolean found;
		for(int i = 0; i < tiles.length; i++)
		{
			found = false;
			for(int j = 0; j < validSpots.length; j++)
			{
				if(tiles[i].getPos()[0] == validSpots[j][0] && tiles[i].getPos()[1] == validSpots[j][1])
				{
					tiles[i].getRenderStates().enableRendering();
					found = true;
				}
			}
			if(!found)
			{
				tiles[i].getRenderStates().disableRendering();
			}
		}
	}
	public int[] decode(String CNPos)
	{
		char sCol = CNPos.toUpperCase().charAt(0);
		char sRow = CNPos.toUpperCase().charAt(1);
		int iRow, iCol;
		
		switch(sCol)
		{
			case 'A':
				iCol = 0;
				break;
			case 'B':
				iCol = 1;
				break;
			case 'C':
				iCol = 2;
				break;
			case 'D':
				iCol = 3;
				break;
			case 'E':
				iCol = 4;
				break;
			case 'F':
				iCol = 5;
				break;
			case 'G':
				iCol = 6;
				break;
			case 'H':
				iCol = 7;
				break;
			default:
				System.out.println("AHHHHH");
				iCol = 0;
				break;
		}
		iRow = sRow - '1';
		
		int[] position = {iRow, iCol};
		return position;
	}
	public String encode(int[] arrayPos)
	{
		String sRow, sCol;
		String CNPos; 
		
		switch(arrayPos[1])
		{
			case 0:
				sCol = "a";
				break;
			case 1:
				sCol = "b";
				break;
			case 2:
				sCol = "c";
				break;
			case 3:
				sCol = "d";
				break;
			case 4:
				sCol = "e";
				break;
			case 5:
				sCol = "f";
				break;
			case 6:
				sCol = "g";
				break;
			case 7:
				sCol = "h";
				break;
			default:
				System.out.println("AHHHHH");
				sCol = "z";
				break;
		}
		sRow = String.valueOf(arrayPos[0] + 1);
		
		CNPos = sCol.concat(sRow);
		return CNPos;
	}
	public boolean movePiece(ChessPiece piece)
	{
		int[] situation = new int[3];
		boolean success;
		situation = validSpotCheck(piece);
		switch(situation[0])
		{
			case 0:
				success = false;
				break;
			case 1:
				success = true;
				boardState.chessMove(decode(piece.getCNPos()), (new int[]{situation[1], situation[2]}), 1);
				piece.setCNPos(encode((new int[]{situation[1], situation[2]})));
				boardState.displayMatrix();
				break;
			case 2:
				success = true;
				break;
			default:
				success = false;
				break;
		}
		//System.out.println(situation[0]);
		return success;
	}
	public int[] validSpotCheck(ChessPiece piece)
	{
		int[] situation = new int[3];
		situation[0] = 0;
		int[][] validMoves = validMoves(piece);
		int[] currentSpot = currentSpot(piece);
		// Outside of the board
		if(piece.getWorldLocation().x() > 17.5f || piece.getWorldLocation().x() < -17.5 || 
		piece.getWorldLocation().z() > 17.5f || piece.getWorldLocation().z() < -17.5)
		{
			System.out.println("Out of bounds!");
			situation[0] = 0;
		}
		//Piece hasn't moved
		else if(currentSpot[0] == decode(piece.getCNPos())[0] && currentSpot[1] == decode(piece.getCNPos())[1])
		{
			situation[0] = 2;
		}
		else
		{
			for(int i = 0; i < validMoves.length; i++)
			{	
				System.out.println("Valid Move: " + validMoves[i][0] + ", " + validMoves[i][1] + "; Current Spot: " + currentSpot[0] + ", " + currentSpot[1]);
				if(currentSpot[0] == validMoves[i][0] && currentSpot[1] == validMoves[i][1])
				{
					situation[0] = 1;
				}
			}
		}
		situation[1] = currentSpot[0];
		situation[2] = currentSpot[1];
		System.out.println(situation[0]);
		return situation;
	}
	public int[] currentSpot(ChessPiece piece)
	{
		int[] currentSpot = new int[2];
		for(int i = 0; i < 8; i++)
		{
			//System.out.println("World Pos: X-" + piece.getWorldLocation().x() + ", Z-" + piece.getWorldLocation().z() + "; Current Checker: i=" 
			//+ i + ", X/Z-" + ((float)17.5-5*i));
			if(piece.getWorldLocation().x() == (float)17.5-5*i){currentSpot[1]=i;}
			if(piece.getWorldLocation().z() == (float)-17.5+5*i){currentSpot[0]=i;}
		}
		return currentSpot;
	}
}